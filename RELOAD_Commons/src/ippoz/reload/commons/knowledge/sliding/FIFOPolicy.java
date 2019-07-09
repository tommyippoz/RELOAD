/**
 * 
 */
package ippoz.reload.commons.knowledge.sliding;

import ippoz.reload.commons.support.WeightedIndex;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class FIFOPolicy extends SlidingPolicy {

	@Override
	public int canReplace(List<WeightedIndex> indexList, WeightedIndex wi) {
		return 0;
	}

	@Override
	public boolean canEnter(WeightedIndex wi) {
		return true;
	}

	@Override
	public String toString() {
		return "FIFO";
	}

}
