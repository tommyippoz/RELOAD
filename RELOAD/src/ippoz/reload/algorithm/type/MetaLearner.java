/**
 * 
 */
package ippoz.reload.algorithm.type;

import ippoz.reload.algorithm.meta.BaggingMetaLearner;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.meta.MetaLearnerType;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class MetaLearner extends LearnerType {
	
	private MetaLearnerType mlType;
	
	private AlgorithmType[] atList;
	
	public MetaLearner(MetaLearnerType mlType, AlgorithmType[] atList) {
		super();
		this.mlType = mlType;
		this.atList = atList;
	}

	public MetaLearner(MetaLearnerType mlType, AlgorithmType[] atList, Map<String, String> learnerPreferences) {
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
						if(toDecode.contains(",")){
							try {
								atList = new AlgorithmType[]{AlgorithmType.valueOf(toDecode.split(",")[0].trim())};
								addPreference(BaggingMetaLearner.N_SAMPLES, toDecode.split(",")[1].trim());
							} catch(Exception ex){
								AppLogger.logInfo(getClass(), "Unable to decode '" + mlString + "' learner");
							}
						} else {
							try {
								atList = new AlgorithmType[]{AlgorithmType.valueOf(toDecode)};
								addPreference(BaggingMetaLearner.N_SAMPLES, String.valueOf(BaggingMetaLearner.DEFAULT_SAMPLES));
							} catch(Exception ex){
								AppLogger.logInfo(getClass(), "Unable to decode '" + mlString + "' learner");
							}
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

	public AlgorithmType[] getBaseLearners() {
		return atList;
	}	
	
	@Override
	public String toString() {
		String toString = "";
		if(mlType != null){
			toString = toString + mlType.toString();
			if(atList != null && atList.length > 0)
				toString = toString + Arrays.toString(atList).replace("[", "(").replace("]", ")");
		}
		return toString;
	}	
	
	@Override
	public int compareTo(LearnerType other) {
		if(other != null && other instanceof MetaLearner &&((MetaLearner)other).getMetaType() == mlType){
			return Arrays.equals(atList, ((MetaLearner)other).getBaseLearners()) ? 0 : -1;
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
			return new BaseLearner(atList[0]);
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
