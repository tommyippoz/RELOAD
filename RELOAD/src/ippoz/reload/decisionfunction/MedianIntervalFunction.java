/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;

/**
 * @author Tommy
 *
 */
public class MedianIntervalFunction extends DecisionFunction {
	
	/** The mode. */
	private double median;
	
	/** The st dev. */
	private double std;
	
	/** The ratio. */
	private double ratio;

	/**
	 * Instantiates a new confidence interval function.
	 *
	 * @param ratio the ratio
	 * @param avg the avg
	 * @param std the std
	 */
	public MedianIntervalFunction(double ratio, double mode, double std, boolean revertFlag) {
		super("median_interval", DecisionFunctionType.MEDIAN_INTERVAL, revertFlag);
		this.median = mode;
		this.std = std;
		this.ratio = ratio;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult aResult) {
		if(!Double.isFinite(aResult.getScore()))
			return AnomalyResult.UNKNOWN;
		if(getRevertFlag()){
			if(aResult.getScore() >= median - ratio*std && aResult.getScore() <= median + ratio*std)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		} else {
			if(aResult.getScore() < median - ratio*std)
				return AnomalyResult.ANOMALY;
			else if(aResult.getScore() > median + ratio*std)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		}
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		if(getRevertFlag())
			return "MEDCONF(median:" + AppUtility.formatDouble(median) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ") - {ANOMALY: value >= " + AppUtility.formatDouble(median -ratio*std) + " and value <= " + AppUtility.formatDouble(median +ratio*std) + "}";		
		else return "MEDCONF(median:" + AppUtility.formatDouble(median) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ") - {ANOMALY: value < " + AppUtility.formatDouble(median -ratio*std) + " or value > " + AppUtility.formatDouble(median +ratio*std) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "MEDIAN_INTERVAL(" + ratio + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{median - ratio*std, median + ratio*std};
	}

}
