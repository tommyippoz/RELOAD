/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;

/**
 * The Class RightIQRFunction. Sets the IQR as q3-q1, evaluating data point as anomalous if
 * value > q3+ratio*iqr
 *
 * @author Tommy
 */
public class RightIQRFunction extends DecisionFunction {
	
	/** The q1. */
	protected double q1;
	
	/** The q3. */
	protected double q3;
	
	/** The ratio. */
	protected double ratio;

	/**
	 * Instantiates a new right iqr function.
	 *
	 * @param ratio the ratio
	 * @param q1 the q1
	 * @param q3 the q3
	 */
	protected RightIQRFunction(double ratio, double q1, double q3, boolean revertFlag) {
		super("RIGHT_IQR", DecisionFunctionType.RIGHT_IQR, revertFlag);
		this.q1 = q1;
		this.q3 = q3;
		this.ratio = ratio;
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.decisionfunction.DecisionFunction#classify(double)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult value) {
		double iqr = q3 - q1;
		boolean outright = value.getScore() > q3 + ratio*iqr;
		if(getRevertFlag()){
			if(!outright)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		} else {
			if(outright)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		}
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		double iqr = q3 - q1;
		if(getRevertFlag())
			return "RIGHT_IQR(Q1:" + AppUtility.formatDouble(q1) + " Q3:" + AppUtility.formatDouble(q3) + " IQR: " + AppUtility.formatDouble(iqr) + " ratio:" + ratio + ") - {ANOMALY: value <= " + AppUtility.formatDouble(q3 + ratio*iqr) + "}";
		else return "RIGHT_IQR(Q1:" + AppUtility.formatDouble(q1) + " Q3:" + AppUtility.formatDouble(q3) + " IQR: " + AppUtility.formatDouble(iqr) + " ratio:" + ratio + ") - {ANOMALY: value > " + AppUtility.formatDouble(q3 + ratio*iqr) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "RIGHT_IQR(" + ratio + ")";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		double iqr = q3 - q1;
		return new double[]{q3 + ratio*iqr};
	}

}
