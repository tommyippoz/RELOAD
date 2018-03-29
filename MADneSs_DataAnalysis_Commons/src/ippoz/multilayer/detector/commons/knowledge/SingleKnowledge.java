/**
 * 
 */
package ippoz.multilayer.detector.commons.knowledge;

import java.util.List;

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
	public List<Snapshot> toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KnowledgeType getKnowledgeType() {
		return KnowledgeType.SINGLE;
	}

}
