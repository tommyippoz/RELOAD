/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class ConfidenceIntervalFunction extends DecisionFunction {
	
	private double avg;
	
	private double std;
	
	private double ratio;

	public ConfidenceIntervalFunction(double ratio, double avg, double std) {
		super("confidence_interval", DecisionFunctionType.CONFIDENCE_INTERVAL);
		this.avg = avg;
		this.std = std;
		this.ratio = ratio;
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult aResult) {
		if(!Double.isFinite(aResult.getScore()))
			return AnomalyResult.UNKNOWN;
		if(aResult.getScore() < avg - ratio*std)
			return AnomalyResult.ANOMALY;
		else if(aResult.getScore() > avg + ratio*std)
			return AnomalyResult.ANOMALY;
		else return AnomalyResult.NORMAL;
	}

	@Override
	public String toCompactString() {
		return "CONF(avg:" + avg + " ratio:" + ratio + " std:" + std + ") - {ANOMALY: value < " + (avg -ratio*std) + " or value > " + (avg +ratio*std) + "}";
	}

}
