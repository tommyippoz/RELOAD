/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
	protected ValuesDecision(String[] val, ValueSeries algorithmScores) {
		super("ValuesDecision", DecisionFunctionType.VALUES, false, algorithmScores);
		buildThresholds(val); 
	}

	private void buildThresholds(String[] val) {
		List<Double> thrList = new LinkedList<>();
		if(val != null && val.length > 0){
			thresholds = new double[val.length][2];
			for(int i=0;i<val.length;i++){
				if(AppUtility.isNumber(val[i])){
					double num = Double.valueOf(val[i]);
					int n = AppUtility.getLimExp(num);
					double offset = Double.valueOf("1.0E" + n);
					thresholds[i][0] = num - offset;
					thresholds[i][1] = num + offset;
					thrList.add(num);
				} else if(val[i].contains("-")){
					String[] splt = val[i].split("-");
					if(splt[0].length() > 0 && AppUtility.isNumber(splt[0].trim())){
						thresholds[i][0] = Double.valueOf(splt[0].trim());
						thrList.add(thresholds[i][0]);
					}
					if(splt[1].length() > 0 && AppUtility.isNumber(splt[1].trim())){
						thresholds[i][1] = Double.valueOf(splt[1].trim());
						thrList.add(thresholds[i][1]);
					}
				}
			}
		} else thresholds = null;
		values = ArrayUtils.toPrimitive(thrList.toArray(new Double[thrList.size()]));
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	public AnomalyResult classify(AlgorithmResult value) {
		if(thresholds != null){
			for(int i=0;i<thresholds.length;i++){
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
