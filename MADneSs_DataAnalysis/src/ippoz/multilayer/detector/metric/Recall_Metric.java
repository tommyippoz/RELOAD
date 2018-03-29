/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.knowledge.Knowledge;
import ippoz.multilayer.detector.commons.support.TimedValue;

import java.util.List;

/**
 * The Class Recall_Metric.
 * Implements a metric based on Recall.
 *
 * @author Tommy
 */
public class Recall_Metric extends BetterMaxMetric {

	public Recall_Metric(boolean validAfter) {
		super(validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		double tp = new TP_Metric(true, isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double fn = new FN_Metric(true, isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		if(tp + fn > 0)
			return 1.0*tp/(tp+fn);
		else return 0.0;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "Recall";
	}

}
