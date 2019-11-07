/**
 * 
 */
package ippoz.reload.trainer;

import ippoz.reload.algorithm.AutomaticTrainingAlgorithm;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.TimedResult;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	public static String[] DECISION_FUNCTIONS = {"MODE(0.5)", "MODE(0.2)", "MODE(0.05)", "MEDIAN(0.5)", "MEDIAN(0.2)", "MEDIAN(0.05)", "MEDIAN_INTERVAL(0.1)", "MEDIAN_INTERVAL(0.05)", "MEDIAN_INTERVAL(0)", "MODE_INTERVAL(0.1)", "MODE_INTERVAL(0.05)", "MODE_INTERVAL(0)", "IQR", "IQR(1)", "IQR(0.5)", "IQR(0.2)", "IQR(0)", "CONFIDENCE_INTERVAL","CONFIDENCE_INTERVAL(1)", "CONFIDENCE_INTERVAL(0.5)", "CONFIDENCE_INTERVAL(0.2)", "LEFT_POSITIVE_IQR", "LEFT_POSITIVE_IQR(0)", "LEFT_IQR(1)", "LEFT_IQR(0.5)", "CLUSTER(STD)", "CLUSTER(0.1STD)", "CLUSTER(0.5STD)", "CLUSTER(VAR)"};

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
	public ConfigurationSelectorTrainer(AlgorithmType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, List<AlgorithmConfiguration> basicConfigurations, String datasetName, int kfold) {
		super(algTag, dataSeries, metric, reputation, kList, datasetName, kfold);
		configurations = confCloneAndComplete(basicConfigurations);
	}
	
	/**
	 * Clones the configurations to avoid updating the same Java structures.
	 *
	 * @param inConf the configurations to clone
	 * @return the cloned list of configuration
	 */
	private List<AlgorithmConfiguration> confCloneAndComplete(List<AlgorithmConfiguration> inConf) {
		List<AlgorithmConfiguration> list = new ArrayList<AlgorithmConfiguration>(inConf.size());
		try {
			for(AlgorithmConfiguration conf : inConf){
				AlgorithmConfiguration ac = (AlgorithmConfiguration) conf.clone();
				ac.addItem(AlgorithmConfiguration.DATASET_NAME, getDatasetName());
				list.add(ac);
			}
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone Configurations");
		}
		return list;
	}

	@Override
	protected AlgorithmConfiguration lookForBestConfiguration() {
		Map<String, ValueSeries> currentMetricValue = null;
		//List<Double> metricResults;
		DetectionAlgorithm algorithm;
		AlgorithmConfiguration bestConf = null;
		AlgorithmConfiguration currentConf = null;
		boolean trainingResult = true;
		try {
			metricScore = null;
			
			/* Iterates for Configurations */
			for(AlgorithmConfiguration conf : configurations){
				currentMetricValue = new HashMap<String, ValueSeries>();
				for(String decFunctString : DECISION_FUNCTIONS){
					if(DecisionFunction.isApplicableTo(getAlgType(), decFunctString))
						currentMetricValue.put(decFunctString, new ValueSeries());
				}
				
				/* Iterates for K-Fold */
				for(Map<String, List<Knowledge>> knMap : getKnowledgeList()){
					//metricResults = new LinkedList<Double>();
					currentConf = (AlgorithmConfiguration)conf.clone();
					algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), currentConf);
					
					/* Automatic Training */
					trainingResult = false;
					if(algorithm instanceof AutomaticTrainingAlgorithm) {
						trainingResult = ((AutomaticTrainingAlgorithm)algorithm).automaticTraining(knMap.get("TRAIN"), false);
					}
					
					/* If training succeeded */
					if(!(algorithm instanceof AutomaticTrainingAlgorithm) || trainingResult){
						
						/* Calculates Algorithm Scores (just numbers, no threshold applied) */
						Map<TimedResult, AlgorithmResult> resultList = new HashMap<TimedResult, AlgorithmResult>();
						for(Knowledge know : knMap.get("TEST")){
							for(int i=0;i<know.size();i++){
								Snapshot snap = know.buildSnapshotFor(i, getDataSeries());
								AlgorithmResult ar = algorithm.evaluateSnapshot(know, i);
								resultList.put(new TimedResult(snap.getTimestamp(), Double.NaN, ar.getScore(), snap.getInjectedElement()), ar);
							}
						}
						
						/* Tries all the possible decision functions */
						for(String decFunctString : DECISION_FUNCTIONS){
							if(DecisionFunction.isApplicableTo(getAlgType(), decFunctString)){
								List<TimedResult> updatedList = updateResultWithDecision(algorithm, resultList, decFunctString);
								double val = getMetric().evaluateAnomalyResults(updatedList);
								//System.out.println(decFunctString + " ADD " + val);
								currentMetricValue.get(decFunctString).addValue(val);
							}
						}
						
					}	
				}
				
				/* Chooses the best decision function out of the available ones */
				for(String decFunctString : DECISION_FUNCTIONS){
					if(DecisionFunction.isApplicableTo(getAlgType(), decFunctString)){
						//System.out.println(decFunctString + " - " + currentMetricValue.get(decFunctString).getAvg());
						if(metricScore == null || getMetric().compareResults(currentMetricValue.get(decFunctString), metricScore) == 1){	
							metricScore = currentMetricValue.get(decFunctString);
							//System.out.println("UPDATE " + decFunctString + " - " + metricScore);
							bestConf = (AlgorithmConfiguration) conf.clone();
							bestConf.addItem(AlgorithmConfiguration.THRESHOLD, decFunctString);
						}
					}
				}
				
			}
			
			System.out.println(bestConf.getItem(AlgorithmConfiguration.THRESHOLD));
			
			algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), bestConf);
			if(algorithm instanceof AutomaticTrainingAlgorithm) {
				((AutomaticTrainingAlgorithm)algorithm).automaticTraining(getKnowledgeList().get(0).get("TEST"), true);
			} else {
				for(Knowledge knowledge : getKnowledgeList().get(0).get("TEST")){
					//algorithm.setDecisionFunction(dFunctionString);
					getMetric().evaluateMetric(algorithm, knowledge);
				}
			}
			trainScore = algorithm.getTrainScore();
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone configuration");
		}
		return bestConf;
	}
	
	private static List<TimedResult> updateResultWithDecision(DetectionAlgorithm algorithm, Map<TimedResult, AlgorithmResult> resMap, String decisionString){
		List<TimedResult> newList = new LinkedList<TimedResult>();
		algorithm.setDecisionFunction(decisionString);
		for(TimedResult tr : resMap.keySet()){
			AnomalyResult ar = algorithm.getDecisionFunction().classify(resMap.get(tr));
			tr.updateEvaluationScore(DetectionAlgorithm.convertResultIntoDouble(ar));
			newList.add(tr);
		}
		return newList;
	}
	
}
