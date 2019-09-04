/**
 * 
 */
package ippoz.reload.reputation;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.TimedResult;

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
		List<TimedResult> anomalyEvaluations = new ArrayList<TimedResult>(knowledge.size());
		for(int i=0;i<knowledge.size();i++){
			AlgorithmResult aRes = alg.snapshotAnomalyRate(knowledge, i);
			double algScore = DetectionAlgorithm.convertResultIntoDouble(aRes.getScoreEvaluation());
			anomalyEvaluations.add(new TimedResult(knowledge.getTimestamp(i), algScore, aRes.getScore(), knowledge.getInjection(i)));
		}
		return evaluateExperimentReputation(anomalyEvaluations);
	}

	/**
	 * Votes experiment reputation.
	 *
	 * @param knowledge the list of snapshots
	 * @param anomalyEvaluations the anomaly evaluations of each snapshot
	 * @return the final reputation
	 */
	protected abstract double evaluateExperimentReputation(List<TimedResult> anomalyEvaluations);
	
}
