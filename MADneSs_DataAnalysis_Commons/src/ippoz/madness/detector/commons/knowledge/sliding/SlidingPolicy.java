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
public abstract class SlidingPolicy {

	public static SlidingPolicy getPolicy(SlidingPolicyType policyType){
		switch(policyType){
		case FIFO:
			return new FIFOPolicy();
		case FIFO_NORMAL:
			return new FIFONormalPolicy();
		default:
			return null;
		}
		
	}

	public abstract int canReplace(List<WeightedIndex> indexList, WeightedIndex wi);

	public abstract boolean canEnter(WeightedIndex wi);
	
}
