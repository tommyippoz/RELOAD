/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.ThreadScheduler;

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
		if(isValidKnowledge())
			AppLogger.logInfo(getClass(), "Instances Loaded with " + getInjectionsRatio() + "% of Faults/Attacks");
	}      
	
	public double getInjectionsRatio(){
		int injSum = 0;
		int itemSum = 0;
		for(Knowledge k : kMap.get(kMap.keySet().iterator().next())){
			injSum = injSum + k.getInjectionCount();
			itemSum = itemSum + k.size();
			AppLogger.logInfo(getClass(), "'" + k.getTag() + "' has " + (100.0*k.getInjectionCount()/k.size()) + "% of Faults/Attack Ratio");
		}
		return 100.0 * injSum / itemSum;
	}
	
	public int experimentsSize(){
		return kMap.get(kMap.keySet().iterator().next()).size();
	}
	
	public boolean isValidKnowledge(){
		return kMap != null && !kMap.isEmpty() && getKnowledge(kMap.keySet().iterator().next()) != null && !getKnowledge(kMap.keySet().iterator().next()).isEmpty();
	}
	
	public List<Knowledge> getKnowledge() {
		return getKnowledge(kMap.keySet().iterator().next());
	}
	
	public List<Knowledge> getKnowledge(KnowledgeType kType) {
		return kMap.get(kType);
	}
	
	public Set<KnowledgeType> getKnowledgeTypes(){
		return kMap.keySet();
	}
	
}
