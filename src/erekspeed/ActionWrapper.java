package erekspeed;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

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
	
	public int getInt() {
		return ActionWrapper.intValue(acts);
	}
	
	
	/**
	 * Taken from http://stackoverflow.com/questions/1528204/casting-a-boolean-array-in-java
	 * @param array a boolean array to convert to an int.
	 * @return int value of this binary array
	 */
	static int intValue(boolean[] array) {
		return new BigInteger(Arrays.toString(array)
				.replace("true", "1")
				.replace("false", "0")
				.replaceAll("[^01]", ""), 2).intValue();
	}
}
