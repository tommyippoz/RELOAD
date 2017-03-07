/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;

/**
 * The Class FP_Metric.
 * Implements a metric based on the false positives.
 *
 * @author Tommy
 */
public class FP_Metric extends BinaryClassificationMetric {
	
	/**
	 * Instantiates a new fp_ metric.
	 *
	 * @param absolute the absolute flag
	 */
	public FP_Metric(boolean absolute, boolean validAfter) {
		super(absolute, validAfter);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "False Positives";
	}

	@Override
	protected boolean classifyMetric(Snapshot snap, Double anEvaluation) {
		if(snap.getInjectedElement() == null || !snap.getInjectedElement().happensAt(snap.getTimestamp())){
			if(Metric.anomalyTrueFalse(anEvaluation))
				return true;
		}
		return false;
	}

}
