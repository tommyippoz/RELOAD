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
public class ModeFunction extends DecisionFunction {
	
	/** The mode. */
	private double mode;
	
	/** The ratio. */
	private double ratio;

	/**
	 * Instantiates a new confidence interval function.
	 *
	 * @param ratio the ratio
	 * @param avg the avg
	 * @param std the std
	 */
	public ModeFunction(double ratio, double mode, boolean revertFlag) {
		super("mode_function", DecisionFunctionType.MODE, revertFlag);
		this.mode = mode;
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
			if(aResult.getScore() >= mode - ratio*mode && aResult.getScore() <= mode + ratio*mode)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		} else {
			if(aResult.getScore() < mode - ratio*mode)
				return AnomalyResult.ANOMALY;
			else if(aResult.getScore() > mode + ratio*mode)
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
			return "MODE(mode:" + AppUtility.formatDouble(mode) + " ratio:" + ratio + ") - {ANOMALY: value >= " + AppUtility.formatDouble(mode - ratio*mode) + " and value <= " + AppUtility.formatDouble(mode + ratio*mode) + "}";		
		else return "MODE(mode:" + AppUtility.formatDouble(mode) + " ratio:" + ratio + ") - {ANOMALY: value < " + AppUtility.formatDouble(mode - ratio*mode) + " or value > " + AppUtility.formatDouble(mode + ratio*mode) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "MODE(" + ratio + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{mode - mode*ratio, mode + mode*ratio};
	}

}
