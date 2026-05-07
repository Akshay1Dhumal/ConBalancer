/**
 * MultiBrokerProducer publishes container statistics to Kafka brokers.
 * 
 * This producer:
 * 1. Connects to multiple Kafka brokers for high availability
 * 2. Uses custom partitioner to distribute messages across brokers
 * 3. Sends container statistics from worker nodes to the scheduler
 * 4. Implements request acknowledgment for reliability
 * 
 * Usage: Call produce(topic, message) to send statistics to a Kafka topic.
 */
public class MultiBrokerProducer {
	/** Kafka producer for sending statistics messages */
	private static Producer<Integer, String> kafkaProducer;
	
	/** Configuration properties for Kafka producer */
	private final Properties producerProperties = new Properties();

	/**
	 * Constructor initializes Kafka producer with broker configuration.
	 * Sets up partitioner, serializers, and acknowledgment settings.
	 */
	public MultiBrokerProducer() {
		// Configure list of Kafka brokers
		producerProperties.put("metadata.broker.list", "10.21.235.181:9092,10.21.233.193:9092");
		
		// Set message serializer
		producerProperties.put("serializer.class", "kafka.serializer.StringEncoder");
		
		// Use custom partitioner for distribution
		producerProperties.put("partitioner.class", "SimplePartitioner");
		
		// Request acknowledgment from broker before returning
		producerProperties.put("request.required.acks", "1");
		
		ProducerConfig producerConfig = new ProducerConfig(producerProperties);
		kafkaProducer = new Producer<>(producerConfig);
	}

	/**
	 * Send statistics message to a Kafka topic.
	 * 
	 * @param kafkaTopic Topic name (derived from machine ID)
	 * @param statisticsMessage Container statistics formatted as "containerID,stats..."
	 */
	public static void produce(String kafkaTopic, String statisticsMessage) {
		new MultiBrokerProducer();
		Random random = new Random();
		System.out.println("Publishing statistics - Topic: " + kafkaTopic + " Message: " + statisticsMessage);
		
		// Send message with round-robin partitioning
		kafkaProducer.send(new KeyedMessage<Integer, String>(kafkaTopic, statisticsMessage));
		kafkaProducer.close();
	}
}