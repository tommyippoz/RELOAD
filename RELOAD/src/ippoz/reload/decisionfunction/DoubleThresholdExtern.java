/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.ValueSeries;

/**
 * The Class DoubleThresholdExtern. Sets two thresholds, evaluates data point as anomalous
 * if value is outside the interval.
 *
 * @author Tommy
 */
public class DoubleThresholdExtern extends DecisionFunction {
	
	/** The lower threshold. */
	private double lowerThreshold;
	
	/** The upper threshold. */
	private double upperThreshold;

	/**
	 * Instantiates a new double threshold extern.
	 *
	 * @param lowerThreshold the lower threshold
	 * @param upperThreshold the upper threshold
	 */
	protected DoubleThresholdExtern(double lowerThreshold, double upperThreshold, ValueSeries algorithmScores) {
		super("DoubleThresholdExtern", DecisionFunctionType.DOUBLE_THRESHOLD_EXTERN, false, algorithmScores);
		this.lowerThreshold = lowerThreshold;
		this.upperThreshold = upperThreshold;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	public AnomalyResult classify(AlgorithmResult value) {
		if(value.getScore() >= lowerThreshold && value.getScore() <= upperThreshold)
			return !getRevertFlag() ? AnomalyResult.NORMAL : AnomalyResult.ANOMALY;
		else return !getRevertFlag() ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		return "DTHREX(" + lowerThreshold + ", " + upperThreshold + ") -  {ANOMALY: value < " + lowerThreshold + " or value > " + upperThreshold + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "DOUBLE_THRESHOLD_EXTERN(" + lowerThreshold + ", " + upperThreshold + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{lowerThreshold, upperThreshold};
	}

}
