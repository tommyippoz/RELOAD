/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class LeftPositiveIQRFunction extends IQRFunction {
	
	protected LeftPositiveIQRFunction(double ratio, double q1, double q3) {
		super(tuneRatio(ratio, q1, q3), q1, q3);
	}
	
	private static double tuneRatio(double toTune, double q1, double q3){
		double iqr = q3 - q1;
		while(q1 - toTune*iqr <= 0){
			toTune = toTune*0.75;
		}
		return toTune;
		
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.decisionfunction.DecisionFunction#classify(double)
	 */
	@Override
	protected AnomalyResult classify(AlgorithmResult value) {
		double iqr = q3 - q1;
		boolean outleft = value.getScore() < q1 - ratio*iqr;
		if(outleft)
			return AnomalyResult.ANOMALY;
		else return AnomalyResult.NORMAL;
	}
	
	@Override
	public String toCompactString() {
		double iqr = q3 - q1;
		return "LPIQR(Q1:" + q1 + " Q3:" + q3 + " ratio:" + ratio + ") - {ANOMALY: 0 <= value < " + (q1 - ratio*iqr) + "}";
	}

}
