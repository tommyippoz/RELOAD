/**
 * 
 */
package ippoz.multilayer.detector.trainer;

import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.performance.TrainingTiming;
import ippoz.multilayer.detector.reputation.Reputation;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class FixedConfigurationTrainer.
 * Used by algorithms which accept only one configuration.
 *
 * @author Tommy
 */
public class FixedConfigurationTrainer extends AlgorithmTrainer {

	/** The fixed (unique) configuration. */
	private AlgorithmConfiguration fixConf;
	
	/**
	 * Instantiates a new algorithm trainer.
	 *
	 * @param algTag the algorithm tag
	 * @param dataSeries the chosen data series
	 * @param metric the used metric
	 * @param reputation the used reputation metric
	 * @param trainData the considered train data
	 * @param configurations the possible configurations
	 */
	public FixedConfigurationTrainer(AlgorithmType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, TrainingTiming tTiming, LinkedList<ExperimentData> trainData, AlgorithmConfiguration configuration) {
		super(algTag, dataSeries, metric, reputation, tTiming, trainData);
		try {
			fixConf = (AlgorithmConfiguration) configuration.clone();
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to train '" + algTag + "' algorithm");
		}
	}

	@Override
	protected AlgorithmConfiguration lookForBestConfiguration(HashMap<String, LinkedList<Snapshot>> algExpSnapshots,  TrainingTiming tTiming) {
		tTiming.addTrainingTime(getAlgType(), 0, 1);
		return fixConf;
	}
	
}
