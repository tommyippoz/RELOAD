/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;

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
	protected LeftPositiveIQRFunction(double ratio, ValueSeries algorithmScores, boolean revertFlag) {
		super("LEFT_POSITIVE_IQR", DecisionFunctionType.LEFT_POSITIVE_IQR, revertFlag, algorithmScores);
		this.q1 = algorithmScores.getQ1();
		this.q3 = algorithmScores.getQ3();
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
		int maxIt = 100;
		while(maxIt > 0 && toTune > 0 && q1 - toTune*iqr <= 0){
			toTune = toTune*0.75;
			maxIt--;
		}
		return toTune;
		
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.decisionfunction.DecisionFunction#classify(double)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult value) {
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
		return "LPIQR(Q1:" + AppUtility.formatDouble(q1) + " Q3:" + AppUtility.formatDouble(q3) + " ratio:" + ratio + ") - {ANOMALY: 0 <= value < " + AppUtility.formatDouble(q1 - ratio*iqr) + "}";
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
