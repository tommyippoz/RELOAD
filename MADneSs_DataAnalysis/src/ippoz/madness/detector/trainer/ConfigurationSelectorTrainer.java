/**
 * 
 */
package ippoz.madness.detector.trainer;

import ippoz.madness.detector.algorithm.AutomaticTrainingAlgorithm;
import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.commons.support.ValueSeries;
import ippoz.madness.detector.metric.FalsePositiveRate_Metric;
import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.metric.TruePositiveRate_Metric;
import ippoz.madness.detector.reputation.Reputation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

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
		try {
			metricScore = null;
			for(AlgorithmConfiguration conf : configurations){
				currentMetricValue = new ValueSeries();
				for(Map<String, List<Knowledge>> knMap : getKnowledgeList()){
					metricResults = new LinkedList<Double>();
					algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), conf);
					if(algorithm instanceof AutomaticTrainingAlgorithm) {
						((AutomaticTrainingAlgorithm)algorithm).automaticTraining(knMap.get("TRAIN"), false);
					}
					for(Knowledge knowledge : knMap.get("TEST")){
						metricResults.add(getMetric().evaluateMetric(algorithm, knowledge)[0]);
					}
					currentMetricValue.addValue(AppUtility.calcAvg(metricResults.toArray(new Double[metricResults.size()])));
				}
				if(metricScore == null || getMetric().compareResults(currentMetricValue, metricScore) == 1){	
					metricScore = currentMetricValue;
					bestConf = (AlgorithmConfiguration) conf.clone();
				}
			}
			algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), bestConf);
			if(algorithm instanceof AutomaticTrainingAlgorithm) {
				((AutomaticTrainingAlgorithm)algorithm).automaticTraining(getKnowledgeList().get(0).get("TEST"), true);
			}
			trainScore = algorithm.getTrainScore();
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone configuration");
		}
		return bestConf;
	}
	
}
