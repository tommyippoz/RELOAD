/**
 * 
 */
package ippoz.reload.algorithm.type;

import ippoz.reload.algorithm.meta.BaggingMetaLearner;
import ippoz.reload.algorithm.meta.BoostingMetaLearner;
import ippoz.reload.algorithm.meta.CascadingMetaLearner;
import ippoz.reload.algorithm.meta.DataSeriesMetaLearner;
import ippoz.reload.algorithm.meta.DelegatingMetaLearner;
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
	
	private MetaLearner(MetaLearnerType mlType){
		this(mlType, null);
	}
	
	public MetaLearner(MetaLearnerType mlType, BaseLearner[] atList) {
		super();
		this.mlType = mlType;
		this.atList = atList;
		setDefaultMetaFreferences();
	}

	public MetaLearner(MetaLearnerType mlType, BaseLearner[] atList, Map<String, String> learnerPreferences) {
		super(learnerPreferences);
		this.mlType = mlType;
		this.atList = atList;
	}
	
	public static MetaLearner buildMetaLearner(String mlString){
		MetaLearner ml = null;
		if(mlString != null && mlString.trim().length() > 0){
			mlString = mlString.trim();
			if(mlString.contains("(") && mlString.contains(")")){
				ml = new MetaLearner(MetaLearnerType.valueOf(mlString.substring(0, mlString.indexOf("(")).trim()));
				String toDecode = mlString.substring(mlString.indexOf("(")+1, mlString.length()-1).trim();
				switch(ml.getMetaType()){
					case BAGGING:
						if(toDecode.contains(","))
							ml.addPreference(BaggingMetaLearner.N_SAMPLES, toDecode.split(",")[1].trim());
						else ml.addPreference(BaggingMetaLearner.N_SAMPLES, String.valueOf(BaggingMetaLearner.DEFAULT_SAMPLES));	 
						try {
							ml.setBaseLearners(new BaseLearner[]{new BaseLearner(AlgorithmType.valueOf(toDecode.split(",")[0].trim()))});
						} catch(Exception ex){
							AppLogger.logInfo(MetaLearner.class, "Unable to decode '" + mlString + "' learner");
							return null;
						}
						break;
					case BOOSTING:
						if(toDecode.contains(",")){
							String[] splitted = toDecode.split(",");
							ml.addPreference(BoostingMetaLearner.N_ENSEMBLES, toDecode.split(",")[1].trim());
							if(splitted.length > 2){
								ml.addPreference(BoostingMetaLearner.LEARNING_SPEED, toDecode.split(",")[2].trim());
							} else ml.addPreference(BoostingMetaLearner.LEARNING_SPEED, String.valueOf(BoostingMetaLearner.DEFAULT_SPEED));
						} else {
							ml.addPreference(BoostingMetaLearner.N_ENSEMBLES, String.valueOf(BoostingMetaLearner.DEFAULT_ENSEMBLES));
							ml.addPreference(BoostingMetaLearner.LEARNING_SPEED, String.valueOf(BoostingMetaLearner.DEFAULT_SPEED));
						}
						try {
							ml.setBaseLearners(new BaseLearner[]{new BaseLearner(AlgorithmType.valueOf(toDecode.split(",")[0].trim()))});
						} catch(Exception ex){
							AppLogger.logInfo(MetaLearner.class, "Unable to decode '" + mlString + "' learner");
							return null;
						}
						break;
					case CASCADE_GENERALIZATION:
					case CASCADING:
						if(toDecode.contains("@")){
							String[] splitted = toDecode.split("@");
							ml.addPreference(CascadingMetaLearner.CONFIDENCE_THRESHOLD, splitted[1].trim());
							toDecode = splitted[0].trim();
						} else ml.addPreference(CascadingMetaLearner.CONFIDENCE_THRESHOLD, String.valueOf(CascadingMetaLearner.DEFAULT_CONFIDENCE_THRESHOLD));
						ml.addPreference(BoostingMetaLearner.LEARNING_SPEED, String.valueOf(BoostingMetaLearner.DEFAULT_SPEED));
						ml.addPreference(DataSeriesMetaLearner.BASE_LEARNERS, toDecode.trim());
						try {
							List<BaseLearner> lList = new LinkedList<>();
							for(String item : toDecode.split(",")){
								 lList.add(new BaseLearner(AlgorithmType.valueOf(item.trim())));
							}
							ml.setBaseLearners(lList.toArray(new BaseLearner[lList.size()]));
						} catch(Exception ex){
							AppLogger.logInfo(MetaLearner.class, "Unable to decode '" + mlString + "' learner");
							return null;
						}
						break;
					case DELEGATING:
						if(toDecode.contains("@")){
							String[] splitted = toDecode.split("@");
							ml.addPreference(DelegatingMetaLearner.CONFIDENCE_THRESHOLD, splitted[1].trim());
							toDecode = splitted[0].trim();
						} else ml.addPreference(DelegatingMetaLearner.CONFIDENCE_THRESHOLD, String.valueOf(DelegatingMetaLearner.DEFAULT_CONFIDENCE_THRESHOLD));
						ml.addPreference(DataSeriesMetaLearner.BASE_LEARNERS, toDecode.trim());
						try {
							List<BaseLearner> lList = new LinkedList<>();
							for(String item : toDecode.split(",")){
								 lList.add(new BaseLearner(AlgorithmType.valueOf(item.trim())));
							}
							ml.setBaseLearners(lList.toArray(new BaseLearner[lList.size()]));
						} catch(Exception ex){
							AppLogger.logInfo(MetaLearner.class, "Unable to decode '" + mlString + "' learner");
							return null;
						}
						break;
					case STACKING:
					case STACKING_FULL:
						if(toDecode.contains("@")){
							ml.addPreference(StackingMetaLearner.STACKING_LEARNER, toDecode.split("@")[1].trim());
							toDecode = toDecode.split("@")[0].trim();
						} else ml.addPreference(StackingMetaLearner.STACKING_LEARNER, StackingMetaLearner.DEFAULT_META_LEARNER.toCompactString());
						ml.addPreference(StackingMetaLearner.BASE_LEARNERS, toDecode.trim());
						try {
							List<BaseLearner> lList = new LinkedList<>();
							for(String item : toDecode.split(",")){
								 lList.add(new BaseLearner(AlgorithmType.valueOf(item.trim())));
							}
							ml.setBaseLearners(lList.toArray(new BaseLearner[lList.size()]));
						} catch(Exception ex){
							AppLogger.logInfo(MetaLearner.class, "Unable to decode '" + mlString + "' learner");
							return null;
						}
						break;
					case WEIGHTED_VOTING:
					case VOTING:
						ml.addPreference(VotingMetaLearner.BASE_LEARNERS, toDecode.trim());
						try {
							List<BaseLearner> lList = new LinkedList<>();
							for(String item : toDecode.split(",")){
								 lList.add(new BaseLearner(AlgorithmType.valueOf(item.trim())));
							}
							ml.setBaseLearners(lList.toArray(new BaseLearner[lList.size()]));
						} catch(Exception ex){
							AppLogger.logInfo(MetaLearner.class, "Unable to decode '" + mlString + "' learner");
							return null;
						}
						break;
					default:
						AppLogger.logInfo(MetaLearner.class, "Unable to decode '" + mlString + "' learner");
						return null;
				}
			} else {
				AppLogger.logInfo(MetaLearner.class, "Unable to decode '" + mlString + "' learner");
				return null;
			}
		} else {
			AppLogger.logInfo(MetaLearner.class, "Unable to decode '" + mlString + "' learner");
			return null;
		}
		return ml;
	}
	
	private void setBaseLearners(BaseLearner[] baseLearners) {
		this.atList = baseLearners;
	}

	private void setDefaultMetaFreferences() {
		switch(mlType){
			case BAGGING:
				addPreference(BaggingMetaLearner.N_SAMPLES, String.valueOf(BaggingMetaLearner.DEFAULT_SAMPLES));
				break;
			case BOOSTING:
				addPreference(BoostingMetaLearner.N_ENSEMBLES, String.valueOf(BoostingMetaLearner.DEFAULT_ENSEMBLES));
				addPreference(BoostingMetaLearner.LEARNING_SPEED, String.valueOf(BoostingMetaLearner.DEFAULT_SPEED));
				break;
			case CASCADE_GENERALIZATION:
			case CASCADING:
				if(atList != null)
					addPreference(DataSeriesMetaLearner.BASE_LEARNERS, Arrays.toString(atList).replace("[", "").replace("]", ""));
				addPreference(BoostingMetaLearner.LEARNING_SPEED, String.valueOf(BoostingMetaLearner.DEFAULT_SPEED));
				addPreference(CascadingMetaLearner.CONFIDENCE_THRESHOLD, String.valueOf(CascadingMetaLearner.DEFAULT_CONFIDENCE_THRESHOLD));
				break;
			case DELEGATING:
				if(atList != null)
					addPreference(DataSeriesMetaLearner.BASE_LEARNERS, Arrays.toString(atList).replace("[", "").replace("]", ""));
				addPreference(DelegatingMetaLearner.CONFIDENCE_THRESHOLD, String.valueOf(DelegatingMetaLearner.DEFAULT_CONFIDENCE_THRESHOLD));
				break;
			case STACKING:
			case STACKING_FULL:
				if(atList != null)
					addPreference(DataSeriesMetaLearner.BASE_LEARNERS, Arrays.toString(atList).replace("[", "").replace("]", ""));
				break;
			case WEIGHTED_VOTING:
			case VOTING:
				if(atList != null)
					addPreference(DataSeriesMetaLearner.BASE_LEARNERS, Arrays.toString(atList).replace("[", "").replace("]", ""));
				break;
		}
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
		if(prefString.equals(DataSeriesMetaLearner.BASE_LEARNERS) && 
				(mlType == MetaLearnerType.WEIGHTED_VOTING || mlType == MetaLearnerType.STACKING || 
					mlType == MetaLearnerType.DELEGATING || mlType == MetaLearnerType.CASCADING || mlType == MetaLearnerType.CASCADE_GENERALIZATION ||
						mlType == MetaLearnerType.STACKING_FULL || mlType == MetaLearnerType.VOTING)){
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
					case BAGGING:
						toString = toString + "(" + Arrays.toString(atList).replace("[", "").replace("]", "") 
							+ "," + getPreference(BaggingMetaLearner.N_SAMPLES) + ")";
						break;
					case BOOSTING:
						toString = toString + "(" + Arrays.toString(atList).replace("[", "").replace("]", "") 
							+ "," + getPreference(BoostingMetaLearner.N_ENSEMBLES) + "," + getPreference(BoostingMetaLearner.LEARNING_SPEED) + ")";
						break;
					case CASCADE_GENERALIZATION:
					case CASCADING:
						toString = toString + "(" + Arrays.toString(atList).replace("[", "").replace("]", "") 
							+ "@" + getPreference(CascadingMetaLearner.CONFIDENCE_THRESHOLD) + "@" + getPreference(BoostingMetaLearner.LEARNING_SPEED) + ")";
						break;
					case DELEGATING:
						toString = toString + "(" + Arrays.toString(atList).replace("[", "").replace("]", "") 
						+ "@" + getPreference(DelegatingMetaLearner.CONFIDENCE_THRESHOLD) + ")";
					break;
					case STACKING:
					case STACKING_FULL:
						toString = toString + Arrays.toString(atList).replace("[", "(").replace("]", "@");
						toString = toString + getPreference(StackingMetaLearner.STACKING_LEARNER) + ")";
						break;
					case WEIGHTED_VOTING:
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
		/*if(other != null && other instanceof MetaLearner &&((MetaLearner)other).getMetaType() == mlType){
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
		} else return -1;*/
		if(other != null)
			return toString().compareTo(other.toString());
		else return -1;
	}

	public static String describe(MetaLearnerType type) {
		switch(type){
			case BAGGING:
				return "Samples training set into " + BaggingMetaLearner.N_SAMPLES + 
						" groups and creates many copies of the same learner that are trained using "
						+ "different groups";
			case BOOSTING:
				return "Uses the chosen learner to build " + BoostingMetaLearner.N_ENSEMBLES + " weak learners "
						+ "(stumps), which are going to be averaged, weighted using confidence, for final score.";
			case CASCADING:
				return "Uses the chosen learner to build " + BoostingMetaLearner.N_ENSEMBLES + " weak learners "
					+ "(stumps), which are going to be averaged, weighted using confidence, for final score."
					+ "If a single weak learner reached a confidence " + CascadingMetaLearner.CONFIDENCE_THRESHOLD 
					+ ", evaluation ends and outputs partial score.";
			case DELEGATING:
				return "Independently runs " + DataSeriesMetaLearner.BASE_LEARNERS + " and then chooses the first algorithm"
					+ "which is confident enough, according to " + DelegatingMetaLearner.CONFIDENCE_THRESHOLD + " for final evaluation."; 
			case STACKING:
				return "Independently runs " + DataSeriesMetaLearner.BASE_LEARNERS + " and aggregates them through "
					+ StackingMetaLearner.STACKING_LEARNER + ", which provides the final score.";
			case STACKING_FULL:
				return "Independently runs " + DataSeriesMetaLearner.BASE_LEARNERS + " and aggregates them - alongside with "
						+ "relevant datasets features -  through " + StackingMetaLearner.STACKING_LEARNER + ", which provides the final score.";
			case VOTING:
				return "Independently runs " + DataSeriesMetaLearner.BASE_LEARNERS + " and then counts "
						+ "how many of them raised anomalies. Final score sums anomalies raised by each algorithm."; 
			case WEIGHTED_VOTING:
				return "Independently runs " + DataSeriesMetaLearner.BASE_LEARNERS + " and then counts "
					+ "how many of them raised anomalies. Final score weights anomalies raised by each algorithm.";
			default:
				return "Not Implemented yet";	
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
