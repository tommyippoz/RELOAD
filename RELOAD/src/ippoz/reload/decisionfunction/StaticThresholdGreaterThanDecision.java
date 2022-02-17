/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.ValueSeries;

/**
 * The Class StaticThresholdGreaterThanDecision. Data point is anomalous if value > threshold.
 *
 * @author Tommy
 */
public class StaticThresholdGreaterThanDecision extends DecisionFunction {

	/** The threshold. */
	private double threshold;
	
	/**
	 * Instantiates a new static threshold greater than decision.
	 *
	 * @param threshold the threshold
	 */
	public StaticThresholdGreaterThanDecision(double threshold, ValueSeries algorithmScores) {
		super("StaticGreaterThanClassifier", DecisionFunctionType.STATIC_THRESHOLD_GREATERTHAN, false, algorithmScores);
		this.threshold = threshold;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult value) {
		if(value.getScore() < threshold)
			return !getRevertFlag() ? AnomalyResult.NORMAL : AnomalyResult.ANOMALY;
		else return !getRevertFlag() ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		return "SGTHR(" + threshold + ") -  {ANOMALY: value > " + threshold + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "STATIC_THRESHOLD_GREATERTHAN(" + threshold + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{threshold};
	}

}
