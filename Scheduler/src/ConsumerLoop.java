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


class container_stats {
	Stats s; // Structure containing the statistics
	String container_id;

	public Stats getS() {
		return s;
	}

	public void setS(Stats s) {
		this.s = s;
	}

	public String getContainer_id() {
		return container_id;
	}

	public void setContainer_id(String container_id) {
		this.container_id = container_id;
	}

	public container_stats(Stats s, String container_id) {
		super();
		this.s = s;
		this.container_id = container_id;
	}
}

class ContainerInfo {
	String container_id;
	Stats container_stats;
	int machine_id; // here machine id refers to Machine topic

	public ContainerInfo() {

	}

	public ContainerInfo(String container_id, Stats container_stats, int machine_id) {
		super();
		this.container_id = container_id;
		this.container_stats = container_stats;
		this.machine_id = machine_id;
	}

	public String getContainer_id() {
		return container_id;
	}

	public void setContainer_id(String container_id) {
		this.container_id = container_id;
	}

	public Stats getContainer_stats() {
		return container_stats;
	}

	public void setContainer_stats(Stats container_stats) {
		this.container_stats = container_stats;
	}

	public int getMachine_id() {
		return machine_id;
	}

	public void setMachine_id(int topic) {
		this.machine_id = topic;
	}

}

public class ConsumerLoop implements Runnable {
	private final KafkaConsumer<String, String> consumer;
	private final List<String> topics;
	ArrayList<Integer> machine_list = new ArrayList<>();
	long startTime, stopTime, elapsedTime;
	ContainerInfo con_info;
	double total_cpu_usage, total_mem_usage, total_neti, total_neto, total_blocki, total_blocko;;
	HashMap<Integer, ArrayList<ContainerInfo>> hm = new HashMap<>(); // each machine_topic as key with its container
	ArrayList<ContainerInfo>c_info= new ArrayList<>();
	DecimalFormat numberFormat = new DecimalFormat("#.00");
	Logger logger = Logger.getLogger("MyLog");
	FileHandler fh;
	
