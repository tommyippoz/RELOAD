/**
 * 
 */
package ippoz.madness.detector.metric;

import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.TimedValue;

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
	public double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		double tpr = new TruePositiveRate_Metric(isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double fpr = new FalsePositiveRate_Metric(isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double auc = (tpr * fpr)/2 + (tpr+1)*(1-fpr)/2;
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
