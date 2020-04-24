/**
 * 
 */
package ippoz.reload.trainer;

import ippoz.reload.algorithm.AutomaticTrainingAlgorithm;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.TimedResult;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.meta.MetaData;
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
	
	private static int LINEAR_SEARCH_MAX_ITERATIONS = 8;
	
	public static String[] DECISION_FUNCTIONS = {
		"MODE(3)", "MODE(0.5)", "MODE(0.2)", "MODE(0.05)", 
		"MEDIAN(0.5)", "MEDIAN(0.2)", "MEDIAN(0.05)", 
		"MEDIAN_INTERVAL(0.1)", "MEDIAN_INTERVAL(0.05)", "MEDIAN_INTERVAL(0)", 
		"MODE_INTERVAL(0.1)", "MODE_INTERVAL(0.05)", "MODE_INTERVAL(0)", 
		"IQR", "IQR(1)", "IQR(0.5)", "IQR(0.2)", "IQR(0)", 
		"CONFIDENCE_INTERVAL","CONFIDENCE_INTERVAL(1)", "CONFIDENCE_INTERVAL(0.5)", "CONFIDENCE_INTERVAL(0.2)", 
		"LEFT_POSITIVE_IQR", "LEFT_POSITIVE_IQR(0)", "LEFT_IQR(1)", "LEFT_IQR(0.5)", 
		"RIGHT_IQR(1)", "RIGHT_IQR(0.5)", 
		"CLUSTER(STD)", "CLUSTER(0.1STD)", 
		"CLUSTER(0.5STD)", "CLUSTER(VAR)"};

	/** The possible configurations. */
	private List<BasicConfiguration> configurations;
	
	/**
	 * Instantiates a new algorithm trainer.
	 *
	 * @param algTag the algorithm tag
	 * @param dataSeries the chosen data series
	 * @param metric the used metric
	 * @param reputation the used reputation metric
	 * @param expList the considered train data
	 */
	public ConfigurationSelectorTrainer(LearnerType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, List<BasicConfiguration> basicConfigurations, String datasetName, int kfold) {
		super(algTag, dataSeries, metric, reputation, kList, datasetName, kfold);
		configurations = confCloneAndComplete(basicConfigurations);
	}
	
	public ConfigurationSelectorTrainer(LearnerType algTag, DataSeries dataSeries, List<Knowledge> kList, MetaData mData) {
		super(algTag, dataSeries, mData.getTargetMetric(), mData.getReputation(), kList, mData.getDatasetName(), mData.getKfold(), mData);
		configurations = confCloneAndComplete(mData.getConfigurationsFor(algTag));
	}
	
	/**
	 * Clones the configurations to avoid updating the same Java structures.
	 *
	 * @param inConf the configurations to clone
	 * @return the cloned list of configuration
	 */
	private List<BasicConfiguration> confCloneAndComplete(List<BasicConfiguration> inConf) {
		List<BasicConfiguration> list = new ArrayList<BasicConfiguration>(inConf.size());
		try {
			for(BasicConfiguration conf : inConf){
				BasicConfiguration ac = (BasicConfiguration) conf.clone();
				ac.addItem(BasicConfiguration.DATASET_NAME, getDatasetName());
				list.add(ac);
			}
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone Configurations");
		}
		return list;
	}

	@Override
	protected BasicConfiguration lookForBestConfiguration() {
		Map<String, ValueSeries> currentMetricValue = null;
		ValueSeries vs = null;
		DetectionAlgorithm algorithm;
		BasicConfiguration bestConf = null;
		BasicConfiguration currentConf = null;
		Map<TimedResult, AlgorithmResult> resultList = null;
		boolean trainingResult = true;
		try {
			metricScore = null;
			
			/* Iterates for Configurations */
			for(BasicConfiguration conf : configurations){
				currentMetricValue = new HashMap<String, ValueSeries>();
				for(String decFunctString : DECISION_FUNCTIONS){
					if(DecisionFunction.isApplicableTo(getAlgType(), decFunctString))
						currentMetricValue.put(decFunctString, new ValueSeries());
				}
				
				/* Iterates for K-Fold */
				for(Map<String, List<Knowledge>> knMap : getKnowledgeList()){
					currentConf = (BasicConfiguration)conf.clone();
					// eventually has metadata
					algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), currentConf);
					
					/* Automatic Training */
					trainingResult = false;
					if(algorithm instanceof AutomaticTrainingAlgorithm) {
						trainingResult = ((AutomaticTrainingAlgorithm)algorithm).automaticTraining(knMap.get("TRAIN"), false);
					}
					
					vs = algorithm.getLoggedScores();
					
					/* If training succeeded */
					if(!(algorithm instanceof AutomaticTrainingAlgorithm) || trainingResult){
						
						/* Calculates Algorithm Scores (just numbers, no threshold applied) */
						resultList = new HashMap<TimedResult, AlgorithmResult>();
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
								algorithm.setDecisionFunction(decFunctString);
								if(algorithm.getDecisionFunction() != null){
									List<AlgorithmResult> updatedList = updateResultWithDecision(algorithm.getDecisionFunction(), resultList);
									double val = getMetric().evaluateAnomalyResults(updatedList);
									currentMetricValue.get(decFunctString).addValue(val);
								}
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
							bestConf = (BasicConfiguration) conf.clone();
							bestConf.addItem(BasicConfiguration.THRESHOLD, decFunctString);
						}
					}
				}
				
			}
			
			/* Searches for the best static threshold, if any */
			if(resultList != null && vs != null){
				//System.out.println(metricScore.getAvg());
				String[] value = linearSearchOptimalSingleThreshold("STATIC_THRESHOLD_GREATER", vs, vs.getMin(), vs.getMax(), 0, resultList);
				if(getMetric().compareResults(Double.valueOf(value[1]), metricScore.getAvg()) > 0){
					metricScore.clear();
					metricScore.addValue(Double.valueOf(value[1]));
					bestConf.addItem(BasicConfiguration.THRESHOLD, value[0]);
					
				}
				value = linearSearchOptimalSingleThreshold("STATIC_THRESHOLD_LOWER", vs, vs.getMin(), vs.getMax(), 0, resultList);
				if(getMetric().compareResults(Double.valueOf(value[1]), metricScore.getAvg()) > 0){
					metricScore.clear();
					metricScore.addValue(Double.valueOf(value[1]));
					bestConf.addItem(BasicConfiguration.THRESHOLD, value[0]);
				}
			}
						
			// eventually has metadata
			algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), bestConf);
			if(algorithm instanceof AutomaticTrainingAlgorithm) {
				((AutomaticTrainingAlgorithm)algorithm).automaticTraining(getKnowledgeList().get(0).get("TEST"), true);
			} else {
				for(Knowledge knowledge : getKnowledgeList().get(0).get("TEST")){
					//algorithm.setDecisionFunction(dFunctionString);
					getMetric().evaluateMetric(algorithm, knowledge);
				}
			}
			trainResult = new HashMap<>();
			for(Knowledge know : kList){
				trainResult.put(know, calculateResults(algorithm, know));
			}
			trainMetricScore = algorithm.getTrainScore();
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone configuration");
		}
		return bestConf;
	}
	
	private String[] linearSearchOptimalSingleThreshold(String thrCode, ValueSeries scores, double thrLeft, double thrRight, int iteration, Map<TimedResult, AlgorithmResult> resultList){
		double thrValue = (thrRight + thrLeft)/2;
		String threshold = thrCode + "(" + AppUtility.formatDouble(thrValue) + ")";
		List<AlgorithmResult> updatedList = updateResultWithDecision(DecisionFunction.buildDecisionFunction(scores, threshold, false), resultList);
		double mScore = getMetric().evaluateAnomalyResults(updatedList);
		
		if(iteration <= LINEAR_SEARCH_MAX_ITERATIONS){
			String[] leftBest = linearSearchOptimalSingleThreshold(thrCode, scores, thrLeft, thrValue, iteration + 1, resultList);
			String[] rightBest = linearSearchOptimalSingleThreshold(thrCode, scores, thrValue, thrRight, iteration + 1, resultList);
			return (getMetric().compareResults(mScore, Double.valueOf(leftBest[1])) > 0  && 
						getMetric().compareResults(mScore, Double.valueOf(rightBest[1])) > 0) ? new String[]{threshold, "" + mScore} : 
							(getMetric().compareResults(Double.valueOf(leftBest[1]), Double.valueOf(rightBest[1])) > 0 ? leftBest : rightBest); 
		} else return new String[]{threshold, "" + mScore};
	}
	
	private static List<AlgorithmResult> updateResultWithDecision(DecisionFunction dFunction, Map<TimedResult, AlgorithmResult> resMap){
		List<AlgorithmResult> newList = new LinkedList<AlgorithmResult>();
		for(TimedResult tr : resMap.keySet()){
			AlgorithmResult alr = resMap.get(tr);
			AnomalyResult ar = dFunction.classify(resMap.get(tr));
			newList.add(new AlgorithmResult(alr.getData(), alr.getInjection(), DetectionAlgorithm.convertResultIntoDouble(ar), ar, dFunction, alr.getConfidence()));
		}
		return newList;
	}
	
}
