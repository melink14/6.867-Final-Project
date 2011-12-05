package erekspeed;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

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
		intMap.put(act, ActionWrapper.intFromBooleanArray(act.acts));
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

	@Override
	public String toString() {
		return Arrays.toString(acts);
	}

	public static ActionWrapper parseActionWrapper(String string) {
		StringTokenizer tokens = new StringTokenizer(string, "[ ,]");
		int max = tokens.countTokens();
		boolean[] acts = new boolean[max];
		String token;
		for(int i = 0; i < max; ++i){
			token = tokens.nextToken();
			acts[i] = Boolean.parseBoolean(token);
		}
		ActionWrapper aw = new ActionWrapper(acts);
		return aw;
	}
	
	/**
	 * Only for ActionWrapperS size 6
	 * @param val
	 * @return
	 */
	public static ActionWrapper parseActionWrapper(int val) {
		boolean[] ret = new boolean[6];
		int mask = 1;
		int buf;
		int i = 5;
		while(i >= 0) {
			buf = val&mask;
			ret[i--] = (buf == 1);
			val >>= 1;
		}		
		return new ActionWrapper(ret);
	}
	
	public static int intFromBooleanArray(boolean[] array) {
	    return new BigInteger(Arrays.toString(array)
	                          .replace("true", "1")
	                          .replace("false", "0")
	                          .replaceAll("[^01]", ""), 2).intValue();
	}
}
