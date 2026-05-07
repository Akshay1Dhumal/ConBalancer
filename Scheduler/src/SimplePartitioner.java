/**
 * SimplePartitioner implements a custom Kafka partitioner for message distribution.
 * 
 * This partitioner uses a modulo operation to distribute messages across partitions
 * based on the integer key, ensuring:
 * 1. Deterministic placement (same key always goes to same partition)
 * 2. Even load distribution across partitions
 * 3. Locality awareness for related messages
 */
import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

@SuppressWarnings("UnusedDeclaration")
public class SimplePartitioner implements Partitioner {
	/**
	 * Constructor for SimplePartitioner.
	 * @param properties Kafka producer properties (not used)
	 */
	public SimplePartitioner(VerifiableProperties properties) {
	}

	/**
	 * Determine which partition a message should be sent to.
	 * 
	 * Uses a modulo operation: partition = key % numberOfPartitions
	 * This ensures:
	 * - All messages with the same key go to the same partition
	 * - Messages are evenly distributed across partitions
	 * - Preserves ordering within each partition
	 * 
	 * @param key Message key (typically a machine ID in our case)
	 * @param numberOfPartitions Total number of partitions available
	 * @return partitionId The partition index where message should be sent (0-based)
	 */
	public int partition(Object key, int numberOfPartitions) {
		int partitionId = 0;
		int intKey = (Integer) key;
		if (intKey > 0) {
			partitionId = intKey % numberOfPartitions;
		}
		return partitionId;
	}
}