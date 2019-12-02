/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class AUC_Metric extends BetterMaxMetric {

	public AUC_Metric(boolean validAfter) {
		super(MetricType.AUC, validAfter);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double evaluateAnomalyResults(List<? extends AlgorithmResult> anomalyEvaluations) {
		double tpr = new TruePositiveRate_Metric(isValidAfter())
				.evaluateAnomalyResults(anomalyEvaluations);
		double fpr = new FalsePositiveRate_Metric(isValidAfter())
				.evaluateAnomalyResults(anomalyEvaluations);
		double auc = (tpr * fpr) / 2 + (tpr + 1) * (1 - fpr) / 2;
		return auc;
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
