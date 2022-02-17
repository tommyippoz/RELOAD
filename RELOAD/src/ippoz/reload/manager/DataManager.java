/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.ThreadScheduler;

import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class DataManager extends ThreadScheduler {

	/** The experiments list. */
	private List<Knowledge> kList;
	
	public DataManager(List<Knowledge> kList) {
		super();
		this.kList = kList;
		if(isValidKnowledge()){
			AppLogger.logInfo(getClass(), "Instances Loaded with " + getInjectionsRatio() + "% of Faults/Attacks");
			if(getInjectionsRatio() <= 0)
				AppLogger.logError(getClass(), "NoAttacksInSetError", "Portion of the Dataset you choose does not contain attacks.");
		}
	}      
	
	public double getInjectionsRatio(){
		int injSum = 0;
		int itemSum = 0;
		for(Knowledge k : kList){
			injSum = injSum + k.getInjectionCount();
			itemSum = itemSum + k.size();
		}
		return 100.0 * injSum / itemSum;
	}
	
	public int experimentsSize(){
		return kList.size();
	}
	
	public boolean isValidKnowledge(){
		return kList != null && !kList.isEmpty() && kList.get(0) != null;
	}
	
	public List<Knowledge> getKnowledge() {
		return kList;
	}
	
}
