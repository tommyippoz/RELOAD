/**
 * 
 */
package ippoz.reload.metric;

import java.util.List;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

/**
 * @author tomma
 *
 */
public class Recall_Unk_Metric extends BetterMaxMetric {

	public Recall_Unk_Metric(double noPredictionTHR) {
		super(MetricType.REC_UNK, noPredictionTHR);
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
		double tp = new TP_Unk_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		double fn = new FN_Unk_Metric(true, getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
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
		return "Rec-Unk";
	}

	@Override
	public String getMetricShortName() {
		return "RU";
	}

}
