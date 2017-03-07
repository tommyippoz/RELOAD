/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

// TODO: Auto-generated Javadoc
/**
 * The Class Custom_Metric.
 * Identifies a custom metric, that is user-built and not well-known at the state of the art.
 *
 * @author Tommy
 */
public class Custom_Metric extends BetterMaxMetric {

	public Custom_Metric(boolean validAfter) {
		super(validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(LinkedList<Snapshot> snapList, HashMap<Date, Double> anomalyEvaluations) {
		double p = new Precision_Metric(isValidAfter()).evaluateAnomalyResults(snapList, anomalyEvaluations);
		double r = new Recall_Metric(isValidAfter()).evaluateAnomalyResults(snapList, anomalyEvaluations);
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
