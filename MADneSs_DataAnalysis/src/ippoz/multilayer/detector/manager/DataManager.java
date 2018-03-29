/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.multilayer.detector.commons.knowledge.GlobalKnowledge;
import ippoz.multilayer.detector.commons.knowledge.Knowledge;
import ippoz.multilayer.detector.commons.knowledge.KnowledgeType;
import ippoz.multilayer.detector.commons.knowledge.SingleKnowledge;
import ippoz.multilayer.detector.commons.knowledge.SlidingKnowledge;
import ippoz.multilayer.detector.commons.knowledge.data.MonitoredData;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.ThreadScheduler;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	/** The indicators targeted in the experiments. */
	private Indicator[] indicators;
	
	public DataManager(Indicator[] indicators, List<MonitoredData> expList) {
		super();
		this.indicators = indicators;
		kMap = generateKnowledge(expList);
		AppLogger.logInfo(getClass(), expList.size() + " runs loaded");
	}
	
	private Map<KnowledgeType, List<Knowledge>> generateKnowledge(List<MonitoredData> expList) {
		Map<KnowledgeType, List<Knowledge>> map = new HashMap<KnowledgeType, List<Knowledge>>();
		map.put(KnowledgeType.GLOBAL, new ArrayList<Knowledge>(expList.size()));
		map.put(KnowledgeType.SLIDING, new ArrayList<Knowledge>(expList.size()));
		map.put(KnowledgeType.SINGLE, new ArrayList<Knowledge>(expList.size()));
		for(int i=0;i<expList.size();i++){
			map.get(KnowledgeType.GLOBAL).add(new GlobalKnowledge(expList.get(i)));
			map.get(KnowledgeType.SLIDING).add(new SlidingKnowledge(expList.get(i)));
			map.get(KnowledgeType.SINGLE).add(new SingleKnowledge(expList.get(i)));
		}
		return map;
	}
	
	public int experimentsSize(){
		return kMap.get(KnowledgeType.GLOBAL).size();
	}
	
	public Indicator[] getIndicators() {
		return indicators;
	}
	
	public List<Knowledge> getKnowledge(KnowledgeType kType) {
		return kMap.get(kType);
	}
	
	public Set<KnowledgeType> getKnowledgeTypes(){
		return kMap.keySet();
	}
	
}
