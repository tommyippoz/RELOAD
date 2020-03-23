/**
 * 
 */
package ippoz.reload.voter;

import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.decisionfunction.StaticThresholdGreaterThanDecision;

/**
 * @author Tommy
 *
 */
public class MajorityVoter extends ScoresVoter {

	public MajorityVoter(String checkerSelection, String votingStrategy) {
		super(checkerSelection, votingStrategy);
	}
	
	@Override
	public double voteResults(Knowledge know, int knowIndex, double[] individualScores) {
		double snapScore = 0.0;
		boolean undetectable = true;
		for(double algScore : individualScores){
			if(algScore >= 0.0){				
				undetectable = false;
				snapScore = snapScore + 1.0*algScore;
			}
		}
		if(undetectable)
			return -1.0;
		else return snapScore;		
	}	

	@Override
	public double[] getThresholds() {
		double val = 0;
		switch(getVotingStrategy()){
			case "ALL":
				val = getNVoters();
				break;
			case "HALF":
				val = Math.ceil(getNVoters()/2.0);
				break;
			case "THIRD":
				val = Math.ceil(getNVoters()/3.0);
				break;
			case "QUARTER":
				val = Math.ceil(getNVoters()/4.0);
				break;
			default:
				val = Double.parseDouble(getVotingStrategy());
		}
		return new double[]{val};
	}

	@Override
	public double applyThreshold(double value, VotingResult vr) {
		return value / getThresholds()[0];
	}

	@Override
	public DecisionFunction getDecisionFunction() {
		return new StaticThresholdGreaterThanDecision(getThresholds()[0], null);
	}

	@Override
	public boolean isMetaLearner() {
		return false;
	}

	@Override
	public double getConfidence(double value) {
		return Math.abs(getThresholds()[0] - value) / getThresholds()[0];
	}

}
