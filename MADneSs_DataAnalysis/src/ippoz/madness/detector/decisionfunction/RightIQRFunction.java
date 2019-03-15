/**
 * 
 */
package ippoz.madness.detector.decisionfunction;

import ippoz.madness.detector.algorithm.result.AlgorithmResult;

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

}
