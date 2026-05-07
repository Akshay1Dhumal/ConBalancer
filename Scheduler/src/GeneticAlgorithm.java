
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.management.loading.MLet;

import scala.util.Random;

/**
 * GeneticAlgorithm class implements an evolutionary computation algorithm
 * for optimal container-to-node placement in a distributed cluster environment.
 * 
 * The algorithm works by:
 * 1. Maintaining a population of candidate placement solutions
 * 2. Evaluating fitness based on variance and migration costs
 * 3. Selecting, crossing over, and mutating solutions
 * 4. Converging toward optimal placement after multiple generations
 * 
 * Optimization considers two objectives:
 * - Minimize variance in resource utilization across nodes
 * - Minimize number of container migrations required
 */
public class GeneticAlgorithm {
	/**
	 * OptimizationMetric represents combined fitness metrics for a placement solution.
	 * Encapsulates variance (resource utilization balance) and migration count.
	 */
	class OptimizationMetric {
		/** Variance in resource utilization across cluster nodes (lower is better) */
		double resourceUtilizationVariance;
		
		/** Number of container migrations required to achieve this placement (lower is better) */
		double migrationCost;

		/**
		 * Get the resource utilization variance metric.
		 * @return variance value (lower indicates more balanced resource distribution)
		 */
		public double getVariance() {
			return resourceUtilizationVariance;
		}

		/**
		 * Full constructor for OptimizationMetric creation.
		 * @param variance Resource utilization variance across nodes
		 * @param migration Number of migrations required
		 */
		public OptimizationMetric(double variance, double migration) {
			super();
			this.resourceUtilizationVariance = variance;
			this.migrationCost = migration;
		}

		/**
		 * Default constructor for OptimizationMetric.
		 */
		public OptimizationMetric() {
		}

		/**
		 * Set the resource utilization variance.
		 * @param variance New variance value
		 */
		public void setVariance(double variance) {
			this.resourceUtilizationVariance = variance;
		}

		/**
		 * Get the migration cost metric.
		 * @return number of container migrations required
		 */
		public double getMigration() {
			return migrationCost;
		}

		/**
		 * Set the migration cost.
		 * @param migration Number of migrations required
		 */
		public void setMigration(int migration) {
			this.migrationCost = migration;
		}
	}

	/** Number of individuals in each generation */
	private int populationSize;

	/**
	 * Mutation rate is the fractional probability that an individual gene will
	 * mutate randomly in a given generation. The range is 0.0-1.0, but is generally
	 * small (on the order of 0.1 or less). Mutation introduces diversity to prevent
	 * premature convergence.
	 */
	private double mutationRate;

	/**
	 * Crossover rate is the fractional probability that two individuals will "mate"
	 * with each other, sharing genetic information, and creating offspring with
	 * traits of each of the parents. Range is 0.0-1.0 but typically small.
	 * High crossover promotes exploitation of good solutions.
	 */
	private double crossoverRate;

	/**
	 * Elitism is the concept that the strongest members of the population should be
	 * preserved from generation to generation. Elite individuals are not mutated or
	 * crossed over, ensuring good solutions persist.
	 * Count specifies the exact number of elite individuals to preserve.
	 */
	private int elitismCount;

	/**
	 * Constructor to initialize the Genetic Algorithm with parameters.
	 * @param populationSize Number of solutions in each generation
	 * @param mutationRate Probability of random gene changes (0.0-1.0)
	 * @param crossoverRate Probability of solution breeding (0.0-1.0)
	 * @param elitismCount Number of best solutions to preserve each generation
	 */
	public GeneticAlgorithm(int populationSize, double mutationRate, double crossoverRate, int elitismCount) {
		this.populationSize = populationSize;
		this.mutationRate = mutationRate;
		this.crossoverRate = crossoverRate;
		this.elitismCount = elitismCount;
	}

	/**
	 * Initialize a new population of random container placement solutions.
	 * This creates the first generation with random but valid placements.
	 * 
	 * @param chromosomeLength Number of containers in the cluster
	 * @param machineIds Array of valid machine/node identifiers
	 * @return population Newly generated population of placement solutions
	 */
	public Population initPopulation(int chromosomeLength, int[] machineIds) {
		// Initialize population with random container-to-node assignments
		Population population = new Population(this.populationSize, chromosomeLength, machineIds);
		return population;
	}

