/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class LeftIQRFunction extends IQRFunction {

	protected LeftIQRFunction(double ratio, double q1, double q3) {
		super(ratio, q1, q3);
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
		return "LIQR(Q1:" + q1 + " Q3:" + q3 + " ratio:" + ratio + ") - {ANOMALY: value < " + (q1 - ratio*iqr) + "}";
	}

}
