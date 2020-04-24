/**
 * 
 */
package ippoz.reload.trainer;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.util.List;

/**
 * The Class FixedConfigurationTrainer.
 * Used by algorithms which accept only one configuration.
 *
 * @author Tommy
 */
public class FixedConfigurationTrainer extends AlgorithmTrainer {

	/** The fixed (unique) configuration. */
	private BasicConfiguration fixConf;
	
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
		super(algTag, dataSeries, metric, reputation, kList, null);
		try {
			fixConf = (BasicConfiguration) configuration.clone();
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to train '" + algTag + "' algorithm");
		}
	}

	@Override
	protected BasicConfiguration lookForBestConfiguration() {
		return fixConf;
	}
	
}
