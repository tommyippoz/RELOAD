/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class LeftPositiveConfidenceIntervalFunction extends DecisionFunction {

	private double avg;
	
	private double std;
	
	private double ratio;

	public LeftPositiveConfidenceIntervalFunction(double ratio, double avg, double std) {
		super("left_positive_confidence_interval", DecisionFunctionType.CONFIDENCE_INTERVAL);
		this.avg = avg;
		this.std = std;
		this.ratio = tuneRatio(ratio, avg, std);
	}
	
	private static double tuneRatio(double toTune, double avg, double std){
		while(avg - toTune*std <= 0){
			toTune = toTune*0.75;
		}
		return toTune;
		
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult aResult) {
		if(!Double.isFinite(aResult.getScore()))
			return AnomalyResult.UNKNOWN;
		if(aResult.getScore() < avg - ratio*std)
			return AnomalyResult.ANOMALY;
		else return AnomalyResult.NORMAL;
	}
	
	@Override
	public String toCompactString() {
		return "LPCONF(avg:" + avg + " ratio:" + ratio + " std:" + std + ")  - {ANOMALY: 0 <= value < " + (avg - ratio*std) + "}";
		
	}

}
