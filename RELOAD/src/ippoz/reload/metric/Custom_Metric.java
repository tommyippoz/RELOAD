/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class Custom_Metric. Identifies a custom metric, that is user-built and
 * not well-known at the state of the art.
 *
 * @author Tommy
 */
public class Custom_Metric extends BetterMaxMetric {

	public Custom_Metric(boolean validAfter) {
		super(MetricType.CUSTOM, validAfter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.
	 * multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(List<? extends AlgorithmResult> anomalyEvaluations) {
		double ce = new ConfidenceErrorMetric(isValidAfter(), 0.5).evaluateAnomalyResults(anomalyEvaluations);
		double acc = new Accuracy_Metric(isValidAfter()).evaluateAnomalyResults(anomalyEvaluations);
		return Math.abs(ce)*acc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "Custom";
	}

	@Override
	public String getMetricShortName() {
		return "Custom";
	}

}
