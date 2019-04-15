/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.TimedValue;

import java.util.List;

/**
 * The Class FalsePositiveRate_Metric. Implements a metric dependent on the
 * false positive rate FP/(FP+TN)
 *
 * @author Tommy
 */
public class FalsePositiveRate_Metric extends BetterMinMetric {

	public FalsePositiveRate_Metric(boolean validAfter) {
		super(MetricType.FPR, validAfter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.
	 * multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(Knowledge knowledge,
			List<TimedValue> anomalyEvaluations) {
		double tn = new TN_Metric(true, isValidAfter()).evaluateAnomalyResults(
				knowledge, anomalyEvaluations);
		double fp = new FP_Metric(true, isValidAfter()).evaluateAnomalyResults(
				knowledge, anomalyEvaluations);
		if (tn + fp > 0)
			return 1.0 * fp / (fp + tn);
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
		return "False Positive Rate";
	}

	@Override
	public String getMetricShortName() {
		return "FPR";
	}

}
