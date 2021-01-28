/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class GMean_Metric extends BetterMaxMetric {

	public GMean_Metric(boolean validAfter) {
		super(MetricType.GMEAN, validAfter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.
	 * multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public MetricResult evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations) {
		double fpr = new FalsePositiveRate_Metric(isValidAfter()).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		double r = new Recall_Metric(isValidAfter()).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		fpr = 1 - fpr;
		return new DoubleMetricResult(Math.sqrt(fpr*r));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "GMean";
	}

	@Override
	public String getMetricShortName() {
		return "GMean";
	}

}
