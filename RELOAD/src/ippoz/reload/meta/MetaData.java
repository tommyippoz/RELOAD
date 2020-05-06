/**
 * 
 */
package ippoz.reload.meta;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class MetaData {
	
	private int kfold;
	
	private String scoresFolder;
	
	private boolean forceBaseTraining;
	
	private String datasetName;
	
	private Metric targetMetric;
	
	private Reputation reputation;
	
	private Map<LearnerType, List<BasicConfiguration>> confMap;

	public MetaData(String scoresFolder, int kfold, boolean forceBaseTraining, String datasetName, Metric targetMetric, Reputation reputation, Map<LearnerType, List<BasicConfiguration>> baseConfs) {
		this.scoresFolder = scoresFolder;
		this.kfold = kfold;
		this.datasetName = datasetName;
		this.targetMetric = targetMetric;
		this.reputation = reputation;
		this.confMap = baseConfs;
		this.forceBaseTraining = forceBaseTraining;
	}

	public int getKfold() {
		return kfold;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public Metric getTargetMetric() {
		return targetMetric;
	}

	public Reputation getReputation() {
		return reputation;
	}

	public Map<LearnerType, List<BasicConfiguration>> getConfMap() {
		return confMap;
	}

	public List<BasicConfiguration> getConfigurationsFor(LearnerType algTag) {
		List<BasicConfiguration> confList = null;
		if(confMap != null){
			for(LearnerType lt : confMap.keySet()){
				if(lt.compareTo(algTag) == 0){
					confList = confMap.get(lt);
					break;
				}
			}
		}
		if(confList == null || confList.size() == 0){
			AppLogger.logError(getClass(), "UnrecognizedConfiguration", algTag + " does not have an associated configuration: basic will be applied");
			confList = new LinkedList<BasicConfiguration>();
			confList.add(BasicConfiguration.buildConfiguration(algTag));
		}
		return confList;
	}

	public Metric[] getValidationMetrics() {
		return null;
	}

	public boolean getForceTraining() {
		return forceBaseTraining;
	}

	public String extractScoresFolder(String learnerString) {
		return scoresFolder + getDatasetName() + File.separatorChar + learnerString;
	}	

}
