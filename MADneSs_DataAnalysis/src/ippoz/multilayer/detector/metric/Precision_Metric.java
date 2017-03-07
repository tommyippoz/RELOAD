/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class Precision_Metric.
 * Implements a metric based on Precision.
 *
 * @author Tommy
 */
public class Precision_Metric extends BetterMaxMetric {

	public Precision_Metric(boolean validAfter) {
		super(validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(LinkedList<Snapshot> snapList, HashMap<Date, Double> anomalyEvaluations) {
		double tp = new TP_Metric(true, isValidAfter()).evaluateAnomalyResults(snapList, anomalyEvaluations);
		double fp = new FP_Metric(true, isValidAfter()).evaluateAnomalyResults(snapList, anomalyEvaluations);
		if(tp + fp > 0)
			return 1.0*tp/(tp+fp);
		else return 0.0;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "Precision";
	}

}