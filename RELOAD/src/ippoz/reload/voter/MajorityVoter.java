/**
 * 
 */
package ippoz.reload.voter;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.evaluation.AlgorithmModel;

import java.util.Collection;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class MajorityVoter extends ScoresVoter {

	public MajorityVoter(String checkerSelection, String votingStrategy) {
		super(checkerSelection, votingStrategy);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public double voteResults(Collection<AlgorithmResult> individualScores) {
		double snapScore = 0.0;
		boolean undetectable = true;
		for(AlgorithmResult ar : individualScores){
			double algScore = DetectionAlgorithm.convertResultIntoDouble(ar.getScoreEvaluation());
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
	public double getThreshold() {
		switch(getVotingStrategy()){
			case "ALL":
				return getNVoters();
			case "HALF":
				return Math.ceil(getNVoters()/2.0);
			case "THIRD":
				return Math.ceil(getNVoters()/3.0);
			case "QUARTER":
				return Math.ceil(getNVoters()/4.0);
			default:
				return Double.parseDouble(getVotingStrategy());
		}
	}

	@Override
	public double applyThreshold(double value) {
		return value / getThreshold();
	}

}
