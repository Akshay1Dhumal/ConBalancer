

import java.util.Arrays;
import java.util.Random;

public class Individual {
	private int[] chromosome;
	private double fitness =Double.MAX_VALUE;

	/**
	 * Initializes individual with specific chromosome
	 * 
	 * @param chromosome
	 *            The chromosome to give individual
	 */
	public Individual(int[] chromosome) {
		// Create individual chromosome
		this.chromosome = chromosome;
	}
	 int binarySearch(double arr[], int l, int r, double x)
	    {
	        if (r>=l)
	        {
	            int mid = l + (r - l)/2;
	 
	            // If the element is present at the 
	            // middle itself
	            if (arr[mid] == x)
	               return mid;
	 
	            // If element is smaller than mid, then 
	            // it can only be present in left subarray
	            if (arr[mid] > x)
	               return binarySearch(arr, l, mid-1, x);
	 
	            // Else the element can only be present
	            // in right subarray
	            return binarySearch(arr, mid+1, r, x);
	        }
	 
	        // We reach here when element is not present
	        //  in array
	        return -1;
	    }
	 
	/**
	 * Initializes random individual.
	 * 
	 * This constructor assumes that the chromosome is made entirely of 0s and
	 * 1s, which may not always be the case, so make sure to modify as
	 * necessary. This constructor also assumes that a "random" chromosome means
	 * simply picking random zeroes and ones, which also may not be the case
	 * (for instance, in a traveling salesman problem, this would be an invalid
	 * solution).
	 * 
	 * @param chromosomeLength
	 *            The length of the individuals chromosome
	 */

	public Individual(int chromosomeLength,int[] machine_ids)  {
		int i;
		double rand_arr[]=new double[machine_ids.length+1];
		this.chromosome = new int[chromosomeLength];
		Random ran=new Random();		
		//System.out.println(Arrays.toString(chromosome));
		for (int gene = 0; gene < chromosomeLength; gene++) {
	
		//double random= Math.random();
		
		/*for(i=0;i<machine_ids.length;i++)
		{
			rand_arr[i]=((double)i/(double)machine_ids.length);
			//System.out.println("Rand "+rand_arr[i]);
		}
		rand_arr[i]=1;
		for(i=0;i<machine_ids.length;i++)
		{
			if(random>rand_arr[i] && random<=rand_arr[i+1]) {
				this.setGene(gene, i);
				//System.out.println( " Random "+random+" "+i);
				break;
			}
		}*/
		
		int s=ran.nextInt(machine_ids.length);
		//System.out.println(s+" ........... "+machine_ids.length);
		this.setGene(gene,s);
			
			
		/*	if (0.5 < Math.random()) {
				this.setGene(gene, 1);
			} else {
				this.setGene(gene, 0);
			}*/
		}

	}

	/**
	 * Gets individual's chromosome
	 * 
	 * @return The individual's chromosome
	 */
	public int[] getChromosome() {
		return this.chromosome;
	}

	/**
	 * Gets individual's chromosome length
	 * 
	 * @return The individual's chromosome length
	 */
	public int getChromosomeLength() {
		return this.chromosome.length;
	}

	/**
	 * Set gene at offset
	 * 
	 * @param gene
	 * @param offset
	 * @return gene
	 */
	public void setGene(int offset, int gene) {
		this.chromosome[offset] = gene;
	}

	/**
	 * Get gene at offset
	 * 
	 * @param offset
	 * @return gene
	 */
	public int getGene(int offset) {
		return this.chromosome[offset];
	}

	/**
	 * Store individual's fitness
	 * 
	 * @param fitness
	 *            The individuals fitness
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	/**
	 * Gets individual's fitness
	 * 
	 * @return The individual's fitness
	 */
	public double getFitness() {
		return this.fitness;
	}
	public int[] getIntegerRepresentation() {
		int output[]=new int[chromosome.length];
		for (int gene = 0; gene < this.chromosome.length; gene++) {
			output[gene]= this.chromosome[gene];
		}
		return output;
	}
	
	/**
	 * Display the chromosome as a string.
	 * 
	 * @return string representation of the chromosome
	 */
	public String toString() {
		String output = "";
		for (int gene = 0; gene < this.chromosome.length; gene++) {
			output += this.chromosome[gene];
		}
		return output;
	}
}
