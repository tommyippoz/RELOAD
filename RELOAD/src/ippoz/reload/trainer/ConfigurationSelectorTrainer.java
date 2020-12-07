/**
 * 
 */
package ippoz.reload.trainer;

import ippoz.reload.algorithm.AutomaticTrainingAlgorithm;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.meta.MetaData;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	private static int LINEAR_SEARCH_MAX_ITERATIONS = 3;

	/** The possible configurations. */
	private List<BasicConfiguration> configurations;
	
	private DetectionAlgorithm bestAlgorithm;
	
	/**
	 * Instantiates a new algorithm trainer.
	 *
	 * @param algTag the algorithm tag
	 * @param dataSeries the chosen data series
	 * @param metric the used metric
	 * @param reputation the used reputation metric
	 * @param expList the considered train data
	 */
	public ConfigurationSelectorTrainer(LearnerType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, List<BasicConfiguration> basicConfigurations, String datasetName, int kfold, Metric[] validationMetrics) {
		super(algTag, dataSeries, metric, reputation, kList, datasetName, kfold, validationMetrics);
		configurations = confCloneAndComplete(basicConfigurations);
	}
	
	public ConfigurationSelectorTrainer(LearnerType algTag, DataSeries dataSeries, List<Knowledge> kList, MetaData mData, List<BasicConfiguration> confList, Metric[] validationMetrics) {
		super(algTag, dataSeries, mData.getTargetMetric(), mData.getReputation(), kList, mData.getDatasetName(), mData.getKfold(), mData, validationMetrics);
		configurations = confList;
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
	protected ObjectPair<Map<Knowledge, List<AlgorithmResult>>, Double> lookForBestConfiguration() {
		Map<Knowledge, List<AlgorithmResult>> trainResult = new HashMap<>();
		ValueSeries vs = null;
		List<AlgorithmResult> resultList = null;
		double bestScore = Double.NaN;
		try {			
			/* Iterates for Configurations */
			for(BasicConfiguration conf : configurations){
				
				AppLogger.logInfo(getClass(), "Analyzing configuration: '" + conf.toString() + "'");
				
				/* Iterates for K-Fold. Not needed for meta-learning, which internally k-folds base learners. */
				if(getAlgType() instanceof MetaLearner){
					BasicConfiguration currentConf = (BasicConfiguration)conf.clone();
					// eventually has metadata
					DetectionAlgorithm algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), currentConf);
					
					/* Automatic Training */
					boolean trainingResult = false;
					if(algorithm instanceof AutomaticTrainingAlgorithm) {
						trainingResult = ((AutomaticTrainingAlgorithm)algorithm).automaticTraining(kList);
					}
					
					if(trainingResult){
						vs = algorithm.getLoggedScores();
						resultList = new ArrayList<>();
						for(Knowledge know : kList){
							resultList.addAll(calculateResults(algorithm, know));
						}
						ObjectPair<String, Double> value = electBestDecisionFunction(algorithm, resultList, vs);
						if(value != null && (!Double.isFinite(bestScore) || getMetric().compareResults(value.getValue(), bestScore) > 0)){
							currentConf.addItem(BasicConfiguration.THRESHOLD, value.getKey());
							algorithm.setDecisionFunction(value.getKey());
							bestAlgorithm = algorithm;
							bestScore = value.getValue();
							bestConf = currentConf;
						}
					}
				} else {
					for(Map<String, List<Knowledge>> knMap : getKnowledgeList()){
						BasicConfiguration currentConf = (BasicConfiguration)conf.clone();
						DetectionAlgorithm algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), currentConf);
						
						/* Automatic Training */
						boolean trainingResult = false;
						if(algorithm instanceof AutomaticTrainingAlgorithm) {
							trainingResult = ((AutomaticTrainingAlgorithm)algorithm).automaticTraining(knMap.get("TRAIN"));
						}
						
						vs = algorithm.getLoggedScores();
						
						/* If training succeeded */
						if(!(algorithm instanceof AutomaticTrainingAlgorithm) || trainingResult){
							
							/* Calculates Algorithm Scores (just numbers, no threshold applied) */
							resultList = new ArrayList<>();
							for(Knowledge know : knMap.get("TEST")){
								resultList.addAll(calculateResults(algorithm, know));
							}
							
							ObjectPair<String, Double> value = electBestDecisionFunction(algorithm, resultList, vs);
							if(value != null && value.getKey() != null && (!Double.isFinite(bestScore) || getMetric().compareResults(value.getValue(), bestScore) > 0)){
								currentConf.addItem(BasicConfiguration.THRESHOLD, value.getKey());
								algorithm.setDecisionFunction(value.getKey());
								bestAlgorithm = algorithm;
								bestScore = value.getValue();
								bestConf = currentConf;
							}
						}	/* end if */						
					} /* end kfold for */
				} /* end else */
			} /* end conf for */
			
			if(getAlgType() instanceof MetaLearner){
				ObjectPair<String, Double> value = linearSearchOptimalSingleThreshold("STATIC_THRESHOLD_GREATER", vs, vs.getMin(), vs.getMax(), 0, resultList);
				if(value != null && (!Double.isFinite(bestScore) || getMetric().compareResults(value.getValue(), bestScore) > 0)){
					bestScore = value.getValue();
					bestConf.addItem(BasicConfiguration.THRESHOLD, value.getKey());
					bestAlgorithm.setDecisionFunction(value.getKey());
				}
				value = linearSearchOptimalSingleThreshold("STATIC_THRESHOLD_LOWER", vs, vs.getMin(), vs.getMax(), 0, resultList);
				if(value != null && (!Double.isFinite(bestScore) || getMetric().compareResults(value.getValue(), bestScore) > 0)){
					bestScore = value.getValue();
					bestConf.addItem(BasicConfiguration.THRESHOLD, value.getKey());
					bestAlgorithm.setDecisionFunction(value.getKey());
				}
			}
			
			// Final Operations, assume 'algorithm', 'vs' and 'bestConf' are set
			if(vs != null && bestConf != null) {
				bestConf.addItem(BasicConfiguration.TRAIN_AVG, vs.getAvg());
				bestConf.addItem(BasicConfiguration.TRAIN_STD, vs.getStd());
				bestConf.addItem(BasicConfiguration.TRAIN_Q0, vs.getMin());
				bestConf.addItem(BasicConfiguration.TRAIN_Q1, vs.getQ1());
				bestConf.addItem(BasicConfiguration.TRAIN_Q2, vs.getMedian());
				bestConf.addItem(BasicConfiguration.TRAIN_Q3, vs.getQ3());
				bestConf.addItem(BasicConfiguration.TRAIN_Q4, vs.getMax());
			}
			if(bestAlgorithm != null){
				for(Knowledge know : kList){
					trainResult.put(know, calculateResults(bestAlgorithm, know));
				}
			} else
				bestAlgorithm = null;
					
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone configuration");
		}
		return new ObjectPair<Map<Knowledge, List<AlgorithmResult>>, Double>(trainResult, bestScore);
	}
	
	private ObjectPair<String, Double> linearSearchOptimalSingleThreshold(String thrCode, ValueSeries scores, double thrLeft, double thrRight, int iteration, List<AlgorithmResult> resultList){
		try {
			double thrValue = (thrRight + thrLeft)/2;
		String threshold = thrCode + "(" + AppUtility.formatDouble(thrValue) + ")";
		List<AlgorithmResult> updatedList = updateResultWithDecision(DecisionFunction.buildDecisionFunction(scores, threshold, false), resultList);
		double mScore = getMetric().evaluateAnomalyResults(updatedList);
		if(iteration <= LINEAR_SEARCH_MAX_ITERATIONS){
			ObjectPair<String, Double> leftBest = linearSearchOptimalSingleThreshold(thrCode, scores, thrLeft, thrValue, iteration + 1, resultList);
			ObjectPair<String, Double> rightBest = linearSearchOptimalSingleThreshold(thrCode, scores, thrValue, thrRight, iteration + 1, resultList);
			if(leftBest != null && rightBest != null){
				if(getMetric().compareResults(mScore, leftBest.getValue()) > 0  && getMetric().compareResults(mScore, rightBest.getValue()) > 0){
					return new ObjectPair<String, Double>(threshold, mScore);
				} else {
					if(getMetric().compareResults(leftBest.getValue(), rightBest.getValue()) > 0)
						return leftBest;
					else return rightBest;
				} 
			} else return null;
		} else return new ObjectPair<String, Double>(threshold, mScore);
		} catch(Exception ex){
			return null;
		}
	}
	
	public void saveAlgorithmScores(){
		if(bestAlgorithm != null)
			bestAlgorithm.saveLoggedScores();
		else AppLogger.logError(getClass(), "ConfSaveError", "Unable to save Train Result");
	}
	
}
