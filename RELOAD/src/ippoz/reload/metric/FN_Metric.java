/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.commons.support.TimedResult;

/**
 * The Class FN_Metric. Implements a metric based on false negatives.
 *
 * @author Tommy
 */
public class FN_Metric extends ClassificationMetric {

	/**
	 * Instantiates a new fn_ metric.
	 *
	 * @param absolute
	 *            the absolute flag
	 */
	public FN_Metric(boolean absolute, boolean validAfter) {
		super(MetricType.FN, absolute, validAfter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "False Negatives";
	}

	@Override
	public String getMetricShortName() {
		return "FN";
	}

	@Override
	protected int classifyMetric(TimedResult tResult) {
		if (tResult.getInjectedElement() != null && !Metric.anomalyTrueFalse(tResult.getValue())) {
			return 1;
		} else return 0;
	}

}
