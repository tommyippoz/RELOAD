/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;

/**
 * @author Tommy
 *
 */
public class ModeIntervalFunction extends DecisionFunction {
	
	/** The mode. */
	private double mode;
	
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
	public ModeIntervalFunction(double ratio, ValueSeries algorithmScores, boolean revertFlag) {
		super("mode_interval", DecisionFunctionType.MODE_INTERVAL, revertFlag, algorithmScores);
		this.mode = algorithmScores.getMode();
		this.std = algorithmScores.getStd();
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
			if(aResult.getScore() >= mode - ratio*std && aResult.getScore() <= mode + ratio*std)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		} else {
			if(aResult.getScore() < mode - ratio*std)
				return AnomalyResult.ANOMALY;
			else if(aResult.getScore() > mode + ratio*std)
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
			return "MCONF(mode:" + AppUtility.formatDouble(mode) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ") - {ANOMALY: value >= " + AppUtility.formatDouble(mode -ratio*std) + " and value <= " + AppUtility.formatDouble(mode +ratio*std) + "}";		
		else return "MCONF(mode:" + AppUtility.formatDouble(mode) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ") - {ANOMALY: value < " + AppUtility.formatDouble(mode -ratio*std) + " or value > " + AppUtility.formatDouble(mode +ratio*std) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "MODE_INTERVAL(" + ratio + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{mode - ratio*std, mode + ratio*std};
	}

}
