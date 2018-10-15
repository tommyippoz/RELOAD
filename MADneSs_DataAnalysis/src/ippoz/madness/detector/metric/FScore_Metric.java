/**
 * 
 */
package ippoz.madness.detector.metric;

import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.support.TimedValue;

import java.util.List;

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
		super(MetricType.FSCORE, validAfter);
		this.beta = beta;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		double p = new Precision_Metric(isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double r = new Recall_Metric(isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
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
	
	@Override
	public String getMetricShortName() {
		return "F" + (int)beta;
	}

}
