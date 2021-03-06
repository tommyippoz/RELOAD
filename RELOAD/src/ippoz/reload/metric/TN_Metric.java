/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * The Class TN_Metric. Implements a metric based on true negatives.
 *
 * @author Tommy
 */
public class TN_Metric extends ClassificationMetric {

	/**
	 * Instantiates a new tn_ metric.
	 *
	 * @param absolute
	 *            the absolute flag
	 */
	public TN_Metric(boolean absolute, boolean validAfter) {
		super(MetricType.TN, absolute, validAfter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "True Negatives";
	}

	@Override
	public String getMetricShortName() {
		return "TN";
	}

	@Override
	protected int classifyMetric(AlgorithmResult tResult) {
		if (!tResult.hasInjection() && !tResult.getBooleanScore()) {
			return 1;
		} else return 0;
	}

}
