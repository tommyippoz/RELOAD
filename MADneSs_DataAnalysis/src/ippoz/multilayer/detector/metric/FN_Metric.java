/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;

/**
 * The Class FN_Metric.
 * Implements a metric based on false negatives.
 *
 * @author Tommy
 */
public class FN_Metric extends BinaryClassificationMetric {

	/**
	 * Instantiates a new fn_ metric.
	 *
	 * @param absolute the absolute flag
	 */
	public FN_Metric(boolean absolute, boolean validAfter) {
		super(absolute, validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "False Negatives";
	}

	@Override
	protected boolean classifyMetric(Snapshot snap, Double anEvaluation) {
		if(snap.getInjectedElement() != null && snap.getInjectedElement().happensAt(snap.getTimestamp())){	
			if(!Metric.anomalyTrueFalse(anEvaluation))
				return true;
		}
		return false;
	}

}
