package erekspeed;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;

/**
 * User: espeed
 * Date: Aug 16, 2009
 * Time: 1:56:04 AM
 * Holds an boolean array for actions
 */
public class ActionWrapper implements Serializable {
	static final long serialVersionUID = 567011624892982938L;
	public boolean[] acts;
	private int cachedHash = 0;
	static public int count = 0;

	public ActionWrapper(int size) {
		acts = new boolean[size];
	}
	
	public ActionWrapper(boolean[] m_acts) {
		acts = m_acts;
		generateHashCode();
	}

	public void add(int idx, boolean val) {
		acts[idx] = val;
	}

	final public ActionWrapper clone() {
		ActionWrapper ret = new ActionWrapper(acts.length);
		System.arraycopy(acts, 0, ret.acts, 0, acts.length);
		return ret;
	}
	
	/**
	 * Converts an ActionWrapper (boolean array) to an integer value.
	 * ActionWrapper's int values are assigned a first come first served basis
	 * @return an integer mapping to this ActionWrapper
	 */
	public int getInt() {
		Integer ret = intMap.get(this);
		if(ret == null) {
			addToHash(this);
		}
		
		return intMap.get(this);
	}
	
	
	private static HashMap<ActionWrapper, Integer> intMap= new HashMap<ActionWrapper, Integer>();
	
	
	/**
	 * When creating the set of ActionWrappers, add them to the internal hash to allow 
	 * converting to intS later.
	 * @param act
	 * @param val
	 */
	public static void addToHash(ActionWrapper act) {
		act.generateHashCode();
		intMap.put(act, count++);
	}
	
	private void generateHashCode() {
		cachedHash  =  Arrays.hashCode(acts);
	}
	
	public int hashCode() {
		return cachedHash;
	}
	
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;
		
		ActionWrapper other = (ActionWrapper)o;
		
		return cachedHash == other.cachedHash;
	}
}
