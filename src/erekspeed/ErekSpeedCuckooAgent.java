package erekspeed;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;

import ch.idsia.agents.Agent;
import ch.idsia.agents.LearningAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.MarioAIOptions;

/**
 * Created by IntelliJ IDEA.
 * User: espeed
 * Date: Jul 8, 2010
 * Time: 2:39:47 PM
 * Purpose: A class which implements a cuckoo search with Levy flights to
 * evolve a mario agent.
 * The algorithm is presented in "Cuckoo Search with Levy Flights" 2009
 */
public class ErekSpeedCuckooAgent implements LearningAgent {
	private String name;
	private final boolean DEBUG = false;

	// A unique id which makes saving files less dangerous.
	private final int id;

	private long maxEvals;
	private long curEvals;


	private Random random;

	// Probability worst nest is removed
	private final double pa = .25;
	// population size/number of nests
	private final int n = 20;


	private ArrayList<CuckooSubAgent> nests;

	private int genCount = 0;

	private MarioAIOptions cmdLineOptions;
	private LearningTask lTask;

	/**
	 * The major Agent has a population of SubAgentS.  This lets me select
	 * the sub agent on the fly.
	 */
	public static SubAgent AGENT = SubAgent.FBJTA;

	/**
	 * Default Constructor.
	 */
	public ErekSpeedCuckooAgent() {
		setName("Cuckoo Agent");
		random = new Random();
		id = random.nextInt(10000);

	}

	/**
	 * Constructor
	 * @param cmdOpt allows the agent to have specific data about the simulation
	 * so that it can append said data to saved files.
	 */
	public ErekSpeedCuckooAgent(MarioAIOptions cmdOpt) {
		this();
		cmdLineOptions = cmdOpt;

	}

	/**
	 * Sets cmdLineOptions to options
	 * @param options command line options for the current run.
	 */
	public void setCmdLineOptions(MarioAIOptions options) {
		cmdLineOptions = options;
	}


	/**
	 * When called, it interrupts the learning cycle to showcase the current
	 * best agent.
	 */
	public void doYourBest() {
	}





	/**
	 * Initializes Agent to a clean population set for a new run.
	 */
	public void init() {
		nests = new ArrayList<CuckooSubAgent>(n);
		for (int i = 0; i < n; ++i) {
			CuckooSubAgent nest = CuckooSubAgent.getNewAgent();
			int fitness = lTask.evaluate(nest);
			curEvals++;
			nest.setFitness(fitness);
			nests.add(CuckooSubAgent.getNewAgent());
		}

		//state = State.INIT;
		genCount = 0;
	}


	public void learn() {

		// This value sets the number of bad nests to replace.
		int repN = 1;//(int)(n*pa);

		// We generate a new egg during the first part of the algorithm
		// Do some bookkeeping and use levy flights to make a new agent.
		while(curEvals < maxEvals) {
			genCount++;
			if ((genCount % 100) == 0) {
				System.out.println("Generation:" + genCount);
				System.out.println("best:" + nests.get(n - 1).getFitness());
			}
			
			// Collections.sort(nests, new FitnessComparator());
			// Review: try using the best instead of random
			CuckooSubAgent baseAgent = nests.get(random.nextInt(n));
			CuckooSubAgent newAgent = levyFlight(baseAgent, 10.0);

			int fitness = lTask.evaluate(newAgent);
			curEvals++;
			newAgent.setFitness(fitness);

			int jIndex = random.nextInt(n);
			CuckooSubAgent jNest = nests.get(jIndex);
			if (newAgent.getFitness() > jNest.getFitness()) {
				nests.remove(jIndex);
				nests.add(newAgent);
			}
			// Review: better variable name
			// With probability pa we will abandon our worst nest with new nests
			if (random.nextDouble() < pa) {

				ArrayList<CuckooSubAgent> replacementNests = new ArrayList<CuckooSubAgent>(repN);
				Collections.sort(nests, new FitnessComparator());

				for (int i = 0; i < repN; ++i) {
					// get variations on the worst nests
					replacementNests.add(levyFlight(nests.get(i), 1.0));
				}
				for(CuckooSubAgent nest : replacementNests) {
					fitness = lTask.evaluate(nest);
					curEvals++;
					nest.setFitness(fitness);
				}
				Collections.sort(nests, new FitnessComparator());
				nests.subList(0, repN).clear();
				nests.addAll(replacementNests);
			} 

			Collections.sort(nests, new FitnessComparator());

		}		
	}




	public Agent getBestAgent() {
		CuckooSubAgent nest = Collections.max(nests, new FitnessComparator());
		saveNest(nest);
		return nest;
	}

