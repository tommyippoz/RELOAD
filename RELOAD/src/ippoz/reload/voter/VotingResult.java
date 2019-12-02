/**
 * 
 */
package ippoz.reload.voter;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class VotingResult extends AlgorithmResult {
	
	private double votingResult;
	
	private ScoresVoter voter;

	public VotingResult(AlgorithmResult ar, double votingResult, ScoresVoter voter) {
		super(ar.getData(), ar.getInjection(), ar.getScore(), ar.getScoreEvaluation(), ar.getDecisionFunction());
		this.votingResult = votingResult;
		this.voter = voter;
	}

	public double getVotingResult() {
		return votingResult;
	}

	@Override
	public double getScore() {
		if(voter.getNVoters() > 1)
			return voter.applyThreshold(votingResult);
		else return super.getScore();
	}

	@Override
	public String toString() {
		return "[" + getScore() + "]";
	}	
	
	

}
