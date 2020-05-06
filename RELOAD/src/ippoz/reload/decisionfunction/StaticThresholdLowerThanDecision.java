/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.ValueSeries;

/**
 * The Class StaticThresholdLowerThanDecision. Data point is anomalous if value < threshold.
 *
 * @author Tommy
 */
public class StaticThresholdLowerThanDecision extends DecisionFunction {

	/** The threshold. */
	private double threshold;
	
	/**
	 * Instantiates a new static threshold lower than decision.
	 *
	 * @param threshold the threshold
	 */
	public StaticThresholdLowerThanDecision(double threshold, ValueSeries algorithmScores) {
		super("StaticLowerThanClassifier", DecisionFunctionType.STATIC_THRESHOLD_LOWERTHAN, false, algorithmScores);
		this.threshold = threshold;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult value) {
		if(value.getScore() >= threshold)
			return AnomalyResult.NORMAL;
		else return AnomalyResult.ANOMALY;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		return "SLTHR(" + threshold + ") -  {ANOMALY: value < " + threshold + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "STATIC_THRESHOLD_LOWERTHAN(" + threshold + ")";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{threshold};
	}

}
