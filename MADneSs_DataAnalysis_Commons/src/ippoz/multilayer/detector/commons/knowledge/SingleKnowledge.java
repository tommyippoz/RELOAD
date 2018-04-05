/**
 * 
 */
package ippoz.multilayer.detector.commons.knowledge;

import java.util.ArrayList;
import java.util.List;

import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.knowledge.data.MonitoredData;
import ippoz.multilayer.detector.commons.knowledge.snapshot.Snapshot;

/**
 * @author Tommy
 *
 */
public class SingleKnowledge extends Knowledge {

	public SingleKnowledge(MonitoredData baseData) {
		super(baseData);
		// TODO Auto-generated constructor stub
	}

	@Override
	public KnowledgeType getKnowledgeType() {
		return KnowledgeType.SINGLE;
	}

	@Override
	public List<Snapshot> toArray(AlgorithmType algType, DataSeries dataSeries) {
		List<Snapshot> snapArray = new ArrayList<Snapshot>(baseData.size());
		for(int i=0;i<baseData.size();i++){
			snapArray.add(buildSnapshotFor(algType, i, dataSeries));
		}
		return snapArray;
	}

}
