/**
 * 
 */
package ippoz.madness.detector.commons.knowledge;

import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.data.MonitoredData;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class SlidingKnowledge extends Knowledge {

	public SlidingKnowledge(MonitoredData baseData) {
		super(baseData);
		// TODO Auto-generated constructor stub
	}

	@Override
	public KnowledgeType getKnowledgeType() {
		return KnowledgeType.SLIDING;
	}

	@Override
	public List<Snapshot> toArray(AlgorithmType algType, DataSeries dataSeries) {
		// TODO Auto-generated method stub
		return null;
	}

}
