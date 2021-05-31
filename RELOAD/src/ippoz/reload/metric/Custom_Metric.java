/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class Custom_Metric. Identifies a custom metric, that is user-built and
 * not well-known at the state of the art.
 *
 * @author Tommy
 */
public class Custom_Metric extends BetterMaxMetric {

	public Custom_Metric(double noPredTHR) {
		super(MetricType.CUSTOM, noPredTHR);
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
		double ce = new ConfidenceErrorMetric(getNoPredictionThreshold(), 0.5).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		double acc = new Accuracy_Metric(getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		return new DoubleMetricResult(Math.abs(ce)*acc);
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
