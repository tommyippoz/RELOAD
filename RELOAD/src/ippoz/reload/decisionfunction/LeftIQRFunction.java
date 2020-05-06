/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;

/**
 * The Class LeftIQRFunction. Sets the IQR as q3-q1, evaluating data point as anomalous if
 * value < q1-ratio*iqr
 *
 * @author Tommy
 */
public class LeftIQRFunction extends DecisionFunction {
	
	/** The q1. */
	protected double q1;
	
	/** The q3. */
	protected double q3;
	
	/** The ratio. */
	protected double ratio;

	/**
	 * Instantiates a new left iqr function.
	 *
	 * @param ratio the ratio
	 * @param q1 the q1
	 * @param q3 the q3
	 */
	protected LeftIQRFunction(double ratio, ValueSeries algorithmScores, boolean revertFlag) {
		super("LEFT_IQR", DecisionFunctionType.LEFT_IQR, revertFlag, algorithmScores);
		this.q1 = algorithmScores.getQ1();
		this.q3 = algorithmScores.getQ3();
		this.ratio = ratio;
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.decisionfunction.DecisionFunction#classify(double)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult value) {
		double iqr = q3 - q1;
		boolean outleft = value.getScore() < q1 - ratio*iqr;
		if(getRevertFlag()){
			if(!outleft)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		} else {
			if(outleft)
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
			return "LEFT_IQR(Q1:" + AppUtility.formatDouble(q1) + " Q3:" + AppUtility.formatDouble(q3) + " ratio:" + ratio + ") - {ANOMALY: value >= " + AppUtility.formatDouble(q1 - ratio*iqr) + "}";
		return "LEFT_IQR(Q1:" + AppUtility.formatDouble(q1) + " Q3:" + AppUtility.formatDouble(q3) + " ratio:" + ratio + ") - {ANOMALY: value < " + AppUtility.formatDouble(q1 - ratio*iqr) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "LEFT_IQR(" + ratio + ")";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		double iqr = q3 - q1;
		return new double[]{q1 - ratio*iqr};
	}

}
