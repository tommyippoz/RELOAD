/**
 * 
 */
package ippoz.madness.detector.decisionfunction;

import ippoz.madness.detector.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class LogThresholdDecision extends DecisionFunction {
	
	private double perc;
	
	private double size;

	public LogThresholdDecision(double perc, double size) {
		super("logthreshold", DecisionFunctionType.LOG_THRESHOLD);
		this.perc = perc;
		this.size = size;
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult value) {
		double threshold = size*Math.log(1.0/(perc));
		return value.getScore() > threshold ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}

}
