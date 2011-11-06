package erekspeed;

import java.io.Serializable;

/**
 * User: espeed
 * Date: Aug 16, 2009
 * Time: 1:56:04 AM
 * Holds an boolean array for actions
 */
public class ActionWrapper implements Serializable {
	static final long serialVersionUID = 567011624892982938L;
	public boolean[] acts;

	public ActionWrapper(int size) {
		acts = new boolean[size];
	}

	public void add(int idx, boolean val) {
		acts[idx] = val;
	}

	final public ActionWrapper clone() {
		ActionWrapper ret = new ActionWrapper(acts.length);
		System.arraycopy(acts, 0, ret.acts, 0, acts.length);
		return ret;
	}
}
