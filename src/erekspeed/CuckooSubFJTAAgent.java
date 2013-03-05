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
public class CuckooSubFJTAAgent extends CuckooSubAgent implements Serializable {
	static final long serialVersionUID = -904284600634352350L;

	private final Agent forwardAgent = new ForwardJumpingAgent();
	private final double faRate = 1;
	private Agent agent = forwardAgent;
	private double agentRate = faRate;
	private boolean wasLeft = false;
	private int timeLeft;
//TODO: always run forward at the edge of a cliff.


	public CuckooSubFJTAAgent(int capacity) {
		super(capacity);
		setName("Cuckoo FJTA Sub Agent");
		useBit = true;
		reset();
	}

	public CuckooSubFJTAAgent() {
		this(0);
	}

	public boolean[] getAction() {
		MapWrapper map = new MapDirTimeWrapper(mergedObservationBit, wasLeft, timeLeft);

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

		return sol;

	}

	public void reset() {
		agent.reset();
		wasLeft = false;
	}

	public void integrateObservation(Environment environment) {
		timeLeft = environment.getMarioState()[10];
		super.integrateObservation(environment);
		agent.integrateObservation(environment);
	}
}