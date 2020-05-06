/**
 * 
 */
package ippoz.reload.algorithm.configuration;

import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.meta.MetaData;
import ippoz.reload.meta.MetaLearnerType;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MetaConfiguration.
 *
 * @author Tommy
 */
public class MetaConfiguration extends BasicConfiguration {
	
	private MetaLearner mlType;
	
	private Map<LearnerType, List<BasicConfiguration>> baseConfs;

	public MetaConfiguration(LearnerType learnerType) {
		super();
		if(learnerType instanceof MetaLearner)
			this.mlType = (MetaLearner)learnerType;
		else mlType = null;
		baseConfs = new HashMap<>();
	}
	
	public MetaConfiguration(LearnerType learnerType, Map<LearnerType, List<BasicConfiguration>> baseConfs) {
		this(learnerType);
		this.baseConfs = baseConfs;
	}
	
	public MetaConfiguration(LearnerType learnerType, Map<String, Object> confMap, Map<LearnerType, List<BasicConfiguration>> baseConfs) {
		super(confMap);
		if(learnerType instanceof MetaLearner)
			this.mlType = (MetaLearner)learnerType;
		else mlType = null;
		this.baseConfs = baseConfs;
	}

	public Map<LearnerType, List<BasicConfiguration>> getConfigurations() {
		return baseConfs;
	}
	
	public boolean addConfiguration(LearnerType fileLearner, List<BasicConfiguration> confs){
		if(fileLearner != null)
			baseConfs.put(fileLearner, confs);
		return fileLearner != null;
	}
	
	/**
	 * Gets the algorithm type.
	 *
	 * @return the algType
	 */
	public MetaLearnerType getMetaType(){
		return mlType.getMetaType();
	}
	
	@Override
	public String toString() {
		return mlType + ":" + super.toString();
	}

	@Override
	public LearnerType getLearnerType() {
		return mlType;
	}

	public MetaData generateMetaData() {
		Metric met = null;
		Reputation rep = null;
		if(hasItem(METRIC)){
			met = Metric.fromString(getItem(METRIC), "absolute", true);
		}
		if(hasItem(REPUTATION)){
			rep = Reputation.fromString(getItem(REPUTATION), met, true);
		}
		return new MetaData(getItem(SCORES_FOLDER), (int)Double.parseDouble(getItem(K_FOLD)), Boolean.valueOf(getItem(FORCE_META_TRAINING)), getItem(DATASET_NAME), met, rep, baseConfs);
	}

}
