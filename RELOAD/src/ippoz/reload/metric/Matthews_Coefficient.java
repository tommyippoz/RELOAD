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
public class Matthews_Coefficient extends BetterBigMetric {

	public Matthews_Coefficient(double noPredTHR) {
		super(MetricType.MATTHEWS, noPredTHR);
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
		double num = tp * tn - fp * fn;
		if (num != 0.0)
			return new DoubleMetricResult(1.0 * num
					/ Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn)));
		else return new DoubleMetricResult(0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "MatthewsCorrelationCoefficient";
	}

	@Override
	public String getMetricShortName() {
		return "MCC";
	}

}
