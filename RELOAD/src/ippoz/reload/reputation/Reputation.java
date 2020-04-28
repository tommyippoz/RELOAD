/**
 * 
 */
package ippoz.reload.reputation;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.metric.Metric;

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
		List<AlgorithmResult> anomalyEvaluations = new ArrayList<AlgorithmResult>(knowledge.size());
		for(int i=0;i<knowledge.size();i++){
			AlgorithmResult aRes = alg.snapshotAnomalyRate(knowledge, i);
			double algScore = DetectionAlgorithm.convertResultIntoDouble(aRes.getScoreEvaluation());
			anomalyEvaluations.add(new AlgorithmResult(aRes.getData(), knowledge.getInjection(i), algScore, aRes.getScoreEvaluation(), aRes.getDecisionFunction(), alg.getConfidence(algScore)));
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
	protected abstract double evaluateExperimentReputation(List<AlgorithmResult> anomalyEvaluations);

	public static Reputation fromString(String reputationType, Metric metric, boolean validAfter) {
		switch(reputationType.toUpperCase()){
			case "BETA":
				return new BetaReputation(reputationType, validAfter);
			case "METRIC":
				return new MetricReputation(reputationType, metric);
			default:
				if(AppUtility.isNumber(reputationType))
					return new ConstantReputation(reputationType, Double.parseDouble(reputationType));
				else {
					//AppLogger.logError(Reputation.class, "MissingPreferenceError", "Reputation cannot be defined");
					return null;
				}
		}
	}
	
}
