/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;

/**
 * The Class RightConfidenceIntervalFunction. Defines anomalies if
 * value < avg - ratio*std or value > avg + ratio*std
 *
 * @author Tommy
 */
public class RightConfidenceIntervalFunction extends DecisionFunction {
	
	/** The avg. */
	private double avg;
	
	/** The std. */
	private double std;
	
	/** The ratio. */
	private double ratio;

	/**
	 * Instantiates a new right confidence interval function.
	 *
	 * @param ratio the ratio
	 * @param avg the avg
	 * @param std the std
	 */
	public RightConfidenceIntervalFunction(double ratio, ValueSeries algorithmScores, boolean revertFlag) {
		super("right_confidence_interval", DecisionFunctionType.CONFIDENCE_INTERVAL, revertFlag, algorithmScores);
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
			if(aResult.getScore() <= avg + ratio*std)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		} else {
			if(aResult.getScore() > avg + ratio*std)
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
			return "RCONF(avg:" + AppUtility.formatDouble(avg) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ")  - {ANOMALY: value <= " + AppUtility.formatDouble(avg +ratio*std) + "}";
		return "RCONF(avg:" + AppUtility.formatDouble(avg) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ")  - {ANOMALY: value > " + AppUtility.formatDouble(avg +ratio*std) + "}";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "RIGHT_CONFIDENCE_INTERVAL(" + ratio + ")";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{avg + ratio*std};
	}

}
