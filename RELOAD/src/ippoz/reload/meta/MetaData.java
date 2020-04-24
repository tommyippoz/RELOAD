/**
 * 
 */
package ippoz.reload.meta;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class MetaData {
	
	private int kfold;
	
	private String datasetName;
	
	private Metric targetMetric;
	
	private Reputation reputation;
	
	private Map<AlgorithmType, List<BasicConfiguration>> confMap;

	public MetaData(int kfold, String datasetName, Metric targetMetric,
			Reputation reputation, Map<AlgorithmType, List<BasicConfiguration>> confMap) {
		this.kfold = kfold;
		this.datasetName = datasetName;
		this.targetMetric = targetMetric;
		this.reputation = reputation;
		this.confMap = confMap;
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

	public Map<AlgorithmType, List<BasicConfiguration>> getConfMap() {
		return confMap;
	}

	public List<BasicConfiguration> getConfigurationsFor(LearnerType algTag) {
		if(confMap != null && confMap.containsKey(algTag))
			return confMap.get(algTag);
		else return new LinkedList<>();
	}	

}
