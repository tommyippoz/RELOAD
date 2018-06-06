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
public class GlobalKnowledge extends Knowledge {

	public GlobalKnowledge(MonitoredData baseData) {
		super(baseData);
	}

	@Override
	public KnowledgeType getKnowledgeType() {
		return KnowledgeType.GLOBAL;
	}

	@Override
	public List<Snapshot> toArray(AlgorithmType algType, DataSeries dataSeries) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Knowledge cloneKnowledge() {
		return new GlobalKnowledge(baseData);
	}

}
