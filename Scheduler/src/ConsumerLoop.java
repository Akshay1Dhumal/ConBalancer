import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.glassfish.jersey.message.internal.MatchingEntityTag;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.awt.Container;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Container statistics wrapper that associates raw container statistics
 * with its container identifier. Used for tracking container metrics internally.
 */
class ContainerStatsWrapper {
	/** Resource statistics (CPU, memory, I/O) for the container */
	Stats containerResourceStats;
	
	/** Docker container ID or name */
	String containerId;

	/**
	 * Get the container resource statistics.
	 * @return Resource statistics object with CPU, memory, I/O metrics
	 */
	public Stats getS() {
		return containerResourceStats;
	}

	/**
	 * Set the container resource statistics.
	 * @param stats Resource statistics object to associate
	 */
	public void setS(Stats stats) {
		this.containerResourceStats = stats;
	}

	/**
	 * Get the container identifier.
	 * @return Container ID or name
	 */
	public String getContainer_id() {
		return containerId;
	}

	/**
	 * Set the container identifier.
	 * @param containerId Container ID or name to set
	 */
	public void setContainer_id(String containerId) {
		this.containerId = containerId;
	}

	/**
	 * Constructor for ContainerStatsWrapper.
	 * @param stats Resource statistics
	 * @param containerId Container identifier
	 */
	public ContainerStatsWrapper(Stats stats, String containerId) {
		super();
		this.containerResourceStats = stats;
		this.containerId = containerId;
	}
}

/**
 * ContainerInfo represents complete information about a container
 * including its identifier, resource statistics, and current host machine.
 * This is the primary data structure used by the scheduler.
 */
class ContainerInfo {
	/** Unique Docker container identifier */
	String containerId;
	
	/** Resource statistics for this container */
	Stats containerStats;
	
	/** Kafka topic ID / machine ID where container currently resides */
	int hostMachineId;

	/**
	 * Default constructor for ContainerInfo.
	 */
	public ContainerInfo() {
	}

	/**
	 * Full constructor for ContainerInfo.
	 * @param containerId Container identifier
	 * @param containerStats Resource statistics
	 * @param hostMachineId Machine/machine ID where container is running
	 */
	public ContainerInfo(String containerId, Stats containerStats, int hostMachineId) {
		super();
		this.containerId = containerId;
		this.containerStats = containerStats;
		this.hostMachineId = hostMachineId;
	}

	/**
	 * Get container identifier.
	 * @return Container ID
	 */
	public String getContainer_id() {
		return containerId;
	}

	/**
	 * Set container identifier.
	 * @param containerId Container ID
	 */
	public void setContainer_id(String containerId) {
		this.containerId = containerId;
	}

	/**
	 * Get container resource statistics.
	 * @return Statistics object with CPU, memory, I/O metrics
	 */
	public Stats getContainer_stats() {
		return containerStats;
	}

	/**
	 * Set container resource statistics.
	 * @param containerStats Statistics object
	 */
	public void setContainer_stats(Stats containerStats) {
		this.containerStats = containerStats;
	}

	/**
	 * Get the machine ID where container currently resides.
	 * @return Machine ID / Kafka topic ID
	 */
	public int getMachine_id() {
		return hostMachineId;
	}

	/**
	 * Set the machine ID where container is running.
	 * @param hostMachineId Machine/topic ID
	 */
	public void setMachine_id(int hostMachineId) {
		this.hostMachineId = hostMachineId;
	}
}

/**
 * ConsumerLoop is the main orchestration component that:
 * 1. Receives container statistics from Kafka topics (one per machine)
 * 2. Aggregates and normalizes metrics across all containers
 * 3. Invokes the Genetic Algorithm to compute optimal placements
 * 4. Triggers container migrations via ClientListener
 * 5. Logs all decisions and outcomes for monitoring
 * 
 * Implements Runnable to execute as a concurrent task.
 */
public class ConsumerLoop implements Runnable {
	/** Kafka consumer for receiving statistics from producer topics */
	private final KafkaConsumer<String, String> consumer;
	
