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
public class MedianFunction extends DecisionFunction {
	
	/** The mode. */
	private double median;
	
	/** The ratio. */
	private double ratio;

	/**
	 * Instantiates a new confidence interval function.
	 *
	 * @param ratio the ratio
	 * @param avg the avg
	 * @param std the std
	 */
	public MedianFunction(double ratio, ValueSeries algorithmScores, boolean revertFlag) {
		super("median_function", DecisionFunctionType.MEDIAN, revertFlag, algorithmScores);
		this.median = algorithmScores.getMedian();
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
			if(aResult.getScore() >= median - ratio*median && aResult.getScore() <= median + ratio*median)
				return AnomalyResult.ANOMALY;
			else return AnomalyResult.NORMAL;
		} else {
			if(aResult.getScore() < median - ratio*median)
				return AnomalyResult.ANOMALY;
			else if(aResult.getScore() > median + ratio*median)
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
			return "MEDIAN(mode:" + AppUtility.formatDouble(median) + " ratio:" + ratio + ") - {ANOMALY: value >= " + AppUtility.formatDouble(median - ratio*median) + " and value <= " + AppUtility.formatDouble(median + ratio*median) + "}";		
		else return "MEDIAN(mode:" + AppUtility.formatDouble(median) + " ratio:" + ratio + ") - {ANOMALY: value < " + AppUtility.formatDouble(median - ratio*median) + " or value > " + AppUtility.formatDouble(median + ratio*median) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "MEDIAN(" + ratio + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{median - median*ratio, median + median*ratio};
	}

}
