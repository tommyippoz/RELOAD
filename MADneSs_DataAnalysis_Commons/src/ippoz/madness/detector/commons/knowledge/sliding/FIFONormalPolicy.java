/**
 * 
 */
package ippoz.madness.detector.commons.knowledge.sliding;

import ippoz.madness.detector.commons.support.WeightedIndex;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class FIFONormalPolicy extends SlidingPolicy {

	@Override
	public int canReplace(List<WeightedIndex> indexList, WeightedIndex wi) {
		return 0;
	}

	@Override
	public boolean canEnter(WeightedIndex wi) {
		return wi.getWeigth() <= 0.0;
	}
	
	@Override
	public String toString() {
		return "FIFONormal";
	}

}
