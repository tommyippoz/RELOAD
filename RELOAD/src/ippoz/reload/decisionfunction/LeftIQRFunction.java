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
public class LeftIQRFunction extends DecisionFunction {
	
	protected double q1;
	
	protected double q3;
	
	protected double ratio;

	protected LeftIQRFunction(double ratio, double q1, double q3) {
		super("LEFT_IQR", DecisionFunctionType.LEFT_IQR);
		this.q1 = q1;
		this.q3 = q3;
		this.ratio = ratio;
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
		DecimalFormat df = new DecimalFormat("#.000"); 
		return "LEFT_IQR(Q1:" + df.format(q1) + " Q3:" + df.format(q3) + " ratio:" + ratio + ") - {ANOMALY: value < " + df.format(q1 - ratio*iqr) + "}";
	}

	@Override
	public String getClassifierTag() {
		return "LEFT_IQR(" + ratio + ")";
	}

}
