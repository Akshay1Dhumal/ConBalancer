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

public class ClientListener implements Runnable {
	private static  HashMap<Integer, String> hm_machine_ips=new HashMap<>();;
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

	public void executeCommand(String cont_id, int dest_machine) {

		try {
			String command = "/opt/kafka_2.11-0.9.0.0/migrate.sh " + cont_id + " " + dest_machine;
			//Process proc = Runtime.getRuntime().exec(command);
			
			ProcessBuilder builder = new ProcessBuilder("/opt/kafka_2.11-0.9.0.0/migrate.sh");
		        builder.redirectErrorStream(true);
		        Process p = builder.start();
		        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		        String line;
		        while (true) {
		            line = r.readLine();
		            if (line == null) { break; }
		            System.out.println(line);
		        }
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			consumer.subscribe(topics);
			while (true) {
				System.out.println("Hello world ");
				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
				for (ConsumerRecord<String, String> record : records) {
					String msg = record.value();
					String cont_id = msg.substring(0, msg.indexOf(",")).trim();
					int dest_machine = Integer.parseInt(msg.substring(msg.indexOf(",") + 2, msg.length()).trim());
					System.out.println(record.topic() + " Container  " + cont_id + " Dest: " + dest_machine+" "+hm_machine_ips.get(dest_machine));
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
			file=null;
			file = new File("/opt/kafka_2.11-0.9.0.0/MachineInfo");
			br=null;
			br = new BufferedReader(new FileReader(file));
			while ((readLine = br.readLine()) != null) {
				int m_id=Integer.parseInt(readLine.substring(0, readLine.indexOf(" ")).trim());
				String hostname_ip=readLine.substring(readLine.indexOf(" "),readLine.length()).trim();
				System.out.println("Mid "+m_id+" Hostname "+hostname_ip);
				hm_machine_ips.put(m_id,hostname_ip);
			}
			System.out.println("Mid is "+hm_machine_ips.keySet());
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
