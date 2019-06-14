
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;
import java.util.Random;


public class MultiBrokerProducer {
	private static Producer<Integer, String> producer;
	private final Properties properties = new Properties();

	public MultiBrokerProducer() {
		properties.put("metadata.broker.list", "10.21.235.181:9092,10.21.233.193:9092");
		properties.put("serializer.class", "kafka.serializer.StringEncoder");
		properties.put("partitioner.class", "SimplePartitioner");
		properties.put("request.required.acks", "1");
		ProducerConfig config = new ProducerConfig(properties);
		producer = new Producer<>(config);
	}

	public static void produce(String topic, String msg) {
		new MultiBrokerProducer();
		Random random = new Random();
		System.out.println("Listener msg "+ topic+" " +msg);
		//producer.send(new KeyedMessage<Integer, String>(topic, msg));
       /* for (long i = 0; i < 10; i++) {
            Integer key = random.nextInt(255);
            msg = "This message is for key - " + key;
           // producer.send(new KeyedMessage<Integer, String>(topic.trim(), msg.trim()));
        }*/
		producer.send(new KeyedMessage<Integer, String>(topic, msg));
		producer.close();
	}
	/*public static void main(String ar[])
	{
		new MultiBrokerProducer();
		for (long i = 0; i < 10; i++) {
			Random random = new Random();
            Integer key = random.nextInt(255);
           String  msg = "This message is for key - " + key;
            producer.send(new KeyedMessage<Integer, String>("l1", msg.trim()));
         //   producer.close();
	}
}*/
}