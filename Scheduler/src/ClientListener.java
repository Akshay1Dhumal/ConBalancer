import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * ClientListener is a distributed consumer that:
 * 1. Listens on a dedicated Kafka topic for migration commands from the scheduler
 * 2. Parses migration instructions (container ID, destination machine)
 * 3. Executes container migration scripts on worker nodes
 * 4. Reports migration status back to the scheduler
 * 
 * Each worker node runs one instance of ClientListener on its assigned topic.
 * The listener waits for migration events and triggers migrations when notified.
 * 
 * Implements Runnable for concurrent execution in thread pools.
 */
public class ClientListener implements Runnable {
	/** Map of machine ID to machine IP/hostname for SSH access and migration execution */
	private static HashMap<Integer, String> machineAddressMap = new HashMap<>();
	
	/** Kafka consumer for receiving migration commands */
	private final KafkaConsumer<String, String> consumer;
	
	/** Kafka topics this listener subscribes to (migration command channels) */
	private final List<String> topics;
	
	/** Listener instance ID for logging and debugging */
	private final int listenerId;

	/**
	 * Constructor for ClientListener.
	 * 
	 * @param listenerId Unique identifier for this listener instance
	 * @param zookeeperServers Zookeeper server addresses for Kafka coordination
	 * @param consumerGroupId Kafka consumer group identifier
	 * @param topics Kafka topics to subscribe to for migration commands
	 */
	public ClientListener(int listenerId, String zookeeperServers, String consumerGroupId, List<String> topics) {
		this.listenerId = listenerId;
		this.topics = topics;
		Properties kafkaProperties = new Properties();
		kafkaProperties.put("zookeeper.connect", zookeeperServers);
		kafkaProperties.put("group.id", consumerGroupId);
		kafkaProperties.put("bootstrap.servers", "10.21.235.181:9092,10.21.233.193:9092"); // Configure Kafka broker servers
		kafkaProperties.put("key.deserializer", StringDeserializer.class.getName());
		kafkaProperties.put("value.deserializer", StringDeserializer.class.getName());
		this.consumer = new KafkaConsumer<>(kafkaProperties);
	}

	/**
	 * Execute container migration by invoking the migration script.
	 * 
	 * Parses migration command and invokes migrate_v2.sh script which handles:
	 * 1. Container image extraction via Docker commit
	 * 2. Image push to registry
	 * 3. Container stop on source machine
	 * 4. Image pull on destination machine
	 * 5. Container restart on destination with preserved configuration
	 * 
	 * @param containerId Container ID to migrate
	 * @param destinationMachine Target machine ID for migration
	 */
	public void executeContainerMigration(String containerId, int destinationMachine) {
		try {
			String registryAddress = "registry:443/";
			String migrationCommand = "/opt/kafka_2.11-0.9.0.0/migrate_v2.sh " + containerId + " " + destinationMachine + " " + registryAddress;
			
			// Use ProcessBuilder for safer command execution
			ProcessBuilder processBuilder = new ProcessBuilder("/opt/kafka_2.11-0.9.0.0/migrate_v2.sh");
			processBuilder.redirectErrorStream(true);
			Process migrationProcess = processBuilder.start();
			
			// Capture and display migration output
			BufferedReader outputReader = new BufferedReader(new InputStreamReader(migrationProcess.getInputStream()));
			String outputLine;
			while (true) {
				outputLine = outputReader.readLine();
				if (outputLine == null) {
					break;
				}
				System.out.println(outputLine);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main listener loop: Continuously poll for migration commands and execute them.
	 * 
	 * The listener:
	 * 1. Subscribes to assigned topics
	 * 2. Polls for new migration commands from the scheduler
	 * 3. Parses command format: "containerID, destinationMachineID"
	 * 4. Triggers migration execution
	 * 5. Continues indefinitely until shutdown
	 */
	@Override
	public void run() {
		try {
			consumer.subscribe(topics);
			while (true) {
				System.out.println("Listener waiting for migration commands...");
				ConsumerRecords<String, String> migrationRecords = consumer.poll(Long.MAX_VALUE);
				
				// Process each migration command received
				for (ConsumerRecord<String, String> record : migrationRecords) {
					String commandMessage = record.value();
					String containerId = commandMessage.substring(0, commandMessage.indexOf(",")).trim();
					int destinationMachineId = Integer.parseInt(
						commandMessage.substring(commandMessage.indexOf(",") + 1, commandMessage.length()).trim()
					);
					
					System.out.println(record.topic() + " Container: " + containerId + 
						" Destination: " + destinationMachineId + 
						" DestIP: " + machineAddressMap.get(destinationMachineId));
					
					executeContainerMigration(containerId, destinationMachineId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			consumer.commitSync();
			consumer.close();
		}
	}

	/**
	 * Graceful shutdown trigger for the consumer.
	 */
	public void shutdown() {
		consumer.wakeup();
	}

	/**
	 * Main entry point for ClientListener.
	 * 
	 * Initializes the listener with machine addresses and starts listening for
	 * migration commands. Sets up thread pool for potential multiple listeners.
	 * 
	 * Command-line arguments:
	 *   args[0], args[1], ... : Machine IP addresses indexed by machine ID
	 * 
	 * @param args Machine addresses for each machine ID
	 */
	public static void main(String[] args) {
		int numConsumers = 1;
		String topic = "";
		String migrationCommandLine = "";
		String kafkaConsumerGroupId = "migration-consumer";
		
		// Populate machine address map from command-line arguments
		for (int i = 0; i < args.length; i++) {
			System.out.println("Machine " + i + " Address: " + args[i]);
			machineAddressMap.put(i, args[i]);
		}
		
		try {
			// Legacy: Read from configuration files (optional)
			/*
			File machineIdFile = new File("/opt/kafka_2.11-0.9.0.0/MachineID");
			BufferedReader fileReader = new BufferedReader(new FileReader(machineIdFile));
			topic = "L" + fileReader.readLine();
			
			File machineInfoFile = new File("/opt/kafka_2.11-0.9.0.0/MachineInfo");
			fileReader = new BufferedReader(new FileReader(machineInfoFile));
			String fileLineContent;
			while ((fileLineContent = fileReader.readLine()) != null) {
				int machineId = Integer.parseInt(fileLineContent.substring(0, fileLineContent.indexOf(" ")).trim());
				String machineAddress = fileLineContent.substring(fileLineContent.indexOf(" "), fileLineContent.length()).trim();
				System.out.println("Machine ID: " + machineId + " Address: " + machineAddress);
				machineAddressMap.put(machineId, machineAddress);
			}
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Migration topic to listen on: " + topic);
		List<String> topicsList = Arrays.asList(topic);
		final ExecutorService executorService = Executors.newFixedThreadPool(numConsumers);

		final List<ClientListener> consumerInstances = new ArrayList<>();
		for (int i = 0; i < numConsumers; i++) {
			ClientListener listenerInstance = new ClientListener(
				i, 
				"sparknode19:2181,sparknode18:2181,gas:2181",  // Zookeeper servers
				kafkaConsumerGroupId,
				topicsList
			);
			consumerInstances.add(listenerInstance);
			executorService.submit(listenerInstance);
		}

		// Graceful shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (ClientListener listener : consumerInstances) {
					listener.shutdown();
				}
				executorService.shutdown();
				try {
					executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
