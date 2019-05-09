/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;

import java.text.DecimalFormat;

/**
 * @author Tommy
 *
 */
public class LeftPositiveIQRFunction extends DecisionFunction {
	
	protected double q1;
	
	protected double q3;
	
	protected double ratio;

	protected LeftPositiveIQRFunction(double ratio, double q1, double q3) {
		super("LEFT_POSITIVE_IQR", DecisionFunctionType.LEFT_POSITIVE_IQR);
		this.q1 = q1;
		this.q3 = q3;
		this.ratio = tuneRatio(ratio, q1, q3);
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
		DecimalFormat df = new DecimalFormat("#.000");
		return "LPIQR(Q1:" + df.format(q1) + " Q3:" + df.format(q3) + " ratio:" + ratio + ") - {ANOMALY: 0 <= value < " + df.format(q1 - ratio*iqr) + "}";
	}

	@Override
	public String getClassifierTag() {
		return "LEFT_POSITIVE_IQR(" + ratio + ")";
	}

}
