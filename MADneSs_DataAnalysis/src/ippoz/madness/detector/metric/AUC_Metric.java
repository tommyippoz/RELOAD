/**
 * 
 */
package ippoz.madness.detector.metric;

import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.support.TimedValue;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class AUC_Metric extends BetterMaxMetric {

	public AUC_Metric(boolean validAfter) {
		super(null, validAfter);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		double tpr = new TruePositiveRate_Metric(isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double fpr = new FalsePositiveRate_Metric(isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		return (tpr + fpr)/2;
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
