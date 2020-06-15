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
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class FixedConfigurationTrainer.
 * Used by algorithms which accept only one configuration.
 *
 * @author Tommy
 */
public class FixedConfigurationTrainer extends AlgorithmTrainer {
	
	/**
	 * Instantiates a new algorithm trainer.
	 *
	 * @param algTag the algorithm tag
	 * @param dataSeries the chosen data series
	 * @param metric the used metric
	 * @param reputation the used reputation metric
	 * @param expList the considered train data
	 * @param configurations the possible configurations
	 */
	public FixedConfigurationTrainer(LearnerType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, BasicConfiguration configuration) {
		super(algTag, dataSeries, metric, reputation, kList, null, null);
		try {
			bestConf = (BasicConfiguration) configuration.clone();
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to train '" + algTag + "' algorithm");
		}
	}

	@Override
	protected ObjectPair<Map<Knowledge, List<AlgorithmResult>>, Double> lookForBestConfiguration() {
		double bestScore = Double.NaN;
		ValueSeries vs = null;
		DetectionAlgorithm algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), bestConf);
		
		/* Automatic Training */
		boolean trainingResult = false;
		if(algorithm instanceof AutomaticTrainingAlgorithm) {
			trainingResult = ((AutomaticTrainingAlgorithm)algorithm).automaticTraining(kList);
		}
		
		if(trainingResult){
			vs = algorithm.getLoggedScores();
			List<AlgorithmResult> resultList = new ArrayList<>();
			for(Knowledge know : kList){
				resultList.addAll(calculateResults(algorithm, know));
			}
			ObjectPair<String, Double> value = electBestDecisionFunction(algorithm, resultList, vs);
			bestConf.addItem(BasicConfiguration.THRESHOLD, value.getKey());
			algorithm.setDecisionFunction(value.getKey());
			bestScore = value.getValue();
		}
		
		algorithm.saveLoggedScores();
		
		// Final Operations, assume 'algorithm', 'vs' and 'bestConf' are set
		bestConf.addItem(BasicConfiguration.TRAIN_AVG, vs.getAvg());
		bestConf.addItem(BasicConfiguration.TRAIN_STD, vs.getStd());
		bestConf.addItem(BasicConfiguration.TRAIN_Q0, vs.getMin());
		bestConf.addItem(BasicConfiguration.TRAIN_Q1, vs.getQ1());
		bestConf.addItem(BasicConfiguration.TRAIN_Q2, vs.getMedian());
		bestConf.addItem(BasicConfiguration.TRAIN_Q3, vs.getQ3());
		bestConf.addItem(BasicConfiguration.TRAIN_Q4, vs.getMax());
		
		Map<Knowledge, List<AlgorithmResult>> trainResult = new HashMap<>();
		for(Knowledge know : kList){
			trainResult.put(know, calculateResults(algorithm, know));
		}
		
		return new ObjectPair<Map<Knowledge, List<AlgorithmResult>>, Double>(trainResult, bestScore);
	}
	
}
