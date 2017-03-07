/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;

/**
 * The Class TN_Metric.
 * Implements a metric based on true negatives.
 *
 * @author Tommy
 */
public class TN_Metric extends BinaryClassificationMetric {
	
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
	protected boolean classifyMetric(Snapshot snap, Double anEvaluation) {
		if(snap.getInjectedElement() == null || !snap.getInjectedElement().happensAt(snap.getTimestamp())){	
			if(!Metric.anomalyTrueFalse(anEvaluation))
				return true;
		}
		return false;
	}

}
