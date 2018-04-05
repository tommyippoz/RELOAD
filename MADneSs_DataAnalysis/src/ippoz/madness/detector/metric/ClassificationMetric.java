/**
 * 
 */
package ippoz.madness.detector.metric;

import ippoz.madness.detector.commons.failure.InjectedElement;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.support.TimedValue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
	public double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		int detectionHits = 0;
		List<InjectedElement> overallInj = new LinkedList<InjectedElement>(); 
		List<InjectedElement> currentInj = new LinkedList<InjectedElement>(); 
		for(int i=0;i<knowledge.size();i++){
			while(!currentInj.isEmpty() && currentInj.get(0).getFinalTimestamp().before(knowledge.getTimestamp(i))){
				currentInj.remove(0);
			}
			if(knowledge.getInjection(i) != null){
				overallInj.add(knowledge.getInjection(i));
				currentInj.add(knowledge.getInjection(i));
			}
			detectionHits = detectionHits + classifyMetric(knowledge.getTimestamp(i), anomalyEvaluations.get(i).getValue(), currentInj);
		}
		if(knowledge.size() > 0){
			if(!absolute)
				return 1.0*detectionHits/(knowledge.size() - getUndetectable(overallInj));
			else return detectionHits;
		} else return 0.0;
	}
	
	private int getUndetectable(List<InjectedElement> injList){
		int undetectable = 0;
		List<InjectedElement> current;
		while(!injList.isEmpty()){
			current = new LinkedList<InjectedElement>();
			current.add(injList.remove(0));
			while(!injList.isEmpty() && current.get(current.size()-1).compliesWith(injList.get(0))){
				current.add(injList.remove(0));
			}
			undetectable = undetectable + ((int)(current.get(current.size()-1).getFinalTimestamp().getTime() - current.get(0).getTimestamp().getTime())/1000 - current.size());
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

	protected abstract int classifyMetric(Date snapTime, Double anEvaluation, List<InjectedElement> injList);

}
