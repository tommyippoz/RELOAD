/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.commons.support.TimedResult;

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
	protected int classifyMetric(TimedResult tResult) {
		if (tResult.getInjectedElement() != null && Metric.anomalyTrueFalse(tResult.getValue())) {
			return 1;
		} else return 0;
	}

}
