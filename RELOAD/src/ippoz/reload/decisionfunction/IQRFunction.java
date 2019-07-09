/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;

/**
 * The Class IQRFunction. Sets the IQR as q3-q1, evaluating data point as anomalous if
 * value < q1-ratio*iqr or value > q3+ratio*iqr
 *
 * @author Tommy
 */
public class IQRFunction extends DecisionFunction {
	
	/** The q1. */
	protected double q1;
	
	/** The q3. */
	protected double q3;
	
	/** The ratio. */
	protected double ratio;

	/**
	 * Instantiates a new IQR function.
	 *
	 * @param ratio the ratio
	 * @param q1 the q1
	 * @param q3 the q3
	 */
	protected IQRFunction(double ratio, double q1, double q3) {
		super("IQR", DecisionFunctionType.IQR);
		this.ratio = ratio;
		if(q1 <= q3) {
			this.q1 = q1;
			this.q3 = q3;
		} else {
			this.q1 = q3;
			this.q3 = q1;
		}
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	@Override
	protected AnomalyResult classify(AlgorithmResult value) {
		double iqr = q3 - q1;
		boolean outleft = value.getScore() < q1 - ratio*iqr;
		boolean outright = value.getScore() > q3 + ratio*iqr;
		if(outleft || outright)
			return AnomalyResult.ANOMALY;
		else return AnomalyResult.NORMAL;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		double iqr = q3 - q1;
		return "IQR(Q1:" + AppUtility.formatDouble(q1) + " Q3:" + AppUtility.formatDouble(q3) + " ratio:" + ratio + ") - {ANOMALY: value < " + AppUtility.formatDouble(q1 - ratio*iqr) + " or value > " + AppUtility.formatDouble(q3 + ratio*iqr) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "IQR(" + ratio + ")";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		double iqr = q3 - q1;
		return new double[]{q1 - ratio*iqr, q3 + ratio*iqr};
	}

}