	private void saveNest(CuckooSubAgent nest) {
		saveNest(nest, "def");
	}

	private void saveNest(CuckooSubAgent nest, String tag) {
		try {
			FileOutputStream os = new FileOutputStream("best"
					+ "_" + tag + "_"
					+ nest.getFitness() +
					"_" + id +
					"_" + cmdLineOptions.getLevelDifficulty() +
					"_" + cmdLineOptions.getLevelRandSeed() +
					"_lhb-" + cmdLineOptions.getHiddenBlocksCount() +
					"_lt-" + cmdLineOptions.getLevelType() +
					"_" + System.currentTimeMillis() / 100000);

			ObjectOutputStream out = new ObjectOutputStream(os);
			out.writeObject(nest);
			out.close();
			System.out.println("Finished writing");

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private CuckooSubAgent levyFlight(CuckooSubAgent agent, double stepSize) {
		CuckooSubAgent newAgent = CuckooSubAgent.getNewAgent(agent.solution.size());

		// Get a set rate based on the levy distribution.
		double rate = levy(stepSize / agent.solution.size(), 1.001);
		if (DEBUG) {
			System.out.println("size:" + agent.solution.size());
			System.out.println("levy rate:" + rate);
		}
		Set<MapWrapper> keys = agent.solution.keySet();

		for (MapWrapper key : keys) {
			double roll = random.nextDouble();

			if (roll < rate) {
				newAgent.solution.put(key,
						newAgent.actionMap.get(random.nextInt(newAgent.actionMap.size())).acts.clone());
			} else {
				newAgent.solution.put(key, agent.solution.get(key));
			}
		}

		return newAgent;
	}
	/*    private CuckooSubAgent levyFlight(CuckooSubAgent agent, double stepSize) {
        CuckooSubAgent newAgent = CuckooSubAgent.getNewAgent(agent.solution.size());

        // Get a set rate based on the levy distribution.
        double rate = levy(stepSize/agent.solution.size(), 1.001);
        if(DEBUG) {
            System.out.println("size:" + agent.solution.size());
            System.out.println("levy rate:" + rate);
        }

        rate = (rate > 1.0 ? 1.0:rate);
        int mutN = (int) rate*agent.solution.size();

        for(int i = 0; i < agent.sequentialActions.size(); ++i)
        {
            int target = agent.sequentialActions.size()-mutN;
            MapWrapper key = agent.sequentialActions.get(i);
            if(i < target)
            {
                newAgent.solution.put(key, agent.solution.get(key));
            }
            else
            {
                 newAgent.solution.put(key,
                        newAgent.actionMap.get(random.nextInt(newAgent.actionMap.size())).acts.clone());
            }
        }

        return newAgent;
    }*/

	private double levy(double xmin, double alpha) {
		if (DEBUG) {
			System.out.println("xmin:" + xmin);
		}
		double ur = random.nextDouble();

		return xmin / Math.pow(ur, 1 / alpha);


	}

	public boolean[] getAction() {
		return new boolean[6];
	}

	public void integrateObservation(Environment environment) {
//		try {
//			currentAgent.integrateObservation(environment);
//			byte[][] mergedObs = currentAgent.mergedObservation;
//			if (mergedObs[12][11] == -10) {
//				blocked++;
//				//System.out.println("blocked:" + blocked);
//			} else
//				blocked = 0;
//			//            if(blocked > 100)
//			//                ((MarioEnvironment)environment).killMario();
//
//		}
//		catch (Exception ex) {
//			//Collections.sort(nests, new FitnessComparator());
//			//saveNest(nests.get(n-1));
//
//			System.err.println(ex.toString());
//			//System.exit(1);
//		}
	}

	public void giveIntermediateReward(float intermediateReward) {
		//TODO: I have no idea what should go here.
	}

	public void reset() {
	}
	
	@Override
	public void setLearningTask(LearningTask learningTask) {
		lTask = learningTask;

	}

	@Override
	public void setEvaluationQuota(long num) {
		maxEvals = num;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	enum State {
		INIT, NEWEGG, ABANDON, SACRIFICE_RUN
	}

	enum SubAgent {
		RANDOM, FJA, FBJA, FBJTA
	}

	// Sorts worst to best

	private class FitnessComparator implements Comparator<CuckooSubAgent> {
		public int compare(CuckooSubAgent o1, CuckooSubAgent o2) {
			return (int) (o1.getFitness() - o2.getFitness());
		}
	}

	@Override
	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow,
			int egoCol) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Called when a trial is about to begin.
	 */
	public void newEpisode() {
		// This is never called right now.
		reset();
	}


	// This is also never called
	public void giveReward(float reward) {

	}


}
