/**
 * 
 */
package ippoz.reload.decisionfunction;

import java.text.DecimalFormat;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class RightConfidenceIntervalFunction extends DecisionFunction {
	
	private double avg;
	
	private double std;
	
	private double ratio;

	public RightConfidenceIntervalFunction(double ratio, double avg, double std) {
		super("right_confidence_interval", DecisionFunctionType.CONFIDENCE_INTERVAL);
		this.avg = avg;
		this.std = std;
		this.ratio = ratio;
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult aResult) {
		if(!Double.isFinite(aResult.getScore()))
			return AnomalyResult.UNKNOWN;
		else if(aResult.getScore() > avg + ratio*std)
			return AnomalyResult.ANOMALY;
		else return AnomalyResult.NORMAL;
	}
	
	@Override
	public String toCompactString() {
		DecimalFormat df = new DecimalFormat("#.000");
		return "RCONF(avg:" + df.format(avg) + " ratio:" + ratio + " std:" + df.format(std) + ")  - {ANOMALY: value > " + df.format(avg +ratio*std) + "}";
	}
	
	@Override
	public String getClassifierTag() {
		return "RIGHT_CONFIDENCE_INTERVAL(" + ratio + ")";
	}

}
