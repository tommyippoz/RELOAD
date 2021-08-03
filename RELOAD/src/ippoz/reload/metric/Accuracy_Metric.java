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
public class Accuracy_Metric extends BetterMaxMetric {

	public Accuracy_Metric(double noPredTHR) {
		super(MetricType.ACCURACY, noPredTHR);
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
		double tp = new TP_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		double fp = new FP_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		double tn = new TN_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		double fn = new FN_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		if (tp + fp + tn + fn > 0)
			return new DoubleMetricResult(1.0 * (tn + tp) / (tp + fp + tn + fn));
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
		return "Accuracy";
	}

	@Override
	public String getMetricShortName() {
		return "ACC";
	}

}
