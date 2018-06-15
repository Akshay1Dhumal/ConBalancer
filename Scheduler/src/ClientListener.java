import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

public class ClientListener implements Runnable {

	private static  final HashMap<Integer, String> hm_machine_ips;
	private final KafkaConsumer<String, String> consumer;
	private final List<String> topics;
	private final int id;

	public ClientListener(int id, String zookeeper, String groupId, List<String> topics) {
		this.id = id;
		this.topics = topics;

		Properties props = new Properties();
		props.put("zookeeper.connect", zookeeper);
		props.put("group.id", groupId);
		props.put("bootstrap.servers", "sparknode19:9092,sparknode18:9092,gas:9092"); // Config the kafka broker servers
																						// here.
		// props.put("group.id", groupId);
		props.put("key.deserializer", StringDeserializer.class.getName());
		props.put("value.deserializer", StringDeserializer.class.getName());
		this.consumer = new KafkaConsumer<>(props);
	}

	public void executeCommand(String cont_id, String dest) {

		try {
			String command = "/opt/kafka_2.11-0.9.0.0/migrate.sh " + cont_id + " " + dest;
			Process proc = Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("Hello world ");
				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
				for (ConsumerRecord<String, String> record : records) {
					String msg = record.value();
					String cont_id = msg.substring(0, msg.indexOf(",")).trim();
					String dest_machine = msg.substring(msg.indexOf(",") + 1, msg.length()).trim();
					System.out.println(record.topic() + " Container  " + cont_id + " Dest: " + dest_machine);
					executeCommand(cont_id, dest_machine);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			consumer.commitSync();
			consumer.close();
		}
	}

	public void shutdown() {
		consumer.wakeup();
	}

	public static void main(String[] args) {
		int numConsumers = 1;
		String topic = "";
		String readLine = "";
		String groupid = "consumer";
		// Reading file MachineID to determine which topic to listen to
		try {
			File file = new File("/opt/kafka_2.11-0.9.0.0/MachineID");
			BufferedReader br = new BufferedReader(new FileReader(file));
			topic = "L" + br.readLine();
			file = new File("/opt/kafka_2.11-0.9.0.0/MachineInfo");
			br = new BufferedReader(new FileReader(file));
			while ((readLine = br.readLine()) != null) {
				
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Topic read from file is " + topic);
		List<String> topics = Arrays.asList(topic);
		final ExecutorService executor = Executors.newFixedThreadPool(numConsumers);

		final List<ClientListener> consumers = new ArrayList<>();
		for (int i = 0; i < numConsumers; i++) {
			ClientListener consumer = new ClientListener(i, " sparknode19:2181,sparknode18:2181,gas:2181", groupid,
					topics);
			consumers.add(consumer);
			executor.submit(consumer);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (ClientListener consumer : consumers) {
					consumer.shutdown();
				}
				executor.shutdown();
				try {
					executor.awaitTermination(100, TimeUnit.MILLISECONDS);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
