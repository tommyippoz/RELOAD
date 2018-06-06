/**
 * 
 */
package ippoz.madness.detector.trainer;

import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.reputation.Reputation;

import java.util.List;

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
	 * @param expList the considered train data
	 * @param configurations the possible configurations
	 */
	public FixedConfigurationTrainer(AlgorithmType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, AlgorithmConfiguration configuration) {
		super(algTag, dataSeries, metric, reputation, kList);
		try {
			fixConf = (AlgorithmConfiguration) configuration.clone();
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to train '" + algTag + "' algorithm");
		}
	}

	@Override
	protected AlgorithmConfiguration lookForBestConfiguration() {
		return fixConf;
	}
	
}
