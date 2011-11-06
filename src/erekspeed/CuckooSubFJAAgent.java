package erekspeed;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.environments.Environment;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: espeed
 * Date: Aug 6, 2009
 * Time: 12:02:37 PM
 * My agent for the contest.
 */
public class CuckooSubFJAAgent extends CuckooSubAgent implements Serializable {
	static final long serialVersionUID = -90428460063452350L;

	private final Agent agent = new ForwardJumpingAgent();
	private final double agentRate = .8;

	public CuckooSubFJAAgent(int capacity) {
		super(capacity);
		setName("Cuckoo FJA Sub Agent");
		reset();
	}

	public CuckooSubFJAAgent() {
		this(0);
	}

	public boolean[] getAction() {

		MapWrapper map = new MapWrapper(mergedObservation);

		if (!solution.containsKey(map)) {

			if (random.nextDouble() < agentRate) {
				solution.put(map, agent.getAction().clone());
			} else {
				int ran1 = random.nextInt(actionMap.size());
				solution.put(map, actionMap.get(ran1).acts.clone());
			}
		}


		return solution.get(map);

	}

	public void reset() {
		agent.reset();
	}

	public void integrateObservation(Environment environment) {
		super.integrateObservation(environment);
		agent.integrateObservation(environment);
	}
}