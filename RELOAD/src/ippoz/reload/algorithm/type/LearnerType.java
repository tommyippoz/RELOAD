/**
 * 
 */
package ippoz.reload.algorithm.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.meta.MetaLearnerType;

/**
 * @author Tommy
 *
 */
public abstract class LearnerType implements Comparable<LearnerType> {
	
	protected Map<String, String> learnerPreferences;
	
	public LearnerType() {
		learnerPreferences = new HashMap<>();
	}
	
	public LearnerType(Map<String, String> learnerPreferences) {
		this.learnerPreferences = learnerPreferences;
	}
	
	public String getPreference(String prefString) {
		if(learnerPreferences != null && prefString != null && learnerPreferences.containsKey(prefString.trim()))
			return learnerPreferences.get(prefString.trim());
		else return null;
	}
	
	public boolean hasPreference(String prefString) {
		if(learnerPreferences != null && prefString != null && learnerPreferences.containsKey(prefString.trim()))
			return true;
		else return false;
	}
	
	public void addPreference(String prefString, String prefValue) {
		if(learnerPreferences != null)
			learnerPreferences.put(prefString, prefValue);
	}
	
	public static LearnerType fromString(String learnerString) {
		if(learnerString != null && learnerString.length() > 0){
			try {
				AlgorithmType at = AlgorithmType.valueOf(learnerString);
				return new BaseLearner(at);
			} catch(Exception ex){
				return new MetaLearner(learnerString);
			}
		} else return null;
	}
	
	public static boolean hasLearner(List<LearnerType> list, LearnerType item){
		if(list == null || list.size() == 0)
			return false;
		else {
			for(LearnerType lt : list){
				if(lt.compareTo(item) == 0)
					return true;
			}
			return false;
		}
	}
	
	public abstract LearnerType clone();

}
