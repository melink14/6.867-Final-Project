/**
 * 
 */
package project6867;

/**
 * @author espeed
 *
 */
public class DataFitness {
	
	private boolean visited = false;
	private float fitness = 0.0f;
	
	public DataFitness(boolean visited, float fitness) {
		this.visited = visited;
		this.fitness = fitness;
	}
	
	public void add(float val) {
		fitness += val;
	}
	
	public float getFitness() {
		return fitness;
	}
	
	public boolean hasVisited() {
		boolean ret = visited;
		if(!visited) visited = true;
		
		return ret;
	}

	@Override
	public String toString() {
		return "DataFitness [visited=" + visited + ", fitness=" + fitness + "]";
	}

}
