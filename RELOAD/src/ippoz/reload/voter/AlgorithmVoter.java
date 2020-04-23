/**
 * 
 */
package ippoz.reload.voter;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;

/**
 * @author Tommy
 *
 */
public class AlgorithmVoter extends ScoresVoter {
	
	private DataSeriesNonSlidingAlgorithm alg;

	public AlgorithmVoter(String checkerSelection, AlgorithmType algType, BasicConfiguration conf) {
		super(checkerSelection, algType.toString());
		if(conf != null)
			alg = (DataSeriesNonSlidingAlgorithm) DetectionAlgorithm.buildBaseAlgorithm(algType, DataSeries.fromString(conf.getItem(BasicConfiguration.DATASERIES) + "#PLAIN#NO_LAYER", false), conf);
	}
	
	public AlgorithmType getAlgorithmType(){
		if(alg != null)
			return alg.getLearnerType();
		else return AlgorithmType.valueOf(getVotingStrategy());
	}

	@Override
	public double voteResults(Knowledge know, int knowIndex, double[] individualScores) {
		AlgorithmResult ar = alg.evaluateSnapshot(know, knowIndex);
		if(ar != null)
			return ar.getScore();
		else return Double.NaN;
	}

	@Override
	public double[] getThresholds() {
		return getDecisionFunction().getThresholds();
	}

	@Override
	public double applyThreshold(double value, VotingResult vr) {
		if(alg.getDecisionFunction() != null){
			AnomalyResult ar = alg.getDecisionFunction().classify(vr);
			if(ar != null)
				return DetectionAlgorithm.convertResultIntoDouble(ar);
		}
		return Double.NaN;
	}

	@Override
	public DecisionFunction getDecisionFunction() {
		return alg.getDecisionFunction();
	}

	@Override
	public boolean isMetaLearner() {
		return true;
	}

	@Override
	public double getConfidence(double value) {
		if(alg != null)
			return alg.getConfidence(value);
		else return Double.NaN;
	}
	
}
