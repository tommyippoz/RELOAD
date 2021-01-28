/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.List;

/**
 * The Class FMeasure_Metric. Implements the F-Measure (= F-Score(1))
 *
 * @author Tommy
 */
public class FMeasure_Metric extends BetterMaxMetric {

	public FMeasure_Metric(boolean validAfter) {
		super(MetricType.FMEASURE, validAfter);
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
		double p = new Precision_Metric(isValidAfter()).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		double r = new Recall_Metric(isValidAfter()).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		if (p + r > 0)
			return new DoubleMetricResult(2.0 * p * r / (p + r));
		else return new DoubleMetricResult(0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "F-Measure";
	}

	@Override
	public String getMetricShortName() {
		return "F1";
	}

}