	/** List of Kafka topics to consume from (one per machine) */
	private final List<String> topics;
	
	/** List of machine IDs in the cluster */
	ArrayList<Integer> machineIdList = new ArrayList<>();
	
	/** Timing metrics for performance monitoring */
	long startTime, stopTime, elapsedTime;
	
	/** Current container information being processed */
	ContainerInfo currentContainerInfo;
	
	/** Aggregated resource usage across all containers */
	double totalCpuUsage, totalMemUsage, totalNetworkIn, totalNetworkOut, totalBlockIn, totalBlockOut;
	
	/** 
	 * Primary data structure: Map of machine ID -> List of containers on that machine.
	 * Used to track which containers are on which machines.
	 */
	HashMap<Integer, ArrayList<ContainerInfo>> machineContainersMap = new HashMap<>();
	
	/** List of all containers in the cluster with their information */
	ArrayList<ContainerInfo> allContainersInfo = new ArrayList<>();
	
	/** Number formatter for console logging */
	DecimalFormat numberFormat = new DecimalFormat("#.00");
	
	/** Logger for file-based operation tracking */
	Logger logger = Logger.getLogger("MyLog");
	
	/** File handler for logger output */
	FileHandler logFileHandler;
	
	/** Counter tracking GA invocations for debugging */
	static int geneticAlgorithmInvocations = 0;

	/** Wait period between optimization cycles (milliseconds) */
	int optimizationCyclePeriod = 5000; // Configurable

