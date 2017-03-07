/**
 * 
 */
package ippoz.multilayer.detector.reputation;

import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.metric.TP_Metric;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class BetaReputation.
 * Constructs a reputation using the Beta calculation.
 *
 * @author Tommy
 */
public class BetaReputation extends Reputation {
	
	private boolean validAfter;

	/**
	 * Instantiates a new Beta reputation.
	 *
	 * @param reputationTag the reputation tag
	 */
	public BetaReputation(String reputationTag, boolean validAfter) {
		super(reputationTag);
		this.validAfter = validAfter;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.reputation.Reputation#evaluateExperimentReputation(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateExperimentReputation(LinkedList<Snapshot> snapList, HashMap<Date, Double> anomalyEvaluations) {
		double tp = new TP_Metric(true, validAfter).evaluateAnomalyResults(snapList, anomalyEvaluations);
		double nInj = countInjections(snapList);
		double alpha = tp + 1;
		double beta = nInj + 1;
		return alpha*1.0/(alpha + beta);
	}
	
	private int countInjections(LinkedList<Snapshot> snapList){
		int count = 0;
		for(Snapshot snap : snapList){
			if(snap.getInjectedElement() != null)
				count++;
		}
		return count;
	}

}
