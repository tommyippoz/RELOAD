/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.failure.InjectedElement;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public abstract class ClassificationMetric extends BetterMaxMetric {
	
	/** The absolute flag. */
	private boolean absolute;

	/**
	 * Instantiates a new binary classification metric.
	 *
	 * @param absolute the absolute flag
	 * @param absolute the validAfter flag
	 */
	public ClassificationMetric(boolean absolute, boolean validAfter) {
		super(validAfter);
		this.absolute = absolute;
	}

	@Override
	public double evaluateAnomalyResults(List<Snapshot> snapList, Map<Date, Double> anomalyEvaluations) {
		int detectionHits = 0;
		Snapshot snap;
		LinkedList<InjectedElement> overallInj = new LinkedList<InjectedElement>(); 
		LinkedList<InjectedElement> currentInj = new LinkedList<InjectedElement>(); 
		for(int i=0;i<snapList.size();i++){
			snap = snapList.get(i);
			while(!currentInj.isEmpty() && currentInj.getFirst().getFinalTimestamp().before(snap.getTimestamp())){
				currentInj.removeFirst();
			}
			if(snap.getInjectedElement() != null){
				overallInj.add(snap.getInjectedElement());
				currentInj.add(snap.getInjectedElement());
			}
			detectionHits = detectionHits + classifyMetric(snap.getTimestamp(), anomalyEvaluations.get(snap.getTimestamp()), currentInj);
		}
		if(snapList.size() > 0){
			if(!absolute)
				return 1.0*detectionHits/(snapList.size() - getUndetectable(overallInj));
			else return detectionHits;
		} else return 0.0;
	}
	
	private int getUndetectable(LinkedList<InjectedElement> injList){
		int undetectable = 0;
		LinkedList<InjectedElement> current;
		while(!injList.isEmpty()){
			current = new LinkedList<InjectedElement>();
			current.add(injList.removeFirst());
			while(!injList.isEmpty() && current.getLast().compliesWith(injList.getFirst())){
				current.add(injList.removeFirst());
			}
			undetectable = undetectable + ((int)(current.getLast().getFinalTimestamp().getTime() - current.getFirst().getTimestamp().getTime())/1000 - current.size());
		}
		return undetectable;
	}
	
/*	@Override
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
	}*/

	protected abstract int classifyMetric(Date snapTime, Double anEvaluation, LinkedList<InjectedElement> injList);

}
