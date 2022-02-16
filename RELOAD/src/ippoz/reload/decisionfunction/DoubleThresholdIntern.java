/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.ValueSeries;

/**
 * The Class DoubleThresholdIntern. Sets two thresholds, evaluates data point as anomalous
 * if value is inside the interval.
 *
 * @author Tommy
 */
public class DoubleThresholdIntern extends DecisionFunction {
	
	/** The lower threshold. */
	private double lowerThreshold;
	
	/** The upper threshold. */
	private double upperThreshold;

	/**
	 * Instantiates a new double threshold intern.
	 *
	 * @param lowerThreshold the lower threshold
	 * @param upperThreshold the upper threshold
	 */
	protected DoubleThresholdIntern(double lowerThreshold, double upperThreshold, ValueSeries algorithmScores) {
		super("DoubleThresholdIntern", DecisionFunctionType.DOUBLE_THRESHOLD_INTERN, false, algorithmScores);
		this.lowerThreshold = lowerThreshold;
		this.upperThreshold = upperThreshold;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	public AnomalyResult classify(AlgorithmResult value) {
		if(value.getScore() <= lowerThreshold || value.getScore() >= upperThreshold)
			return !getRevertFlag() ? AnomalyResult.NORMAL : AnomalyResult.ANOMALY;
		else return !getRevertFlag() ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		return "DTHRIN(" + lowerThreshold + ", " + upperThreshold + ") -  {ANOMALY: " + lowerThreshold + " < value < " + upperThreshold + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "DOUBLE_THRESHOLD_INTERN(" + lowerThreshold + ", " + upperThreshold + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{lowerThreshold, upperThreshold};
	}

}
