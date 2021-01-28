/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * The Class TP_Metric. Implements a metric based on true positives.
 *
 * @author Tommy
 */
public class TP_Metric extends ClassificationMetric {

	/**
	 * Instantiates a new tp_ metric.
	 *
	 * @param absolute
	 *            the absolute flag
	 */
	public TP_Metric(boolean absolute, boolean validAfter) {
		super(MetricType.TP, absolute, validAfter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "True Positives";
	}

	@Override
	public String getMetricShortName() {
		return "TP";
	}

	@Override
	protected int classifyMetric(AlgorithmResult tResult) {
		if (tResult.isAnomalous() && tResult.getBooleanScore()) {
			return 1;
		} else return 0;
	}

}