	int wait_period = 5000; // Configurable
	/**
	 * Adds the container information to the list.
	 * 
	 *If the container and its data are already there, remove the existing data and update the new one.
	 *@param a 
	 *			 Container stats in specified format separated by : 
	 *@param topic
	 *			 The machine id form where the container resides
	 */
	public void add_info(String[] a, int topic) {
		try {
			Stats st = new Stats();
			st.convertStats(a);
			System.out.println("STATS " + st.cpu_perc + "  ........  " + st.mem_perc);
			con_info = new ContainerInfo();
			con_info.setContainer_id(a[1]);
			con_info.setContainer_stats(st);
			con_info.setMachine_id(topic);
			if (hm.containsKey(topic)) // hashmap has registered that machine, then
			{
				ArrayList<ContainerInfo> aci = hm.get(topic);
				boolean flag = true;
				// loop to see if container exists in that machine. If it exists, then remove
				// and add again
				for (int i = 0; i < aci.size(); i++) {
					System.out.println("alreday in " + aci.get(i).container_id);
					if (aci.get(i).container_id.trim().equals(a[1].trim())) // if container exists in the machine
					{
						aci.remove(i);
						aci.add(con_info);
						flag = false;
						break;
					}
				}
				if (flag) { // if that container is not listed in that machine:
					aci.add(con_info);
				}
			} else // new topic: ie machine add to hashmap
			{
				ArrayList<ContainerInfo> al = new ArrayList<>();
				al.add(con_info);
				hm.put(topic, al);
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
		props.put("bootstrap.servers", "sparknode19:9092,gas:9092"); // Config the kafka broker servers here.
		props.put("group.id", groupId);
		props.put("key.deserializer", StringDeserializer.class.getName());
		props.put("value.deserializer", StringDeserializer.class.getName());
		this.consumer = new KafkaConsumer<>(props);
	}
	/**
	 * Preprocess the data 
	 * 
	 *Normalize all the parameters before applying the GA algo
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
						+ al.get(i).getContainer_stats().mem_avail + " NetI " + al.get(i).getContainer_stats().block_o
						+ " " + al.get(i).getContainer_stats().block_i);

				total_cpu_usage += al.get(i).getContainer_stats().cpu_perc;
				total_mem_usage += al.get(i).getContainer_stats().mem_usage;
				total_blocki += al.get(i).getContainer_stats().block_i;
				total_blocko += al.get(i).getContainer_stats().block_o;
				total_neti += al.get(i).getContainer_stats().net_i;
				total_neto += al.get(i).getContainer_stats().net_o;
			}
		}

		System.out.println("Total cpu usage " + total_cpu_usage + " Total mem usage " + total_mem_usage);
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
	
	public int getNumberContainer()
	{
		Set<Integer> keys = hm.keySet();
	    int total_containers=0;
		for (int s : keys) {
			total_containers+= hm.get(s).size();
		}
		return total_containers;
	}

	/**
	 *Initialize GA algorithm
	 *
	 */
	public void GA_init()
	{
		c_info=new ArrayList<>();
		GeneticAlgorithm ga = new GeneticAlgorithm(10, 0.001, 0.95, 2);
		// Initialize population
		Set<Integer> keys = hm.keySet();
	
		int machine_ids[] = new int[machine_list.size()];int i=0;
		for(int a:machine_list) {
			machine_ids[i]=a;i++;
		}
	
		
		for (int s : keys) {
			ArrayList<ContainerInfo> al= hm.get(s);
			for ( i = 0; i < al.size(); i++) {
				c_info.add(al.get(i));
			}
		}
		i=0;	int initial_placements[]=new int[c_info.size()];
		System.out.println("Unique container are");
		for(ContainerInfo c:c_info)
		{
			System.out.println(c.container_id+" "+c.getMachine_id());
			initial_placements[i]=c.getMachine_id();i++;
		}
		
		i=0;
		Population population = ga.initPopulation(getNumberContainer(),machine_ids); //parameter : chromosome_length and machine id
		//the chromosome length would be equal to number of containers: as each container would list its possible set of machine
		// Evaluate population
		
		ga.evalPopulation(population,hm,c_info,initial_placements);
		// Keep track of current generation
		
		int generation = 1;

		/**
		 * Start the evolution loop
		 * 
		 * Every genetic algorithm problem has different criteria for finishing.
		 * In this case, we know what a perfect solution looks like (we don't
		 * always!), so our isTerminationConditionMet method is very
		 * straightforward: if there's a member of the population whose
		 * chromosome is all ones, we're done!
		 */
	//	while (ga.isTerminationConditionMet(population) == false) {
		while(generation<10) {
			// Print fittest individual from population
			System.out.println("Best solution: " + population.getFittest(0).toString());

			// Apply crossover
			population = ga.crossoverPopulation(population,machine_ids);

			// Apply mutation
			population = ga.mutatePopulation(population);

			// Evaluate population
			ga.evalPopulation(population,hm,c_info,initial_placements);

			// Increment the current generation
			generation++;
		}

		/**
		 * We're out of the loop now, which means we have a perfect solution on
		 * our hands. Let's print it out to confirm that it is actually all
		 * ones, as promised.
		 */
		System.out.println("Found solution in " + generation + " generations");
		System.out.println("Best solution: " + population.getFittest(0).getFitness()+" "+population.getFittest(0));
		
		int[] fittest=population.getFittest(0).getIntegerRepresentation();
		/*
		 * Send the migration data to specific set of machines
		 */
		
		for(i=0;i<fittest.length;i++)
		{
			if(fittest[i]!=initial_placements[i])
			{
				System.out.println("L"+(initial_placements[i]+1)+"  && "+ c_info.get(i).getContainer_id()+",L"+fittest[i]);
			MultiBrokerProducer.produce("L"+(initial_placements[i]+1),c_info.get(i).getContainer_id()+",L"+fittest[i]);
			}
		}

	}
	
	
	
	@Override
	public void run() {
		try {
			int topic ;
			String msg = null;
			fh = new FileHandler("/home/gas/Desktop/Docker_exp/MyLogFile.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
			int flag = 0, flag_time = 0;
			consumer.subscribe(topics);
			/*
			 * sparknode18 : abe7713df8be : 0.00% : 8.75 MiB / 7.816 GiB : 0.11% : 15.1 kB /
			 * 648 B : 7.38 MB / 0 B : 8195200 : 6242336 : 8 : 1 : total=2157 N0=2157 :
			 */

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
						topic = Integer.parseInt(record.topic().substring(1,record.topic().length()));
						msg = record.value();
						//System.out.println(msg+ " .hello world.  "+topic);
						String[] a = msg.split(":"); // a[0] is container id

						if (machine_list.contains(topic)) // if machine exists then simply check whether all containers ARE PART OF IT
						{
							add_info(a, topic);// System.out.println("If condition");
						} else // create a new machine. Add all containers of it.
						{
							// System.out.println("else condition");
							machine_list.add(topic);
							add_info(a, topic);
						}
					System.out.println("END ..............................................................");
					}

					stopTime = System.currentTimeMillis();
					elapsedTime = stopTime - startTime;
					logger.info("Elapsed time " + elapsedTime);
					if (elapsedTime > 5000) {
						flag_time = 0;
						preProcessData();
						GA_init();

					}

				}
				flag = 1;
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
		String groupid = "consumer-tutorial-group";
		String topic[] = new String[] { "M1", "M2", "M3" };

		List<String> topics = Arrays.asList(topic);
		ExecutorService executor = Executors.newFixedThreadPool(numConsumers);

		final List<ConsumerLoop> consumers = new ArrayList<>();
		for (int i = 0; i < numConsumers; i++) {
			ConsumerLoop consumer = new ConsumerLoop(i, " sparknode19:2181,sparknode18:2181,gas:2181", groupid, topics);
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
