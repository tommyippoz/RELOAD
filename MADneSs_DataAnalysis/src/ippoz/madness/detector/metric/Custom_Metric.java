/**
 * 
 */
package ippoz.madness.detector.metric;

import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.support.TimedValue;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class Custom_Metric.
 * Identifies a custom metric, that is user-built and not well-known at the state of the art.
 *
 * @author Tommy
 */
public class Custom_Metric extends BetterMaxMetric {

	public Custom_Metric(boolean validAfter) {
		super(null, validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		double p = new Precision_Metric(isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double r = new Recall_Metric(isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		if(p + r > 0)
			return 1.25*p*r/(0.25*p+r);
		else return 0.0;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "Custom";
	}

}
