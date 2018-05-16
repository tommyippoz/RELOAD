/**
 * 
 */
package ippoz.madness.detector.manager;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.KnowledgeType;
import ippoz.madness.detector.commons.support.ThreadScheduler;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tommy
 *
 */
public abstract class DataManager extends ThreadScheduler {

	/** The experiments list. */
	private Map<KnowledgeType, List<Knowledge>> kMap;
	
	public DataManager(Map<KnowledgeType, List<Knowledge>> map) {
		super();
		kMap = map;
	}
	
	public int experimentsSize(){
		return kMap.get(KnowledgeType.GLOBAL).size();
	}
	
	public Indicator[] getIndicators() {
		List<Knowledge> kList;
		if(kMap.size() > 0){
			kList = kMap.get(kMap.keySet().iterator().next());
			if(kList.size() > 0){
				return kList.get(0).getIndicators();
			} else return null;
		} else return null;

	}
	
	public List<Knowledge> getKnowledge(KnowledgeType kType) {
		return kMap.get(kType);
	}
	
	public Set<KnowledgeType> getKnowledgeTypes(){
		return kMap.keySet();
	}
	
}
