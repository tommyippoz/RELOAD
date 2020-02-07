/**
 * 
 */
package ippoz.reload.voter;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.manager.InputManager;

import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public abstract class ScoresVoter {
	
	private String checkerSelection;
	
	private String votingStrategy;
	
	private int nVoters;
	
	public ScoresVoter(String checkerSelection, String votingStrategy) {
		this.checkerSelection = checkerSelection;
		if(votingStrategy != null)
			this.votingStrategy = votingStrategy.trim();
		nVoters = extractNVoters();
	}

	private int extractNVoters() {
		if(checkerSelection != null){
			checkerSelection = checkerSelection.trim();
			if(checkerSelection.contains("BEST")){
				return Integer.parseInt(checkerSelection.substring(checkerSelection.indexOf("T")+1).trim());
			} else if(checkerSelection.contains("FILTERED")){
				return Integer.parseInt(checkerSelection.substring(checkerSelection.indexOf("D")+1).trim());
			}
		}
		return 0;
	}
	
	/*public double voteResults(List<TimedResult> voting) {
		List<AlgorithmResult> list = new LinkedList<AlgorithmResult>();
		for(TimedResult tr : voting){
			list.add(new AlgorithmResult(tr.));
		}
		// TODO Auto-generated method stub
		return voteResults(list);
	}*/

	public double voteResults(Knowledge know, int knowIndex, Map<AlgorithmModel, AlgorithmResult> snapVoting){
		int index = 0;
		double[] individualScores = null;
		if(snapVoting != null){
			individualScores = new double[snapVoting.size()];
			for(AlgorithmModel model : snapVoting.keySet()){
				individualScores[index++] = DetectionAlgorithm.convertResultIntoDouble(snapVoting.get(model).getScoreEvaluation());
			}
			return voteResults(know, knowIndex, individualScores);
		} else return Double.NaN;
	}
	
	public abstract double voteResults(Knowledge know, int knowIndex, double[] individualScores);
	
	public abstract double[] getThresholds();
	
	/** Bigger than 1 if anomaly */
	public abstract double applyThreshold(double value, VotingResult vr);
	
	@Override
	public String toString(){
		return checkerSelection + " " + votingStrategy;
	}
	
	public String getVotingStrategy(){
		return votingStrategy;
	}
	
	public String getCheckerSelection(){
		return checkerSelection;
	}
	
	public static ScoresVoter generateVoter(String checkerSelection, String votingStrategy) {
		return generateVoter(checkerSelection, votingStrategy, null, null);
	}

	public static ScoresVoter generateVoter(String checkerSelection, String votingStrategy, InputManager iManager, String datasetName) {
		if(checkerSelection != null){
			if(votingStrategy != null && DetectionAlgorithm.isAlgorithm(votingStrategy.trim()) != null){
				if(iManager != null)
					return new AlgorithmVoter(checkerSelection, DetectionAlgorithm.isAlgorithm(votingStrategy.trim()), iManager.getMetaConfigurationFor(datasetName, votingStrategy));
				else return new AlgorithmVoter(checkerSelection, DetectionAlgorithm.isAlgorithm(votingStrategy.trim()), null);
			} else return new MajorityVoter(checkerSelection, votingStrategy.trim());
		} else {
			return null;
		}
	} 

	public boolean checkAnomalyTreshold(Double newMetricValue) {
		if(Math.abs(getSelectionTreshold()) >= 1)
			return nVoters <= Math.abs(getSelectionTreshold());
		else return newMetricValue >= getSelectionTreshold();
	}
	
	/**
	 * Votes results obtaining a contracted indication about anomaly (double score)
	 *
	 * @param snapVoting the complete algorithm scoring results
	 * @return contracted anomaly score
	 */
	/*private double voteResults(Map<AlgorithmModel, AlgorithmResult> snapVoting){
		double snapScore = 0.0;
		boolean undetectable = true;
		for(AlgorithmModel aVoter : snapVoting.keySet()){
			double algScore = DetectionAlgorithm.convertResultIntoDouble(snapVoting.get(aVoter).getScoreEvaluation());
			if(algScore >= 0.0){				
				undetectable = false;
				if(aVoter.getReputationScore() > 0)
					snapScore = snapScore + 1.0*aVoter.getReputationScore()*algScore;
				else snapScore = snapScore + 1.0*algScore;
			}
		}
		if(undetectable)
			return -1.0;
		else return snapScore;
	}*/
	
	public double getSelectionTreshold() {
		if(checkerSelection != null){
			checkerSelection = checkerSelection.trim();
			if(AppUtility.isNumber(checkerSelection))
				return Double.parseDouble(checkerSelection);
			else if(checkerSelection.contains("BEST")){
				return Double.parseDouble(checkerSelection.substring(checkerSelection.indexOf("T")+1).trim());
			} else if(checkerSelection.contains("FILTERED")){
				return -1.0*Double.parseDouble(checkerSelection.substring(checkerSelection.indexOf("D")+1).trim());
			}
		}
		return Double.NaN;
	}

	public boolean checkModel(AlgorithmModel newModel, List<AlgorithmModel> modelList) {
		boolean found = false;
		if(!AppUtility.isNumber(checkerSelection)){
			return nVoters > modelList.size();
		} else {
			for(AlgorithmModel aVoter : modelList){
				if(aVoter.usesSeries(newModel.getDataSeries())){
					found = true;
					break;
				}
			}
			return !found;
		}
	}
	
	public int getNVoters(){
		return nVoters;
	}

	public abstract DecisionFunction getDecisionFunction();

	public abstract boolean isMetaLearner();
	
}
