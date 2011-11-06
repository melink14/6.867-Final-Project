package erekspeed;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: espeed
 * Date: Aug 6, 2009
 * Time: 12:02:37 PM
 * My agent for the contest.
 */
public class CuckooSubFBJAAgent extends CuckooSubAgent implements Serializable {
	static final long serialVersionUID = -90428460063452350L;

	private final Agent forwardAgent = new ForwardJumpingAgent();
	private final Agent backwardAgent = new BackwardJumpingAgent();
	private final double faRate = .8;
	private final double baRate = .7;
	private Agent agent = forwardAgent;
	private double agentRate = faRate;
	private boolean wasLeft = false;

	public CuckooSubFBJAAgent(int capacity) {
		super(capacity);
		setName("Cuckoo FJA Sub Agent");
		reset();
	}

	public CuckooSubFBJAAgent() {
		this(0);
	}

	public boolean[] getAction() {
		MapWrapper map = new MapDirWrapper(mergedObservation, wasLeft);

		if (!solution.containsKey(map)) {

			if (random.nextDouble() < agentRate) {
				solution.put(map, agent.getAction().clone());
			} else {
				int ran1 = random.nextInt(actionMap.size());
				solution.put(map, actionMap.get(ran1).acts.clone());
			}
		}

		boolean[] sol = solution.get(map);
		wasLeft = sol[Mario.KEY_LEFT];

		// Set agent now so integrateObservation is correct for next time.
		agent = wasLeft ? backwardAgent : forwardAgent;
		agentRate = wasLeft ? baRate : faRate;

		return sol;

	}

	public void reset() {
		agent.reset();
		wasLeft = false;
	}

	public void integrateObservation(Environment environment) {
		super.integrateObservation(environment);
		agent.integrateObservation(environment);
	}
}