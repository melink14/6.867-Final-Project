package erekspeed;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: espeed
 * Date: Aug 6, 2009
 * Time: 12:02:37 PM
 * My agent for the contest.
 */
public class CuckooSubRandomAgent extends CuckooSubAgent implements Serializable {
	static final long serialVersionUID = -7186766417037671248L;

	public CuckooSubRandomAgent(int capacity) {
		super(capacity);
		setName("Cuckoo Random Sub Agent");
	}

	public CuckooSubRandomAgent() {
		this(0);
	}

	public boolean[] getAction() {

		MapWrapper map = new MapWrapper(mergedObservation);

		if (!solution.containsKey(map)) {

			int ran1 = random.nextInt(actionMap.size());
			solution.put(map, actionMap.get(ran1).acts.clone());
		}


		return solution.get(map);

	}
}