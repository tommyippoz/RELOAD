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
	
	/** The scores. */
	private ValueSeries scores;

	/**
	 * Instantiates a new threshold decision.
	 *
	 * @param threshold the threshold
	 * @param scores the scores
	 */
	public ThresholdDecision(double threshold, ValueSeries scores) {
		super("threshold", DecisionFunctionType.THRESHOLD, false);
		this.threshold = threshold;
		this.scores = scores;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult value) {
		int index = (int)(threshold*scores.size())-1;
		if(index < 0)
			index = 0;
		if(index < scores.size())
			return value.getScore() >= scores.get(index) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
		else return value.getScore() >= scores.get(scores.size()-1) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		int index = (int)(threshold*scores.size())-1;
		if(index < 0)
			index = 0;
		else if(index == scores.size())
			index--;
		return "THRESHOLD(" + threshold + ") - {ANOMALY: value >= " + scores.get(index) + "}";
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
		int index = (int)(threshold*scores.size())-1;
		if(index < 0)
			index = 0;
		if(index < scores.size())
			toReturn = scores.get(index);
		else toReturn = scores.get(scores.size()-1);
		return new double[]{toReturn};
	}

}
