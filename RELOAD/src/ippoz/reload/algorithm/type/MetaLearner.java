/**
 * 
 */
package ippoz.reload.algorithm.type;

import ippoz.reload.algorithm.meta.BaggingMetaLearner;
import ippoz.reload.algorithm.meta.StackingMetaLearner;
import ippoz.reload.algorithm.meta.VotingMetaLearner;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.meta.MetaLearnerType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class MetaLearner extends LearnerType {
	
	private MetaLearnerType mlType;
	
	private BaseLearner[] atList;
	
	public MetaLearner(MetaLearnerType mlType, BaseLearner[] atList) {
		super();
		this.mlType = mlType;
		this.atList = atList;
		setDefaultMetaFreferences();
	}

	private void setDefaultMetaFreferences() {
		switch(mlType){
			case ARBITRATING:
				break;
			case BAGGING:
				addPreference(BaggingMetaLearner.N_SAMPLES, String.valueOf(BaggingMetaLearner.DEFAULT_SAMPLES));
				break;
			case BOOSTING:
				break;
			case CASCADE_GENERALIZATION:
				break;
			case CASCADING:
				break;
			case DELEGATING:
				break;
			case STACKING:
			case STACKING_FULL:
				if(atList != null)
					addPreference(StackingMetaLearner.BASE_LEARNERS, Arrays.toString(atList).replace("[", "").replace("]", ""));
				break;
			case VOTING:
				if(atList != null)
					addPreference(VotingMetaLearner.BASE_LEARNERS, Arrays.toString(atList).replace("[", "").replace("]", ""));
				break;
		}
	}

	public MetaLearner(MetaLearnerType mlType, BaseLearner[] atList, Map<String, String> learnerPreferences) {
		super(learnerPreferences);
		this.mlType = mlType;
		this.atList = atList;
	}
	
	public MetaLearner(String mlString){
		if(mlString != null && mlString.trim().length() > 0){
			mlString = mlString.trim();
			if(mlString.contains("(") && mlString.contains(")")){
				mlType = MetaLearnerType.valueOf(mlString.substring(0, mlString.indexOf("(")).trim());
				String toDecode = mlString.substring(mlString.indexOf("(")+1, mlString.length()-1).trim();
				switch(mlType){
					case ARBITRATING:
						break;
					case BAGGING:
						if(toDecode.contains(","))
							addPreference(BaggingMetaLearner.N_SAMPLES, toDecode.split(",")[1].trim());
						else addPreference(BaggingMetaLearner.N_SAMPLES, String.valueOf(BaggingMetaLearner.DEFAULT_SAMPLES));	 
						try {
							atList = new BaseLearner[]{new BaseLearner(AlgorithmType.valueOf(toDecode.split(",")[0].trim()))};
						} catch(Exception ex){
							AppLogger.logInfo(getClass(), "Unable to decode '" + mlString + "' learner");
						}
						break;
					case BOOSTING:
						break;
					case CASCADE_GENERALIZATION:
						break;
					case CASCADING:
						break;
					case DELEGATING:
						break;
					case STACKING:
					case STACKING_FULL:
						if(toDecode.contains("@")){
							addPreference(StackingMetaLearner.STACKING_LEARNER, toDecode.split("@")[1].trim());
							toDecode = toDecode.split("@")[0].trim();
						} else addPreference(StackingMetaLearner.STACKING_LEARNER, StackingMetaLearner.DEFAULT_META_LEARNER.toCompactString());
						addPreference(StackingMetaLearner.BASE_LEARNERS, toDecode.trim());
						try {
							List<BaseLearner> lList = new LinkedList<>();
							for(String item : toDecode.split(",")){
								 lList.add(new BaseLearner(AlgorithmType.valueOf(item.trim())));
							}
							atList = lList.toArray(new BaseLearner[lList.size()]);
						} catch(Exception ex){
							AppLogger.logInfo(getClass(), "Unable to decode '" + mlString + "' learner");
						}
						break;
					case VOTING:
						addPreference(VotingMetaLearner.BASE_LEARNERS, toDecode.trim());
						try {
							List<BaseLearner> lList = new LinkedList<>();
							for(String item : toDecode.split(",")){
								 lList.add(new BaseLearner(AlgorithmType.valueOf(item.trim())));
							}
							atList = lList.toArray(new BaseLearner[lList.size()]);
						} catch(Exception ex){
							AppLogger.logInfo(getClass(), "Unable to decode '" + mlString + "' learner");
						}
						break;
					default:
						AppLogger.logInfo(getClass(), "Unable to decode '" + mlString + "' learner");
						break;
				}
			} else AppLogger.logInfo(getClass(), "Unable to decode '" + mlString + "' learner");
		} else AppLogger.logInfo(getClass(), "Unable to decode '" + mlString + "' learner");
	}

	public MetaLearnerType getMetaType() {
		return mlType;
	}

	public BaseLearner[] getBaseLearners() {
		return atList;
	}	
	
	@Override
	public void addPreference(String prefString, String prefValue) {
		super.addPreference(prefString, prefValue);
		if(prefString.equals(VotingMetaLearner.BASE_LEARNERS) && mlType == MetaLearnerType.VOTING){
			List<BaseLearner> lList = new LinkedList<>();
			for(String item : prefValue.split(",")){
				 lList.add(new BaseLearner(AlgorithmType.valueOf(item.trim())));
			}
			atList = lList.toArray(new BaseLearner[lList.size()]);
		} else if(prefString.equals(StackingMetaLearner.BASE_LEARNERS) && (mlType == MetaLearnerType.STACKING || mlType == MetaLearnerType.STACKING_FULL)){
			List<BaseLearner> lList = new LinkedList<>();
			for(String item : prefValue.split(",")){
				 lList.add(new BaseLearner(AlgorithmType.valueOf(item.trim())));
			}
			atList = lList.toArray(new BaseLearner[lList.size()]);
		}
	}

	public String toCompactString(){
		String toRet = mlType.toString();
		if(atList != null && atList.length > 0){
			toRet = toRet + "[";
			for(BaseLearner bl : atList){
				toRet = toRet + bl.getAlgType() + ",";
			}
			toRet = toRet.substring(0, toRet.length()-1) + "]";
			if((mlType == MetaLearnerType.STACKING || mlType == MetaLearnerType.STACKING_FULL) && hasPreference(StackingMetaLearner.STACKING_LEARNER))
				toRet = toRet.substring(0, toRet.length()-1) + "@" + getPreference(StackingMetaLearner.STACKING_LEARNER) + "]";
		}
		return toRet;
	}
	
	public boolean hasBaseLearner(LearnerType otherLearner) {
		if(otherLearner != null && atList != null && atList.length > 0){
			for(BaseLearner bl : atList){
				if(bl.compareTo(otherLearner) == 0)
					return true;
			}
		}
		return false;
	}
	
	public boolean hasLearner(LearnerType otherLearner) {
		boolean base = hasBaseLearner(otherLearner);
		if(base)
			return true;
		else {
			if((mlType == MetaLearnerType.STACKING || mlType == MetaLearnerType.STACKING_FULL) && hasPreference(StackingMetaLearner.STACKING_LEARNER))
				return getPreference(StackingMetaLearner.STACKING_LEARNER).compareTo(otherLearner.toCompactString()) == 0;
			return false;
		}
	}
	
	@Override
	public String toString() {
		String toString = "";
		if(mlType != null){
			toString = toString + mlType.toString();
			if(atList != null && atList.length > 0){
				switch(mlType){
				case ARBITRATING:
					break;
				case BAGGING:
					toString = toString + "(" + Arrays.toString(atList).replace("[", "").replace("]", "") 
						+ "," + getPreference(BaggingMetaLearner.N_SAMPLES) + ")";
					break;
				case BOOSTING:
					break;
				case CASCADE_GENERALIZATION:
					break;
				case CASCADING:
					break;
				case DELEGATING:
					break;
				case STACKING:
				case STACKING_FULL:
					toString = toString + Arrays.toString(atList).replace("[", "(").replace("]", "@");
					toString = toString + getPreference(StackingMetaLearner.STACKING_LEARNER) + ")";
					break;
				case VOTING:
					toString = toString + Arrays.toString(atList).replace("[", "(").replace("]", ")");
					break;
				default:
					break;
				}
			}
		}
		return toString;
	}	
	
	@Override
	public int compareTo(LearnerType other) {
		if(other != null && other instanceof MetaLearner &&((MetaLearner)other).getMetaType() == mlType){
			if(atList != null && ((MetaLearner)other).getBaseLearners() != null){
				LearnerType[] otherList = ((MetaLearner)other).getBaseLearners();
				if(otherList.length != atList.length)
					return -1;
				for(int i=0;i<atList.length;i++){
					if(atList[i].compareTo(otherList[i]) != 0)
						return -1;
				}
				return 0;
			} else return -1;
		} else return -1;
	}

	public static String describe(MetaLearnerType type) {
		switch(type){
			case BAGGING:
				return "Samples training set into " + BaggingMetaLearner.N_SAMPLES + " groups and creates many copies of the same learner that are trained using different groups";
			default:
				return "ToBe Defined";
		}
	}	
	
	public BaseLearner toBase(){
		if(atList != null && atList.length > 0)
			return atList[0];
		else return null;
	}

	public void changeMetaLearner(MetaLearnerType mlt) {
		this.mlType = mlt;
	}

	@Override
	public LearnerType clone() {
		return new MetaLearner(mlType, atList, learnerPreferences);
	}

}
