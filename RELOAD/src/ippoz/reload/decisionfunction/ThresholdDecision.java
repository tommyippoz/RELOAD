/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.ValueSeries;

/**
 * The Class ThresholdDecision. A data point is anomalous if value exceeds the threshold% of values of the training set.
 *
 * @author Tommy
 */
public class ThresholdDecision extends DecisionFunction {
	
	/** The threshold. */
	private double threshold;

	/**
	 * Instantiates a new threshold decision.
	 *
	 * @param threshold the threshold
	 * @param scores the scores
	 */
	public ThresholdDecision(double threshold, ValueSeries scores) {
		super("threshold", DecisionFunctionType.THRESHOLD, false, scores);
		this.threshold = threshold;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult value) {
		int index = (int)(threshold*getRefScores().size())-1;
		if(index < 0)
			index = 0;
		if(index < getRefScores().size())
			return value.getScore() >= getRefScores().get(index) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
		else return value.getScore() >= getRefScores().get(getRefScores().size()-1) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		int index = (int)(threshold*getRefScores().size())-1;
		if(index < 0)
			index = 0;
		else if(index == getRefScores().size())
			index--;
		return "THRESHOLD(" + threshold + ") - {ANOMALY: value >= " + getRefScores().get(index) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "THRESHOLD(" + threshold + ")";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		double toReturn;
		int index = (int)(threshold*getRefScores().size())-1;
		if(index < 0)
			index = 0;
		if(index < getRefScores().size())
			toReturn = getRefScores().get(index);
		else toReturn = getRefScores().get(getRefScores().size()-1);
		return new double[]{toReturn};
	}

}
