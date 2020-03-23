/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;

/**
 * The Class LeftPositiveConfidenceIntervalFunction. Defines anomalies if
 * 0 < value < avg - ratio*std
 *
 * @author Tommy
 */
public class LeftPositiveConfidenceIntervalFunction extends DecisionFunction {

	/** The avg. */
	private double avg;
	
	/** The std. */
	private double std;
	
	/** The ratio. */
	private double ratio;

	/**
	 * Instantiates a new left positive confidence interval function.
	 *
	 * @param ratio the ratio
	 * @param avg the avg
	 * @param std the std
	 */
	public LeftPositiveConfidenceIntervalFunction(double ratio, ValueSeries algorithmScores, boolean revertFlag) {
		super("left_positive_confidence_interval", DecisionFunctionType.CONFIDENCE_INTERVAL, revertFlag, algorithmScores);
		this.avg = algorithmScores.getAvg();
		this.std = algorithmScores.getStd();
		this.ratio = tuneRatio(ratio, avg, std);
	}
	
	/**
	 * Tune ratio.
	 *
	 * @param toTune the to tune
	 * @param avg the avg
	 * @param std the std
	 * @return the double
	 */
	private static double tuneRatio(double toTune, double avg, double std){
		while(avg - toTune*std <= 0){
			toTune = toTune*0.75;
		}
		return toTune;
		
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult aResult) {
		if(!Double.isFinite(aResult.getScore()))
			return AnomalyResult.UNKNOWN;
		if(aResult.getScore() < avg - ratio*std)
			return AnomalyResult.ANOMALY;
		else return AnomalyResult.NORMAL;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		return "LPCONF(avg:" + AppUtility.formatDouble(avg) + " ratio:" + ratio + " std:" + AppUtility.formatDouble(std) + ")  - {ANOMALY: 0 <= value < " + AppUtility.formatDouble(avg - ratio*std) + "}";
		
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "LEFT_POSITIVE_CONFIDENCE_INTERVAL(" + ratio + ")";
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{0, avg - ratio*std};
	}

}
