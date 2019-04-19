/**
 * 
 */
package ippoz.reload.commons.knowledge;

import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;

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
	public List<Snapshot> toArray(DataSeries dataSeries) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Knowledge cloneKnowledge() {
		return new GlobalKnowledge(baseData);
	}

}
