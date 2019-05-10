/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class StaticThresholdGreaterThanDecision extends DecisionFunction {

	private double threshold;
	
	public StaticThresholdGreaterThanDecision(double threshold) {
		super("StaticGreaterThanClassifier", DecisionFunctionType.STATIC_THRESHOLD_GREATERTHAN);
		this.threshold = threshold;
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult value) {
		if(value.getScore() < threshold)
			return AnomalyResult.NORMAL;
		else return AnomalyResult.ANOMALY;
	}
	
	@Override
	public String toCompactString() {
		return "SGTHR(" + threshold + ") -  {ANOMALY: value > " + threshold + "}";
	}

	@Override
	public String getClassifierTag() {
		return "STATIC_THRESHOLD_GREATERTHAN(" + threshold + ")";
	}

}
