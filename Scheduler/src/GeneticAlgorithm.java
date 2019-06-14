
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.management.loading.MLet;

import scala.util.Random;

public class GeneticAlgorithm {
	class metric {
		double variance;
		double migration;

		public double getVariance() {
			return variance;
		}

		public metric(double variance, double migration) {
			super();
			this.variance = variance;
			this.migration = migration;
		}

		public metric() {

		}

		public void setVariance(double variance) {
			this.variance = variance;
		}

		public double getMigration() {
			return migration;
		}

		public void setMigration(int migration) {
			this.migration = migration;
		}
	}

	private int populationSize;

	/**
	 * Mutation rate is the fractional probability than an individual gene will
	 * mutate randomly in a given generation. The range is 0.0-1.0, but is generally
	 * small (on the order of 0.1 or less).
	 */
	private double mutationRate;

	/**
	 * Crossover rate is the fractional probability that two individuals will "mate"
	 * with each other, sharing genetic information, and creating offspring with
	 * traits of each of the parents. Like mutation rate the rance is 0.0-1.0 but
	 * small.
	 */
	private double crossoverRate;

	/**
	 * Elitism is the concept that the strongest members of the population should be
	 * preserved from generation to generation. If an individual is one of the
	 * elite, it will not be mutated or crossover.
	 */
	private int elitismCount;

	public GeneticAlgorithm(int populationSize, double mutationRate, double crossoverRate, int elitismCount) {
		this.populationSize = populationSize;
		this.mutationRate = mutationRate;
		this.crossoverRate = crossoverRate;
		this.elitismCount = elitismCount;
	}

	/**
	 * Initialize population
	 * 
	 * @param chromosomeLength
	 *            The length of the individuals chromosome
	 * @return population The initial population generated
	 */
	public Population initPopulation(int chromosomeLength, int[] machine_ids) {
		// Initialize population
		Population population = new Population(this.populationSize, chromosomeLength, machine_ids);
		return population;
	}

	public double getMean(double parameter[]) {
		double mean = 0;
		for (double p : parameter) {
			mean += p;
		}
		return mean / (double) parameter.length;
	}

	public int numberMigrations(Individual individual, int[] init_placement) {
		int total_migrations = 0;
		// System.out.println();
		// System.out.print("Init placements ");
		/*
		 * for (int i = 0; i < init_placement.length; i++) {
		 * System.out.print(init_placement[i]); }
		 */
		// System.out.print(" new "+individual.toString());
		// System.out.println();

		for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
			if (individual.getGene(geneIndex) != (init_placement[geneIndex])) {
				total_migrations++;
			}
		}
		// System.out.print(" migrations "+total_migrations);
		return total_migrations;
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