	/**
	 * Add or update container information in the internal tracking structure.
	 * 
	 * This method processes inbound container statistics from Kafka and maintains
	 * the current snapshot of container-to-machine mappings. If a container already
	 * exists on a machine, its statistics are updated. Otherwise, it's added as a
	 * new container on that machine.
	 * 
	 * @param statsArray Container statistics array in format: [timestamp, containerID, cpu%, mem%, ...]
	 * @param kafkaTopicId Machine ID (derived from Kafka topic) where container resides
	 */
	public void add_info(String[] statsArray, int kafkaTopicId) {
		try {
			// Parse raw statistics string into Stats object
			Stats containerMetrics = new Stats();
			containerMetrics.convertStats(statsArray);
			System.out.println("STATS " + containerMetrics.cpu_perc + "  ........  " + containerMetrics.mem_perc);
			
			// Create container information entry
			ContainerInfo containerEntry = new ContainerInfo();
			containerEntry.setContainer_id(statsArray[1]);
			containerEntry.setContainer_stats(containerMetrics);
			containerEntry.setMachine_id(kafkaTopicId);
			
			// Check if machine is already tracked in hashmap
			if (machineContainersMap.containsKey(kafkaTopicId)) {
				// Machine exists, check if container already on this machine
				ArrayList<ContainerInfo> machineContainers = machineContainersMap.get(kafkaTopicId);
				boolean containerFound = false;
				
				// Search for existing container entry
				for (int i = 0; i < machineContainers.size(); i++) {
					System.out.println("already in " + machineContainers.get(i).containerId);
					if (machineContainers.get(i).containerId.trim().equals(statsArray[1].trim())) {
						// Container exists, update its statistics
						machineContainers.remove(i);
						machineContainers.add(containerEntry);
						containerFound = true;
						break;
					}
				}
				
				// If container not found on this machine, add it as new entry
				if (!containerFound) {
					machineContainers.add(containerEntry);
				}
			} else {
				// New machine, create entry and add container
				ArrayList<ContainerInfo> newMachineContainers = new ArrayList<>();
				newMachineContainers.add(containerEntry);
				machineContainersMap.put(kafkaTopicId, newMachineContainers);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final int id;

	public ConsumerLoop(int id, String zookeeper, String groupId, List<String> topics) {
		this.id = id;
		this.topics = topics;
		Properties props = new Properties();
		props.put("zookeeper.connect", zookeeper);
		props.put("group.id", groupId);
		// props.put("bootstrap.servers", "sparknode18:9092,sparknode19:9092,gas:9092");
		// // Config the kafka broker servers here.
		props.put("bootstrap.servers", "10.21.235.181:9092,10.21.233.193:9092"); // Config the kafka broker servers
		// here.
		// props.put("group.id", groupId);
		props.put("key.deserializer", StringDeserializer.class.getName());
		props.put("value.deserializer", StringDeserializer.class.getName());

		this.consumer = new KafkaConsumer<>(props);
	}

	/**
	 * Preprocess the data
	 * 
	 * Normalize all the parameters before applying the GA algo
	 */
	public void preProcessData() {
		total_cpu_usage = total_mem_usage = total_neti = total_neto = total_blocki = total_blocko = 0;
		/*
		 * Retrieve data from array : test part and add the individual parameter
		 */
		Set<Integer> keys = hm.keySet(); // keys here indicate all machines
		for (int s : keys) {
			System.out.println("TEST1 " + s);
			ArrayList<ContainerInfo> al = hm.get(s);
			for (int i = 0; i < al.size(); i++) {
				System.out.println("TEST MODE1 " + al.get(i).container_id + " CPU perc  "
						+ al.get(i).getContainer_stats().cpu_perc + "  Mem perc "
						+ al.get(i).getContainer_stats().mem_perc + " Mem Usage"
						+ al.get(i).getContainer_stats().mem_usage + "  Mem Avail "
						+ al.get(i).getContainer_stats().mem_avail + " block io "
						+ al.get(i).getContainer_stats().block_o + " " + al.get(i).getContainer_stats().block_i + " "
						+ al.get(i).getContainer_stats().net_i + " " + al.get(i).getContainer_stats().net_o);

				total_cpu_usage += al.get(i).getContainer_stats().cpu_perc;
				total_mem_usage += al.get(i).getContainer_stats().mem_usage;
				total_blocki += al.get(i).getContainer_stats().block_i;
				total_blocko += al.get(i).getContainer_stats().block_o;
				total_neti += al.get(i).getContainer_stats().net_i;
				total_neto += al.get(i).getContainer_stats().net_o;
			}
		}

		System.out.println("Total cpu_usage " + total_cpu_usage + " Total mem usage " + total_mem_usage);
		// Normalizing the values
		for (int s : keys) {
			logger.info("TEST2 " + s);
			ArrayList<ContainerInfo> al = hm.get(s);
			for (int i = 0; i < al.size(); i++) {

				al.get(i).container_stats.n_mem_usage = Double
						.parseDouble(numberFormat.format(al.get(i).container_stats.mem_usage / total_mem_usage));
				al.get(i).container_stats.n_cpu_perc = Double
						.parseDouble(numberFormat.format(al.get(i).container_stats.cpu_perc / total_cpu_usage));
				if (total_neti != 0)
					al.get(i).container_stats.n_neti = Double
					.parseDouble(numberFormat.format(al.get(i).container_stats.net_i / total_neti));
				if (total_neto != 0)
					al.get(i).container_stats.n_neto = Double
					.parseDouble(numberFormat.format(al.get(i).container_stats.net_o / total_neto));
				if (total_blocki != 0)
					al.get(i).container_stats.n_blocki = Double
					.parseDouble(numberFormat.format(al.get(i).container_stats.block_i / total_blocki));
				if (total_blocko != 0)
					al.get(i).container_stats.n_blocko = Double
					.parseDouble(numberFormat.format(al.get(i).container_stats.block_o / total_blocko));
				logger.info("TEST MODE2 " + al.get(i).container_id + " CPU perc "
						+ al.get(i).getContainer_stats().n_cpu_perc + " Mem perc   "
						+ al.get(i).getContainer_stats().mem_perc + " Mem Usage"
						+ al.get(i).getContainer_stats().n_mem_usage + "  Mem Avail " + " mem avail  "
						+ al.get(i).getContainer_stats().mem_avail + " netI " + al.get(i).getContainer_stats().net_i);
			}
		}

	}

	public int getNumberContainer() {
		Set<Integer> keys = hm.keySet();
		int total_containers = 0;
		for (int s : keys) {
			total_containers += hm.get(s).size();
		}
		return total_containers;
	}

	/**
	 * Initialize GA algorithm
	 *
	 */
	public void GA_init() {
		c_info = new ArrayList<>();
		GeneticAlgorithm ga = new GeneticAlgorithm(1000, 0.1, 0.95, 1); // 100, 0.01, 0.95, 2
		// Initialize population
		Set<Integer> keys = hm.keySet();
		int machine_ids[] = new int[machine_list.size()];
		int i = 0;
		for (int a : machine_list) {
			machine_ids[i] = a;
			i++;
		}

		for (int s : keys) {
			ArrayList<ContainerInfo> al = hm.get(s);
			for (i = 0; i < al.size(); i++) {
				c_info.add(al.get(i));
			}
		}
		i = 0;
		int initial_placements[] = new int[c_info.size()];
		System.out.println("Unique container are");

		for (ContainerInfo c : c_info) {
			System.out.println(c.container_id + " " + c.getMachine_id());
			initial_placements[i] = c.getMachine_id();
			i++;
		}

		i = 0;
		Population population = ga.initPopulation(getNumberContainer(), machine_ids); // parameter : chromosome_length
		// and machine id
		// the chromosome length would be equal to number of containers: as each
		// container would list its possible set of machine
		// Evaluate population
		Individual ind = new Individual(initial_placements);

		double init_var = ga.calc_init_variance(ind, hm, c_info);
		System.out.println("Initial variance........................... " + init_var);

		ga.evalPopulation(population, hm, c_info, initial_placements);
		// Keep track of current generation

		int generation = 1;

		/**
		 * Start the evolution loop
		 * 
		 * Every genetic algorithm problem has different criteria for finishing. In this
		 * case, we know what a perfect solution looks like (we don't always!), so our
		 * isTerminationConditionMet method is very straightforward: if there's a member
		 * of the population whose chromosome is all ones, we're done!
		 */
		double avg = 0;
		// while (ga.isTerminationConditionMet(population) == false) {
		while (generation < 100) {
			// Print fittest individual from population
			// System.out.println("Best solution: " + population.getFittest(0).toString()+"
			// "+population.getFittest(0).getFitness());
			avg = avg + population.getPopulationFitness();
			// population.getPopulationFitness();
			// Apply crossover
			population = ga.crossoverPopulation(population, machine_ids, hm, c_info);
			// System.out.println("cr - "+population.getFittest(0).getFitness());
			// Apply mutation
			population = ga.mutatePopulation(population, machine_ids);
			// System.out.println("mt - "+population.getFittest(0).getFitness());
			// Evaluate population

			ga.evalPopulation(population, hm, c_info, initial_placements);

			// System.out.println("new
			// "+Arrays.toString(population.getIndividual(0).getChromosome()));
			
			/* Individual individualss[] = population.getIndividuals(); 
			 for(Individual in:individualss) {
			  System.out.println("new pop "+Arrays.toString(in.getChromosome())+" .. "+in.getFitness()); }
			 */

			 System.out.println("ev - "+population.getFittest(0).getFitness());
			// Increment the current generation
			generation++;
		}

		/**
		 * We're out of the loop now, which means we have a perfect solution on our
		 * hands. Let's print it out to confirm that it is actually all ones, as
		 * promised.
		 */
		System.out.println("Found solution in " + generation + " generations");
		System.out.println("Best solution: " + population.getFittest(0).getFitness() + " " + population.getFittest(0));
		try {
			// the following statement is used to log any messages
			// logger.info( population.getFittest(0).getFitness()+"\n");
			logger.info(avg / 10 + " ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		int[] fittest = population.getFittest(0).getIntegerRepresentation();
		/*
		 * Send the migration data to specific set of machines
		 */
		if (init_var > population.getFittest(0).getFitness()) { // if initial varaince is more than GA variance then
			// only migrate
			System.out.println("Condition met...Migrating");
			for (i = 0; i < fittest.length; i++) {
				// System.out.println( c_info.get(i).getContainer_id()+"New machine
				// "+fittest[i]+" Old machine "+(initial_placements[i]));
				// System.out.println("Machine "+(initial_placements[i])+" "+
				// c_info.get(i).getContainer_id()+" "+
				// c_info.get(i).getContainer_stats().cpu_perc+"
				// "+c_info.get(i).getContainer_stats().mem_perc);

				if (fittest[i] != initial_placements[i]) {
					System.out.println("L" + (initial_placements[i]) + " Listening ... Migrate "
							+ c_info.get(i).getContainer_id() + "  " + c_info.get(i).getContainer_stats().cpu_perc + " "
							+ c_info.get(i).getContainer_stats().mem_perc + " to L" + fittest[i]);
				
				MultiBrokerProducer.produce("L" + (initial_placements[i]),c_info.get(i).getContainer_id() + ",L" + fittest[i]);  //Comment this line if you dont need to migrate containers
				}
			}
		}

	}

	@Override
	public void run() {
		try {
			int topic;
			String msg = null;
			// fh = new FileHandler("/home/gas/Desktop/Docker_exp/MyLogFile.log");
			// logger.addHandler(fh);
			// SimpleFormatter formatter = new SimpleFormatter();
			// fh.setFormatter(formatter);

			int flag = 0, flag_time = 0;
			consumer.subscribe(topics);
			/*
			 * sparknode18 : abe7713df8be : 0.00% : 8.75 MiB / 7.816 GiB : 0.11% : 15.1 kB /
			 * 648 B : 7.38 MB / 0 B : 8195200 : 6242336 : 8 : 1 : total=2157 N0=2157 :
			 */
			for (int h = 0; h < 4; h++) {// to filter out old records. In 4 polls records are consumed.
				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
				System.out.println("Initial Records Are " + records.count());
			}
			while (true) {
				/*
				 * consumer.pause(); //This is done to remove all the accommodated previous data
				 * Thread.sleep(wait_period); consumer.resume();
				 */
				if (flag_time == 0) {
					startTime = System.currentTimeMillis();
					flag_time = 1;
				}

				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
				System.out.println("Records are " + records.count());

				if (flag != 0) {
					for (ConsumerRecord<String, String> record : records) {
						topic = Integer.parseInt(record.topic().substring(1, record.topic().length()));
						msg = record.value();
						System.out.println(msg + "   " + topic);
						String[] a = msg.split(":"); // a[0] is container id

						if (machine_list.contains(topic)) // if machine exists then simply check whether all containers
							// ARE PART OF IT
						{
							add_info(a, topic);// System.out.println("If condition");
						} else // create a new machine. Add all containers of it.
						{
							// System.out.println("else condition");
							machine_list.add(topic);
							add_info(a, topic);
						}
						// System.out.println("END
						// ..............................................................");
					}
					stopTime = System.currentTimeMillis();
					elapsedTime = stopTime - startTime;
					logger.info("Elapsed time " + elapsedTime);
					if (elapsedTime > 7000) {// tunable to get good number of records.
						flag_time = 0;
						preProcessData();
						// Test for loop
						int test = 1;
						//

						if (ga_calls < 1) {
							// for(int k=0;k<20;k++) {
							// System.out.println(test+"
							// TEST.................................................................................................................................................");
							GA_init();
							// }
							ga_calls++;
						} else {
							System.exit(0);
						}

					}
				}
				flag = 1;

			} // end of while loop

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
		//.out.println("Hello world");
		String groupid = "consumer-grp";
		// String topic[] = new String[] { "M2", "M1", "M3" };
		String topic[] = new String[] { "M0", "M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "M10", "M11",
		"M12" };

		List<String> topics = Arrays.asList(topic);
		ExecutorService executor = Executors.newFixedThreadPool(numConsumers);

		final List<ConsumerLoop> consumers = new ArrayList<>();
		for (int i = 0; i < numConsumers; i++) {
			ConsumerLoop consumer = new ConsumerLoop(i, " 10.21.235.181:2181,10.21.233.193:2181", groupid, topics);
			consumers.add(consumer);
			executor.submit(consumer);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (ConsumerLoop consumer : consumers) {
					consumer.shutdown();
				}
				executor.shutdown();
				try {
					executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
