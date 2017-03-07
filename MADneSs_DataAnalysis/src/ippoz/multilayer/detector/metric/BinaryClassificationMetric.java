/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.failure.InjectedElement;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public abstract class BinaryClassificationMetric extends BetterMaxMetric {
	
	/** The absolute flag. */
	private boolean absolute;

	/**
	 * Instantiates a new binary classification metric.
	 *
	 * @param absolute the absolute flag
	 * @param absolute the validAfter flag
	 */
	public BinaryClassificationMetric(boolean absolute, boolean validAfter) {
		super(validAfter);
		this.absolute = absolute;
	}

	@Override
	public double evaluateAnomalyResults(LinkedList<Snapshot> snapList, HashMap<Date, Double> anomalyEvaluations) {
		int detectionHits = 0;
		int undetectable = 0;
		Snapshot snap;
		InjectedElement injEl = null; 
		for(int i=0;i<snapList.size();i++){
			snap = snapList.get(i);
			if(injEl == null && snap.getInjectedElement() != null)
				injEl = snap.getInjectedElement();
			if(isValidSnapshot(snap, injEl)){
				if(classifyMetric(snap, anomalyEvaluations.get(snap.getTimestamp())))
					detectionHits++;
			} else undetectable++;
		}
		if(snapList.size() > 0){
			if(!absolute)
				return 1.0*detectionHits/(snapList.size()-undetectable);
			else return detectionHits;
		} else return 0.0;
	}

	protected abstract boolean classifyMetric(Snapshot snap, Double anEvaluation);

}
