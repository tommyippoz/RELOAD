/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;

/**
 * The Class TP_Metric.
 * Implements a metric based on true positives.
 *
 * @author Tommy
 */
public class TP_Metric extends BinaryClassificationMetric {

	/**
	 * Instantiates a new tp_ metric.
	 *
	 * @param absolute the absolute flag
	 */
	public TP_Metric(boolean absolute, boolean validAfter) {
		super(absolute, validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "True Positives";
	}

	@Override
	protected boolean classifyMetric(Snapshot snap, Double anEvaluation) {
		if(snap.getInjectedElement() != null && snap.getInjectedElement().happensAt(snap.getTimestamp())){
			if(Metric.anomalyTrueFalse(anEvaluation))
				return true;
		}
		return false;
	}

}
