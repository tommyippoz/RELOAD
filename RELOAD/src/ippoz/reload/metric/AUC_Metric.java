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
public class AUC_Metric extends BetterMaxMetric {

	public AUC_Metric() {
		super(MetricType.AUC);
		// TODO Auto-generated constructor stub
	}
	
	public AUC_Metric(double noPredTHR) {
		super(MetricType.AUC, noPredTHR);
		// TODO Auto-generated constructor stub
	}

	@Override
	public MetricResult evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations, ConfusionMatrix confusionMatrix) {
		double tpr = new TruePositiveRate_Metric(getNoPredictionThreshold())
				.evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		double fpr = new FalsePositiveRate_Metric(getNoPredictionThreshold())
				.evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		double auc = (tpr * fpr) / 2 + (tpr + 1) * (1 - fpr) / 2;
		return new DoubleMetricResult(auc);
	}

	@Override
	public String getMetricName() {
		return "Area Under ROC Curve";
	}

	@Override
	public String getMetricShortName() {
		return "AUC";
	}

}
