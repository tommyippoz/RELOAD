/**
 * 
 */
package ippoz.reload.trainer;

import ippoz.reload.algorithm.AutomaticTrainingAlgorithm;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Class ConfigurationSelectorTrainer.
 * This is used from algorithms which can select the best configuration out of a set of possible ones.
 * 
 * @author Tommy
 *
 */
public class ConfigurationSelectorTrainer extends AlgorithmTrainer {

	/** The possible configurations. */
	private List<AlgorithmConfiguration> configurations;
	
	/**
	 * Instantiates a new algorithm trainer.
	 *
	 * @param algTag the algorithm tag
	 * @param dataSeries the chosen data series
	 * @param metric the used metric
	 * @param reputation the used reputation metric
	 * @param expList the considered train data
	 */
	public ConfigurationSelectorTrainer(AlgorithmType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, List<AlgorithmConfiguration> basicConfigurations, int kfold) {
		super(algTag, dataSeries, metric, reputation, kList, kfold);
		configurations = confClone(basicConfigurations);
	}
	
	/**
	 * Clones the configurations to avoid updating the same Java structures.
	 *
	 * @param inConf the configurations to clone
	 * @return the cloned list of configuration
	 */
	private List<AlgorithmConfiguration> confClone(List<AlgorithmConfiguration> inConf) {
		List<AlgorithmConfiguration> list = new ArrayList<AlgorithmConfiguration>(inConf.size());
		try {
			for(AlgorithmConfiguration conf : inConf){
				list.add((AlgorithmConfiguration) conf.clone());
			}
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone Configurations");
		}
		return list;
	}

	@Override
	protected AlgorithmConfiguration lookForBestConfiguration() {
		ValueSeries currentMetricValue = null;
		List<Double> metricResults;
		DetectionAlgorithm algorithm;
		AlgorithmConfiguration bestConf = null;
		AlgorithmConfiguration currentConf = null;
		boolean trainingResult = true;
		try {
			metricScore = null;
			for(AlgorithmConfiguration conf : configurations){
				currentMetricValue = new ValueSeries();
				for(Map<String, List<Knowledge>> knMap : getKnowledgeList()){
					metricResults = new LinkedList<Double>();
					currentConf = (AlgorithmConfiguration)conf.clone();
					algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), currentConf);
					if(algorithm instanceof AutomaticTrainingAlgorithm) {
						trainingResult = ((AutomaticTrainingAlgorithm)algorithm).automaticTraining(knMap.get("TRAIN"), false);
					}
					if(trainingResult){
						for(Knowledge knowledge : knMap.get("TEST")){
							metricResults.add(getMetric().evaluateMetric(algorithm, knowledge)[0]);
						}
						currentMetricValue.addValue(AppUtility.calcAvg(metricResults.toArray(new Double[metricResults.size()])));
					}	
				}
				if(metricScore == null || getMetric().compareResults(currentMetricValue, metricScore) == 1){	
					metricScore = currentMetricValue;
					bestConf = (AlgorithmConfiguration) conf.clone();
				}
			}
			algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), bestConf);
			if(algorithm instanceof AutomaticTrainingAlgorithm) {
				((AutomaticTrainingAlgorithm)algorithm).automaticTraining(getKnowledgeList().get(0).get("TEST"), true);
			} else {
				for(Knowledge knowledge : getKnowledgeList().get(0).get("TEST")){
					getMetric().evaluateMetric(algorithm, knowledge);
				}
			}
			trainScore = algorithm.getTrainScore();
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone configuration");
		}
		return bestConf;
	}
	
}
