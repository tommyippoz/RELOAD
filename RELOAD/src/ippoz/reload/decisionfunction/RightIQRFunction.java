/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class RightIQRFunction extends IQRFunction {
	
	protected RightIQRFunction(double ratio, double q1, double q3) {
		super(ratio, q1, q3);
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.decisionfunction.DecisionFunction#classify(double)
	 */
	@Override
	protected AnomalyResult classify(AlgorithmResult value) {
		double iqr = q3 - q1;
		boolean outright = value.getScore() > q3 + ratio*iqr;
		if(outright)
			return AnomalyResult.ANOMALY;
		else return AnomalyResult.NORMAL;
	}
	
	@Override
	public String toCompactString() {
		double iqr = q3 - q1;
		return "RIQR(Q1:" + q1 + " Q3:" + q3 + " ratio:" + ratio + ") - {ANOMALY: value > " + (q3 + ratio*iqr) + "}";
	}

}
