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
public class TruePositiveRate_Metric extends BetterMaxMetric {

	public TruePositiveRate_Metric() {
		super(MetricType.TPR);
	}
	
	public TruePositiveRate_Metric(double noPredTHR) {
		super(MetricType.TPR, noPredTHR);
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
			return new DoubleMetricResult(1.0 * tp / (fn + tp));
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
		return "True Positive Rate";
	}

	@Override
	public String getMetricShortName() {
		return "TPR";
	}

}
