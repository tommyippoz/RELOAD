/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.List;

/**
 * The Class FalsePositiveRate_Metric. Implements a metric dependent on the
 * false positive rate FP/(FP+TN)
 *
 * @author Tommy
 */
public class FalsePositiveRate_Metric extends BetterMinMetric {

	public FalsePositiveRate_Metric(double noPredTHR) {
		super(MetricType.FPR, noPredTHR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.
	 * multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public MetricResult evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations, ConfusionMatrix confusionMatrix) {
		double tn = new TN_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		double fp = new FP_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		if (tn + fp > 0)
			return new DoubleMetricResult(1.0 * fp / (fp + tn));
		else return new DoubleMetricResult(0.0);
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