	/**
	 * Calculate the arithmetic mean (average) of a numeric array.
	 * Used for computing average resource utilization per machine.
	 * 
	 * @param resourceValues Array of resource metric values
	 * @return mean The arithmetic average of all values
	 */
	public double getMean(double[] resourceValues) {
		double sum = 0;
		for (double value : resourceValues) {
			sum += value;
		}
		return sum / (double) resourceValues.length;
	}

	/**
	 * Calculate the number of container migrations required to achieve
	 * this placement solution from the initial (current) placement.
	 * 
	 * @param solution Individual solution representing target placement
	 * @param initialPlacement Current placement of containers before optimization
	 * @return migrationCount Number of containers that need to move
	 */
	public int calculateMigrationCount(Individual solution, int[] initialPlacement) {
		int totalMigrations = 0;
		
		// Compare each container's current position with its target position
		for (int containerIndex = 0; containerIndex < solution.getChromosomeLength(); containerIndex++) {
			if (solution.getGene(containerIndex) != (initialPlacement[containerIndex])) {
				totalMigrations++;
			}
		}
		
		return totalMigrations;
	}

	/**
	 * Calculate fitness for an individual.
	 * 
	 * In this case, the fitness score is very simple: it's the number of ones in
	 * the chromosome. Don't forget that this method, and this whole
	 * GeneticAlgorithm class, is meant to solve the problem in the "AllOnesGA"
	 * class and example. For different problems, you'll need to create a different
	 * version of this method to appropriately calculate the fitness of an
	 * individual.
	 * 
	 * @param individual
	 *            the individual to evaluate
	 * @return double The fitness value for individual
	 */

