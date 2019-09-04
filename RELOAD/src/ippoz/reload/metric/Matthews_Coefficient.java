/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.commons.support.TimedResult;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class Matthews_Coefficient extends BetterMaxMetric {

	public Matthews_Coefficient(boolean validAfter) {
		super(MetricType.MATTHEWS, validAfter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.
	 * multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(List<TimedResult> anomalyEvaluations) {
		double tp = new TP_Metric(true, isValidAfter()).evaluateAnomalyResults(anomalyEvaluations);
		double fp = new FP_Metric(true, isValidAfter()).evaluateAnomalyResults(anomalyEvaluations);
		double tn = new TN_Metric(true, isValidAfter()).evaluateAnomalyResults(anomalyEvaluations);
		double fn = new FN_Metric(true, isValidAfter()).evaluateAnomalyResults(anomalyEvaluations);
		double num = tp * tn - fp * fn;
		if (num != 0.0)
			return 1.0 * num
					/ Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));
		else
			return 0.0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "MatthewsCorrelationCoefficient";
	}

	@Override
	public String getMetricShortName() {
		return "MCC";
	}

}
