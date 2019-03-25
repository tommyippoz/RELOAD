/**
 * 
 */
package ippoz.madness.detector.reputation;

import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.support.TimedValue;

import java.util.ArrayList;
import java.util.List;

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
	public double evaluateReputation(DetectionAlgorithm alg, Knowledge knowledge){
		List<TimedValue> anomalyEvaluations = new ArrayList<TimedValue>(knowledge.size());
		for(int i=0;i<knowledge.size();i++){
			double algScore = DetectionAlgorithm.convertResultIntoDouble(alg.snapshotAnomalyRate(knowledge, i).getScoreEvaluation());
			anomalyEvaluations.add(new TimedValue(knowledge.getTimestamp(i), algScore));
		}
		return evaluateExperimentReputation(knowledge, anomalyEvaluations);
	}

	/**
	 * Votes experiment reputation.
	 *
	 * @param knowledge the list of snapshots
	 * @param anomalyEvaluations the anomaly evaluations of each snapshot
	 * @return the final reputation
	 */
	protected abstract double evaluateExperimentReputation(Knowledge knowledge, List<TimedValue> anomalyEvaluations);
	
}
