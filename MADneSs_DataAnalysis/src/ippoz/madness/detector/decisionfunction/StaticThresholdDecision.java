/**
 * 
 */
package ippoz.madness.detector.decisionfunction;

import ippoz.madness.detector.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class StaticThresholdDecision extends DecisionFunction {

	private double threshold;
	
	public StaticThresholdDecision(double threshold) {
		super("StaticClassifier", DecisionFunctionType.STATIC_THRESHOLD);
		this.threshold = threshold;
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult value) {
		if(value.getScore() < threshold)
			return AnomalyResult.NORMAL;
		else return AnomalyResult.ANOMALY;
	}

}
