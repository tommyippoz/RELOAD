/**
 * 
 */
package ippoz.multilayer.detector.reputation;

import ippoz.multilayer.detector.algorithm.DetectionAlgorithm;
import ippoz.multilayer.detector.commons.data.Snapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class Reputation.
 * Needs to be extended from concrete reputation classes.
 *
 * @author Tommy
 */
public abstract class Reputation {
	
	/** The reputation tag. */
	private String reputationTag;

	/**
	 * Instantiates a new reputation.
	 *
	 * @param reputationTag the reputation tag
	 */
	public Reputation(String reputationTag) {
		this.reputationTag = reputationTag;
	}

	/**
	 * Gets the reputation tag.
	 *
	 * @return the reputation tag
	 */
	public String getReputationTag(){
		return reputationTag;
	}
	
	/**
	 * Evaluates the reputation of a given detection algorithm in a specific experiment.
	 *
	 * @param alg the algorithm
	 * @param expData the experiment data
	 * @return the computed reputation
	 */
	public double evaluateReputation(DetectionAlgorithm alg, LinkedList<Snapshot> snapList){
		Snapshot currentSnapshot;
		HashMap<Date, Double> anomalyEvaluations = new HashMap<Date, Double>();
		for(int i=0;i<snapList.size();i++){
			currentSnapshot = snapList.get(i);
			anomalyEvaluations.put(currentSnapshot.getTimestamp(), alg.snapshotAnomalyRate(currentSnapshot));
		}
		return evaluateExperimentReputation(snapList, anomalyEvaluations);
	}

	/**
	 * Votes experiment reputation.
	 *
	 * @param snapList the list of snapshots
	 * @param anomalyEvaluations the anomaly evaluations of each snapshot
	 * @return the final reputation
	 */
	protected abstract double evaluateExperimentReputation(LinkedList<Snapshot> snapList, HashMap<Date, Double> anomalyEvaluations);
	
}
