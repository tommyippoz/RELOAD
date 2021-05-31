/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.List;

/**
 * The Class Recall_Metric. Implements a metric based on Recall.
 *
 * @author Tommy
 */
public class Recall_Metric extends BetterMaxMetric {

	public Recall_Metric(double noPredTHR) {
		super(MetricType.RECALL, noPredTHR);
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
		double fn = new FN_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		if (tp + fn > 0)
			return new DoubleMetricResult(1.0 * tp / (tp + fn));
		else return new DoubleMetricResult(0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "Recall";
	}

	@Override
	public String getMetricShortName() {
		return "R";
	}

}
