/**
 * 
 */
package ippoz.madness.detector.decisionfunction;

import ippoz.madness.detector.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class IQRFunction extends DecisionFunction {
	
	protected double q1;
	
	protected double q3;
	
	protected double ratio;

	protected IQRFunction(double ratio, double q1, double q3) {
		super("IQR", DecisionFunctionType.IQR);
		this.ratio = ratio;
		this.q1 = q1;
		this.q3 = q3;
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult value) {
		double iqr = q3 - q1;
		boolean outleft = value.getScore() < q1 - ratio*iqr;
		boolean outright = value.getScore() > q3 + ratio*iqr;
		if(outleft || outright)
			return AnomalyResult.ANOMALY;
		else return AnomalyResult.NORMAL;
	}

}