	public double calc_init_variance(Individual individual, HashMap<Integer, ArrayList<ContainerInfo>> hm,
			ArrayList<ContainerInfo> c_info) {
		// Track number of correct genes
		int i = 0, p = 0;
		// double weight[]= {0.5,0.5,0,0,0,0};
		double parameter[][] = new double[6][hm.keySet().size()]; // statically fixing the number of parameter as 5 now.
		/*
		 * double total_cpu_machine[]=new double[hm.keySet().size()]; double
		 * total_mem_machine[]=new double[hm.keySet().size()]; double
		 * total_neti_machine[]=new double[hm.keySet().size()]; double
		 * total_neto_machine[]=new double[hm.keySet().size()]; double
		 * total_blocki_machine[]=new double[hm.keySet().size()]; double
		 * total_blocko_machine[]=new double[hm.keySet().size()];
		 */
		double diff = 0, temp = 0, square = 0, variance = 0;
		// Loop over individual's genes

		for (p = 0; p <= 5; p++) {
			for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
				/*
				 * total_cpu_machine[individual.getGene(geneIndex)]+=c_info.get(geneIndex).
				 * getContainer_stats().n_cpu_perc;
				 * total_mem_machine[individual.getGene(geneIndex)]+=c_info.get(geneIndex).
				 * getContainer_stats().n_mem_usage;
				 */
				switch (p) {
				case 0:
					// System.out.println(p+" "+geneIndex);
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex)
							.getContainer_stats().n_cpu_perc;
					break;
				case 1:
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex)
							.getContainer_stats().n_mem_usage;
					break;
				case 2:
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex).getContainer_stats().n_neti;
					break;
				case 3:
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex).getContainer_stats().n_neto;
					break;
				case 4:
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex).getContainer_stats().n_blocki;
					break;
				case 5:
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex).getContainer_stats().n_blocko;
					break;
				}
			}
		}
		for (p = 0; p <= 5; p++) { // for all parameters
			temp = 0;
			for (i = 0; i < hm.keySet().size(); i++) { // uptil all machines
				diff = getMean(parameter[p]) - parameter[p][i];
				square = diff * diff;
				temp = temp + square;
				// System.out.println("Temp "+temp);
			}
			temp = (double) temp / (double) (hm.keySet().size());
			// variance+=weight[p]*temp; //weight the parameters according to your choice
			variance += temp; // weight the parameters according to your choice
		}

		return variance;
	}

	public metric calcFitness(Individual individual, HashMap<Integer, ArrayList<ContainerInfo>> hm,
			ArrayList<ContainerInfo> c_info, int[] init_placements) {

		// Track number of correct genes
		int i = 0, p = 0;
		// double weight[]= {0.5,0.5,0,0,0,0};
		double parameter[][] = new double[6][hm.keySet().size()]; // statically fixing the number of parameter as 5 now.
		/*
		 * double total_cpu_machine[]=new double[hm.keySet().size()]; double
		 * total_mem_machine[]=new double[hm.keySet().size()]; double
		 * total_neti_machine[]=new double[hm.keySet().size()]; double
		 * total_neto_machine[]=new double[hm.keySet().size()]; double
		 * total_blocki_machine[]=new double[hm.keySet().size()]; double
		 * total_blocko_machine[]=new double[hm.keySet().size()];
		 */
		double diff = 0, temp = 0, square = 0, variance = 0;
		// Loop over individual's genes

		for (p = 0; p <= 5; p++) {
			for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
				/*
				 * total_cpu_machine[individual.getGene(geneIndex)]+=c_info.get(geneIndex).
				 * getContainer_stats().n_cpu_perc;
				 * total_mem_machine[individual.getGene(geneIndex)]+=c_info.get(geneIndex).
				 * getContainer_stats().n_mem_usage;
				 */
				switch (p) {
				case 0:
					// System.out.println(p+" "+geneIndex);
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex)
							.getContainer_stats().n_cpu_perc;
					break;
				case 1:
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex)
							.getContainer_stats().n_mem_usage;
					break;
				case 2:
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex).getContainer_stats().n_neti;
					break;
				case 3:
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex).getContainer_stats().n_neto;
					break;
				case 4:
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex).getContainer_stats().n_blocki;
					break;
				case 5:
					parameter[p][individual.getGene(geneIndex)] += c_info.get(geneIndex).getContainer_stats().n_blocko;
					break;
				}
			}
		}
		for (p = 0; p <= 5; p++) { // for all parameters
			temp = 0;
			for (i = 0; i < hm.keySet().size(); i++) { // uptil all machines
				diff = getMean(parameter[p]) - parameter[p][i];
				square = diff * diff;
				temp = temp + square;
				// System.out.println("Temp "+temp);
			}
			temp = (double) temp / (double) (hm.keySet().size());
			// variance+=weight[p]*temp; //weight the parameters according to your choice
			variance += temp; // weight the parameters according to your choice
		}

		// Use number of container migrations as part of variance :
		metric m = new metric(variance, numberMigrations(individual, init_placements));

		// System.out.println("Variance "+variance+ " Individual
		// "+individual.toString()+" Migrations "+numberMigrations(individual,
		// init_placements) );
		// double fitness = (double) correctGenes / individual.getChromosomeLength();

		// Store fitness
		// individual.setFitness(variance); //Commenting now
		return m;
		// return variance;
	}

	/**
	 * Evaluate the whole population
	 * 
	 * Essentially, loop over the individuals in the population, calculate the
	 * fitness for each, and then calculate the entire population's fitness. The
	 * population's fitness may or may not be important, but what is important here
	 * is making sure that each individual gets evaluated.
	 * 
	 * @param population
	 *            the population to evaluate
	 */
	public void evalPopulation(Population population, HashMap<Integer, ArrayList<ContainerInfo>> hm,
			ArrayList<ContainerInfo> c_info, int[] init_placements) {
		metric marray[] = new metric[populationSize];
		double populationFitness = 0;
		int i = 0;
		// Loop over population evaluating individuals and suming population
		// fitness
		for (Individual individual : population.getIndividuals()) {
			// populationFitness += calcFitness(individual, hm, c_info,init_placements); //
			// Compute the variance for that individual
			// i++;
			metric ml = new metric();
			ml = calcFitness(individual, hm, c_info, init_placements);
			marray[i] = new metric(ml.variance, ml.migration);
			individual.setFitness(ml.variance);
			populationFitness += ml.variance;
			i++;
		}
		double vari[] = new double[marray.length];
		double mig[] = new double[marray.length];
		double scaled_vari[] = new double[marray.length];
		double scaled_mig[] = new double[marray.length];

		for (int j = 0; j < marray.length; j++) {
			mig[j] = marray[j].migration;
			vari[j] = marray[j].variance;
		}

		double vmin = minArray(vari);
		double vmax = maxArray(vari);
		double mmin = minArray(mig);
		double mmax = maxArray(mig);
		System.out.println("MIN MAX  vari and migrations " + vmin + " " + vmax + " " + mmin + " " + mmax);
		double vscaleFactor = vmax - vmin;
		double mscaleFactor = mmax - mmin;
		// scaling between [0..1] for starters. Will generalize later.
		for (int x = 0; x < vari.length; x++) {
			scaled_vari[x] = ((vari[x] - vmin) / vscaleFactor);
			scaled_mig[x] = ((mig[x] - mmin) / mscaleFactor);
		}
		i = 0;
		double alpha = 1, beta = 0; // alpha weight factor for variance and beta for migrations
		for (Individual individual : population.getIndividuals()) {
			double score = (beta * scaled_mig[i]) + (alpha * scaled_vari[i]);
			// System.out.println(scaled_mig[i]+" "+mig[i]+" scaled migration and fitness "+
			// scaled_vari[i]+" "+vari[i]+" score "+score);
			// individual.setFitness(score);
			// individual.setFitness(vari[i]);
			// populationFitness += score;
			// populationFitness += vari[i];
			i++;
		}

		population.setPopulationFitness(populationFitness);
	}

	public double minArray(double[] vals) {
		double min = vals[0];
		for (int x = 1; x < vals.length; x++) {
			if (vals[x] < min) {
				min = vals[x];
			}
		}
		return min;
	}

	public double maxArray(double[] vals) {
		double max = vals[0];
		for (int x = 1; x < vals.length; x++) {
			if (vals[x] > max) {
				max = vals[x];
			}
		}
		return max;
	}

	/**
	 * Check if population has met termination condition
	 * 
	 * For this simple problem, we know what a perfect solution looks like, so we
	 * can simply stop evolving once we've reached a fitness of one.
	 * 
	 * @param population
	 * @return boolean True if termination condition met, otherwise, false
	 */
	public boolean isTerminationConditionMet(Population population) {
		for (Individual individual : population.getIndividuals()) {
			if (individual.getFitness() == 1) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Select parent for crossover
	 * 
	 * @param population
	 *            The population to select parent from
	 * @return The individual selected as a parent
	 */
	public Individual selectParent(Population population) {
		// Get individuals
		Individual individuals[] = population.getIndividuals();

		// Spin roulette wheel
		double populationFitness = population.getPopulationFitness();
		double rouletteWheelPosition = Math.random() * populationFitness * 0.05;
		// System.out.println(rouletteWheelPosition+" wheel position");
		// Find parent
		double spinWheel = 0;
		for (Individual individual : individuals) {
			spinWheel += individual.getFitness();
			if (spinWheel >= rouletteWheelPosition) {
				return individual;
			}
		}
		return individuals[population.size() - 1];
	}

	/**
	 * Apply crossover to population
	 * 
	 * Crossover, more colloquially considered "mating", takes the population and
	 * blends individuals to create new offspring. It is hoped that when two
	 * individuals crossover that their offspring will have the strongest qualities
	 * of each of the parents. Of course, it's possible that an offspring will end
	 * up with the weakest qualities of each parent.
	 * 
	 * This method considers both the GeneticAlgorithm instance's crossoverRate and
	 * the elitismCount.
	 * 
	 * The type of crossover we perform depends on the problem domain. We don't want
	 * to create invalid solutions with crossover, so this method will need to be
	 * changed for different types of problems.
	 * 
	 * This particular crossover method selects random genes from each parent.
	 * 
	 * @param population
	 *            The population to apply crossover to
	 * @return The new population
	 */
	public Population crossoverPopulation(Population population, int[] machine_ids,
			HashMap<Integer, ArrayList<ContainerInfo>> hm, ArrayList<ContainerInfo> c_info) {
		// Create new population
		Population newPopulation = new Population(population.size());

		// Loop over current population by fitness
		for (int populationIndex = 0; populationIndex < population.size(); populationIndex++) {
			Individual parent1 = population.getFittest(populationIndex);
			// double ran=Math.random();
			if (populationIndex < this.elitismCount)
				System.out.println("Elite fitness " + parent1.getFitness());
			// Apply crossover to this individual?

			// if (this.crossoverRate >ran && populationIndex >= this.elitismCount) {
			// Initialize offspring
			if (populationIndex >= this.elitismCount) {
				Individual offspring = new Individual(parent1.getChromosomeLength(), machine_ids);

				// Find second parent
				Individual parent2 = selectParent(population);

				// Loop over genome
				for (int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++) {
					// Use half of parent1's genes and half of parent2's genes
					if (Math.random() > this.crossoverRate) {
						offspring.setGene(geneIndex, parent1.getGene(geneIndex));
					} else {
						offspring.setGene(geneIndex, parent2.getGene(geneIndex));
					}
				}

				// Add offspring to new population
				newPopulation.setIndividual(populationIndex, offspring);
				// newPopulation.getIndividual(populationIndex).setFitness(calc_init_variance(offspring,
				// hm, c_info));
				// System.out.println("HERE I COME "+
				// newPopulation.getIndividual(populationIndex).getFitness());
			} else {
				// Add individual to new population without applying crossover
				newPopulation.setIndividual(populationIndex, parent1);
				newPopulation.getIndividual(populationIndex).setFitness(calc_init_variance(parent1, hm, c_info));
				// System.out.println("HERE I COME "+
				// newPopulation.getIndividual(populationIndex).getFitness()+Arrays.toString(parent1.getChromosome()));
			}
		}
		// .out.println("Fittess best new : "+newPopulation.getFittest(0).getFitness());
		/*
		 * System.out.println("new "+Arrays.toString(newPopulation.getIndividual(0).
		 * getChromosome())); Individual individualss[] =
		 * newPopulation.getIndividuals(); for(Individual in: individualss) {
		 * System.out.println("new pop "+Arrays.toString(in.getChromosome())+" .. "+in.
		 * getFitness()); }
		 */
		return newPopulation;
	}

	/**
	 * Apply mutation to population
	 * 
	 * Mutation affects individuals rather than the population. We look at each
	 * individual in the population, and if they're lucky enough (or unlucky, as it
	 * were), apply some randomness to their chromosome. Like crossover, the type of
	 * mutation applied depends on the specific problem we're solving. In this case,
	 * we simply randomly flip 0s to 1s and vice versa.
	 * 
	 * This method will consider the GeneticAlgorithm instance's mutationRate and
	 * elitismCount
	 * 
	 * @param population
	 *            The population to apply mutation to
	 * @return The mutated population
	 */
	public Population mutatePopulation(Population population, int[] machine_ids) {
		// Initialize new population
		Population newPopulation = new Population(this.populationSize);
		// Random ram=new Random();
		// Loop over current population by fitness
		for (int populationIndex = 0; populationIndex < population.size(); populationIndex++) {
			Individual individual = population.getFittest(populationIndex);

			// Loop over individual's genes
			for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
				// Skip mutation if this is an elite individual
				if (populationIndex > this.elitismCount) {
					// Does this gene need mutation?
					if (this.mutationRate > Math.random()) {
						// Get new gene
						Random ram = new Random();
						int x = ram.nextInt(machine_ids.length);
						int y = ram.nextInt(machine_ids.length);
						int xx = individual.getGene(x);
						int yy = individual.getGene(y);
						// int newGene = 1;
						/*
						 * if (individual.getGene(geneIndex) == 1) { newGene = 0; } // Mutate gene
						 * individual.setGene(geneIndex, newGene);
						 */
						individual.setGene(x, yy); // SWAP MUTATION
						individual.setGene(y, xx);
					}
				}
			}

			// Add individual to population
			newPopulation.setIndividual(populationIndex, individual);
		}

		// Return mutated population
		return newPopulation;
	}
}
