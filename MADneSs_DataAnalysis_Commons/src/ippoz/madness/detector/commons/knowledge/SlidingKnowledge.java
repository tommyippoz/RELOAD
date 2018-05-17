/**
 * 
 */
package ippoz.madness.detector.commons.knowledge;

import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.data.MonitoredData;
import ippoz.madness.detector.commons.knowledge.sliding.SlidingPolicy;
import ippoz.madness.detector.commons.knowledge.sliding.SlidingPolicyType;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.support.WeightedIndex;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class SlidingKnowledge extends Knowledge {
	
	private SlidingPolicy sPolicy;
	
	private List<WeightedIndex> indexList;
	
	private int windowSize;

	public SlidingKnowledge(MonitoredData baseData, SlidingPolicyType policyType, int windowSize) {
		super(baseData);
		this.windowSize = windowSize;
		sPolicy = SlidingPolicy.getPolicy(policyType);
		reset();
	}
	
	private SlidingKnowledge(MonitoredData baseData, SlidingPolicy policy, int windowSize) {
		super(baseData);
		this.windowSize = windowSize;
		sPolicy = policy;
		reset();
	}

	@Override
	public KnowledgeType getKnowledgeType() {
		return KnowledgeType.SLIDING;
	}

	@Override
	public List<Snapshot> toArray(AlgorithmType algType, DataSeries dataSeries) {
		List<Integer> indexes = new LinkedList<Integer>();
		for(WeightedIndex wi : indexList){
			indexes.add(wi.getIndex());
		}
		return buildSnapshotsFor(algType, dataSeries, indexes);
	}

	public void slide(int index, double anomalyScore) {
		int replaceIndex;
		WeightedIndex wi = new WeightedIndex(index, anomalyScore);
		if(sPolicy.canEnter(wi)){
			if(indexList.size() < windowSize){
				indexList.add(wi);
			} else {
				replaceIndex = sPolicy.canReplace(indexList, wi);
				if(replaceIndex >= 0){
					indexList.remove(replaceIndex);
					indexList.add(wi);
				}
			}
		}	
		
	}

	public void reset() {
		if(windowSize > 0)
			indexList = new LinkedList<WeightedIndex>();
		else indexList = null;
	}

	@Override
	public Knowledge cloneKnowledge() {
		return new SlidingKnowledge(baseData, sPolicy, windowSize);
	}

	public int getWindowSize() {
		return windowSize;
	}

}
