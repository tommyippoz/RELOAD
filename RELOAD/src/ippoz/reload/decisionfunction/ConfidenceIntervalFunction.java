/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;

/**
 * The Class ConfidenceIntervalFunction. Describes the decision using 
 * confidence interval avg +- ratio*std
 *
 * @author Tommy
 */
public class ConfidenceIntervalFunction extends DecisionFunction {
	
	/** The avg. */
	private double avg;
	
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
	public ConfidenceIntervalFunction(double ratio, double avg, double std, boolean revertFlag) {
		super("confidence_interval", DecisionFunctionType.CONFIDENCE_INTERVAL, revertFlag);
		this.avg = avg;
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
			if(aResult.getScore() >= avg - ratio*std && aResult.getScore() <= avg + ratio*std)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		} else {
			if(aResult.getScore() < avg - ratio*std)
				return AnomalyResult.ANOMALY;
			else if(aResult.getScore() > avg + ratio*std)
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
			return "CONF(avg:" + AppUtility.formatDouble(avg) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ") - {ANOMALY: value >= " + AppUtility.formatDouble(avg -ratio*std) + " and value <= " + AppUtility.formatDouble(avg +ratio*std) + "}";		
		else return "CONF(avg:" + AppUtility.formatDouble(avg) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ") - {ANOMALY: value < " + AppUtility.formatDouble(avg -ratio*std) + " or value > " + AppUtility.formatDouble(avg +ratio*std) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "CONFIDENCE_INTERVAL(" + ratio + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{avg - ratio*std, avg + ratio*std};
	}

}
