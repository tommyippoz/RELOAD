/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;

import java.text.DecimalFormat;

/**
 * The Class LeftPositiveIQRFunction. Sets the IQR as q3-q1, evaluating data point as anomalous if
 * 0 < value < q1-ratio*iqr
 *
 * @author Tommy
 */
public class LeftPositiveIQRFunction extends DecisionFunction {
	
	/** The q1. */
	protected double q1;
	
	/** The q3. */
	protected double q3;
	
	/** The ratio. */
	protected double ratio;

	/**
	 * Instantiates a new left positive iqr function.
	 *
	 * @param ratio the ratio
	 * @param q1 the q1
	 * @param q3 the q3
	 */
	protected LeftPositiveIQRFunction(double ratio, double q1, double q3) {
		super("LEFT_POSITIVE_IQR", DecisionFunctionType.LEFT_POSITIVE_IQR);
		this.q1 = q1;
		this.q3 = q3;
		this.ratio = tuneRatio(ratio, q1, q3);
	}
	
	/**
	 * Tune ratio.
	 *
	 * @param toTune the to tune
	 * @param q1 the q1
	 * @param q3 the q3
	 * @return the double
	 */
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
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		double iqr = q3 - q1;
		DecimalFormat df = new DecimalFormat("#.000");
		return "LPIQR(Q1:" + df.format(q1) + " Q3:" + df.format(q3) + " ratio:" + ratio + ") - {ANOMALY: 0 <= value < " + df.format(q1 - ratio*iqr) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "LEFT_POSITIVE_IQR(" + ratio + ")";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		double iqr = q3 - q1;
		return new double[]{0, q1 - ratio*iqr};
	}

}
