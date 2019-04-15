/**
 * 
 */
package ippoz.madness.detector.trainer;

import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.reputation.Reputation;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;

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
