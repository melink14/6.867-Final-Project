package project6867;

import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.AbstractSimilarity;
import net.sf.javaml.distance.DistanceMeasure;


/**
 * http://en.wikipedia.org/wiki/Jaccard_index
 * 
 * @author espeed
 *
 */
public class JaccardIndex extends AbstractSimilarity {

	@Override
	public double measure(Instance x, Instance y) {
		int m11 = 0;   // both have 1
		int m01 = 0;   // x has 0 y has 1
		int m10 = 0;   // x has 1 y has 0
		
		for(int i = 0; i < x.noAttributes(); ++i) {
			if(x.value(i) == 1) {
				if(y.value(i) == 1) {
					++m11;
					continue;
				}
				else {
					++m10;
					continue;
				}
			}
			else {
				if(y.value(i) == 1) {
					++m01;
				}
			}
		}
		
		return m11/(double)(m01+m10+m11);
	}

}
