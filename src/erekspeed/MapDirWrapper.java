package erekspeed;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: espeed
 * Date: Jul 11, 2010
 * Time: 2:38:44 PM
 * Map Wrapper which contains Direction as well
 */
public class MapDirWrapper extends MapWrapper implements Serializable {

	private final boolean left;

	public MapDirWrapper(byte[][] map, boolean left) {
		super(map);
		cachedHash += (left ? 27017 : 48299);
		this.left = left;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		if (!super.equals(o)) {
			return false;
		}

		MapDirWrapper other = (MapDirWrapper) o;

		return left == other.left;
	}

//    protected void generateHash() {
//        cachedHash += (left?27017:48299);
//        System.out.println(cachedHash);
//    }
}
