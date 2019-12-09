/**
 * 
 */
package ippoz.reload.voter;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.decisionfunction.DecisionFunction;

/**
 * @author Tommy
 *
 */
public class AlgorithmVoter extends ScoresVoter {
	
	private AlgorithmType algType;
	
	private DataSeriesNonSlidingAlgorithm alg;

	public AlgorithmVoter(String checkerSelection, AlgorithmType algType) {
		super(checkerSelection, algType.toString());
		this.algType = algType;
		alg = null;
	}
	
	public void initializeAlgorithm(DataSeries ds, AlgorithmConfiguration conf){
		alg = (DataSeriesNonSlidingAlgorithm) DetectionAlgorithm.buildAlgorithm(algType, ds, conf);
	}

	@Override
	public double voteResults(double[] individualScores) {
		// TODO
		return Double.NaN;
	}

	@Override
	public double[] getThresholds() {
		return getDecisionFunction().getThresholds();
	}

	@Override
	public double applyThreshold(double value) {
		// TODO
		return Double.NaN;
	}

	@Override
	public DecisionFunction getDecisionFunction() {
		return alg.getDecisionFunction();
	}
	
}
