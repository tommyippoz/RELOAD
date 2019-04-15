/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.commons.failure.InjectedElement;

import java.util.Date;
import java.util.List;

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
	protected int classifyMetric(Date snapTime, Double anEvaluation,
			List<InjectedElement> injList) {
		if (injList.isEmpty() && Metric.anomalyTrueFalse(anEvaluation)) {
			return 1;
		} else
			return 0;
	}

}
