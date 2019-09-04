/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.commons.support.TimedResult;

/**
 * The Class FP_Metric. Implements a metric based on the false positives.
 *
 * @author Tommy
 */
public class FP_Metric extends ClassificationMetric {

	/**
	 * Instantiates a new fp_ metric.
	 *
	 * @param absolute
	 *            the absolute flag
	 */
	public FP_Metric(boolean absolute, boolean validAfter) {
		super(MetricType.FP, absolute, validAfter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "False Positives";
	}

	@Override
	public String getMetricShortName() {
		return "FP";
	}

	@Override
	protected int classifyMetric(TimedResult tResult) {
		if (tResult.getInjectedElement() == null && Metric.anomalyTrueFalse(tResult.getValue())) {
			return 1;
		} else return 0;
	}

}
