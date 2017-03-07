/**
 * 
 */
package ippoz.multilayer.detector.trainer;

import ippoz.multilayer.commons.support.AppLogger;
import ippoz.multilayer.detector.algorithm.AutomaticTrainingAlgorithm;
import ippoz.multilayer.detector.algorithm.DetectionAlgorithm;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.performance.TrainingTiming;
import ippoz.multilayer.detector.reputation.Reputation;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class ConfigurationFinderTrainer.
 * This is used for finding the best configuration for algorithms which does not have a static set of configurations to try.
 *
 * @author Tommy
 */
public class ConfigurationFinderTrainer extends AlgorithmTrainer {

	/**
	 * Instantiates a new configuration finder trainer.
	 *
	 * @param algTag the algorithm tag
	 * @param dataSeries the chosen data series
	 * @param metric the scoring metric
	 * @param reputation the scoring reputation
	 * @param tTiming the training timing manager
	 * @param trainData the training data
	 */
	public ConfigurationFinderTrainer(AlgorithmType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, TrainingTiming tTiming, LinkedList<ExperimentData> trainData) {
		super(algTag, dataSeries, metric, reputation, tTiming, trainData);
	}

	@Override
	protected AlgorithmConfiguration lookForBestConfiguration(HashMap<String, LinkedList<Snapshot>> algExpSnapshots, TrainingTiming tTiming) {
		AlgorithmConfiguration bestConf;
		DetectionAlgorithm da = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), null);
		long trainTime = System.currentTimeMillis();
		if(da instanceof AutomaticTrainingAlgorithm)
			bestConf = ((AutomaticTrainingAlgorithm)da).automaticTraining(algExpSnapshots);
		else {
			AppLogger.logError(getClass(), "TrainingError", "Algorithm " + getAlgType() + " is not an automatic training algorithm");
			bestConf = null;
		}
		tTiming.addTrainingTime(getAlgType(), System.currentTimeMillis() - trainTime, 0);
		return bestConf;
	}

}
