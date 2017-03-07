/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class FScore_Metric.
 * Implements the F-Score(b) metric
 *
 * @author Tommy
 */
public class FScore_Metric extends BetterMaxMetric {

	/** The beta parameter. */
	private double beta;
	
	/**
	 * Instantiates a new fscore_metric.
	 *
	 * @param beta the beta parameter of f-score
	 */
	public FScore_Metric(double beta, boolean validAfter){
		super(validAfter);
		this.beta = beta;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(LinkedList<Snapshot> snapList, HashMap<Date, Double> anomalyEvaluations) {
		double p = new Precision_Metric(isValidAfter()).evaluateAnomalyResults(snapList, anomalyEvaluations);
		double r = new Recall_Metric(isValidAfter()).evaluateAnomalyResults(snapList, anomalyEvaluations);
		if(p + r > 0)
			return (1+beta*beta)*p*r/(beta*beta*p+r);
		else return 0.0;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "FScore(" + beta + ")";
	}

}
