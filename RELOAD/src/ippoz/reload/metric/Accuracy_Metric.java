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
public class Accuracy_Metric extends BetterMaxMetric {

	public Accuracy_Metric(boolean validAfter) {
		super(MetricType.ACCURACY, validAfter);
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
		if (tp + fp + tn + fn > 0)
			return 1.0 * (tn + tp) / (tp + fp + tn + fn);
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
		return "Accuracy";
	}

	@Override
	public String getMetricShortName() {
		return "ACC";
	}

}
