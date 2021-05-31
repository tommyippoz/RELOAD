/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.List;

/**
 * The Class Precision_Metric. Implements a metric based on Precision.
 *
 * @author Tommy
 */
public class Precision_Metric extends BetterMaxMetric {

	public Precision_Metric(double noPredTHR) {
		super(MetricType.PRECISION, noPredTHR);
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
		double tp = new TP_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		double fp = new FP_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		if (tp + fp > 0)
			return new DoubleMetricResult(1.0 * tp / (tp + fp));
		else
			return new DoubleMetricResult(0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "Precision";
	}

	@Override
	public String getMetricShortName() {
		return "P";
	}

}