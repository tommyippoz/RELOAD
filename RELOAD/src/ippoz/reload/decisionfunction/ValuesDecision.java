/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Tommy
 *
 */
public class ValuesDecision extends DecisionFunction {
	
	/** The lower threshold. */
	private double[] values;
	
	private double[][] thresholds;

	/**
	 * Instantiates a new double threshold extern.
	 *
	 * @param lowerThreshold the lower threshold
	 * @param upperThreshold the upper threshold
	 */
	protected ValuesDecision(Double[] val, ValueSeries algorithmScores) {
		super("ValuesDecision", DecisionFunctionType.VALUES, false, algorithmScores);
		values = ArrayUtils.toPrimitive(val);
		buildThresholds(); 
	}

	private void buildThresholds() {
		if(values != null && values.length > 0){
			thresholds = new double[values.length][2];
			for(int i=0;i<values.length;i++){
				int n = AppUtility.getLimExp(values[i]);
				double offset = Double.valueOf("1.0E" + n);
				thresholds[i][0] = values[i] - offset;
				thresholds[i][1] = values[i] + offset;
			}
		} else thresholds = null;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	public AnomalyResult classify(AlgorithmResult value) {
		if(values != null && thresholds != null){
			for(int i=0;i<values.length;i++){
				if(value.getScore() > thresholds[i][0] && value.getScore() < thresholds[i][1])
					return AnomalyResult.ANOMALY;
			}
		} return AnomalyResult.NORMAL;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		return "VALUES(" + Arrays.toString(values) + ") -  {ANOMALY: is one of the values before}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "VALUES(" + Arrays.toString(values) + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return values;
	}

}
