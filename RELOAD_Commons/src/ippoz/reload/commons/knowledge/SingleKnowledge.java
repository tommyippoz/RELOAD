/**
 * 
 */
package ippoz.reload.commons.knowledge;

import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class SingleKnowledge extends Knowledge {

	public SingleKnowledge(MonitoredData baseData) {
		super(baseData);
	}

	@Override
	public KnowledgeType getKnowledgeType() {
		return KnowledgeType.SINGLE;
	}

	@Override
	public List<Snapshot> toArray(DataSeries dataSeries) {
		List<Snapshot> snapArray = new ArrayList<Snapshot>(baseData.size());
		for(int i=0;i<baseData.size();i++){
			snapArray.add(buildSnapshotFor(i, dataSeries));
		}
		return snapArray;
	}

	@Override
	public Knowledge cloneKnowledge() {
		return new SingleKnowledge(baseData);
	}

}
