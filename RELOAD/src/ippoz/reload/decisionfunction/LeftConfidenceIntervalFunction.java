/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;

/**
 * The Class LeftConfidenceIntervalFunction. Defines anomalies if
 * value < avg - ratio*std
 *
 * @author Tommy
 */
public class LeftConfidenceIntervalFunction extends DecisionFunction {
	
	/** The avg. */
	private double avg;
	
	/** The std. */
	private double std;
	
	/** The ratio. */
	private double ratio;

	/**
	 * Instantiates a new left confidence interval function.
	 *
	 * @param ratio the ratio
	 * @param avg the avg
	 * @param std the std
	 */
	public LeftConfidenceIntervalFunction(double ratio, ValueSeries algorithmScores, boolean revertFlag) {
		super("left_confidence_interval", DecisionFunctionType.CONFIDENCE_INTERVAL, revertFlag, algorithmScores);
		this.avg = algorithmScores.getAvg();
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
			if(aResult.getScore() >= avg - ratio*std)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		} else {
			if(aResult.getScore() < avg - ratio*std)
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
			return "LCONF(avg:" + AppUtility.formatDouble(avg) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ")  - {ANOMALY: value >= " + AppUtility.formatDouble(avg - ratio*std) + "}";		
		else return "LCONF(avg:" + AppUtility.formatDouble(avg) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ")  - {ANOMALY: value < " + AppUtility.formatDouble(avg - ratio*std) + "}";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "LEFT_CONFIDENCE_INTERVAL(" + ratio + ")";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{avg - ratio*std};
	}

}
