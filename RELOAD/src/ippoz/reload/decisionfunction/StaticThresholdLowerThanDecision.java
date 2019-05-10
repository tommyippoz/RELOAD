/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class StaticThresholdLowerThanDecision extends DecisionFunction {

	private double threshold;
	
	public StaticThresholdLowerThanDecision(double threshold) {
		super("StaticLowerThanClassifier", DecisionFunctionType.STATIC_THRESHOLD_LOWERTHAN);
		this.threshold = threshold;
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult value) {
		if(value.getScore() >= threshold)
			return AnomalyResult.NORMAL;
		else return AnomalyResult.ANOMALY;
	}
	
	@Override
	public String toCompactString() {
		return "SLTHR(" + threshold + ") -  {ANOMALY: value < " + threshold + "}";
	}

	@Override
	public String getClassifierTag() {
		return "STATIC_THRESHOLD_LOWERTHAN(" + threshold + ")";
	}

}
