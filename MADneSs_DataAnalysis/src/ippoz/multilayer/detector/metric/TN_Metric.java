/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.failure.InjectedElement;

import java.util.Date;
import java.util.LinkedList;

/**
 * The Class TN_Metric.
 * Implements a metric based on true negatives.
 *
 * @author Tommy
 */
public class TN_Metric extends ClassificationMetric {
	
	/**
	 * Instantiates a new tn_ metric.
	 *
	 * @param absolute the absolute flag
	 */
	public TN_Metric(boolean absolute, boolean validAfter) {
		super(absolute, validAfter);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "True Negatives";
	}

	@Override
	protected int classifyMetric(Date snapTime, Double anEvaluation, LinkedList<InjectedElement> injList) {
		if(injList.isEmpty() && !Metric.anomalyTrueFalse(anEvaluation)){
			return 1;
		} else return 0;
	}

}
