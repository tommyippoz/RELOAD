/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class FalsePositiveRate_Metric.
 * Implements a metric dependent on the false positive rate FP/(FP+TN)
 *
 * @author Tommy
 */
public class FalsePositiveRate_Metric extends BetterMinMetric {

	public FalsePositiveRate_Metric(boolean validAfter) {
		super(validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(LinkedList<Snapshot> snapList, HashMap<Date, Double> anomalyEvaluations) {
		double tn = new TN_Metric(true, isValidAfter()).evaluateAnomalyResults(snapList, anomalyEvaluations);
		double fp = new FP_Metric(true, isValidAfter()).evaluateAnomalyResults(snapList, anomalyEvaluations);
		if(tn + fp > 0)
			return 1.0*fp/(fp+tn);
		else return 0.0;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "False Positive Rate";
	}

}
