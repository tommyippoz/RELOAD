/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.ValueSeries;

/**
 * @author Tommy
 *
 */
public class ThresholdDecision extends DecisionFunction {
	
	private double threshold;
	
	private ValueSeries scores;

	public ThresholdDecision(double threshold, ValueSeries scores) {
		super("threshold", DecisionFunctionType.THRESHOLD);
		this.threshold = threshold;
		this.scores = scores;
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult value) {
		int index = (int)(threshold*scores.size())-1;
		if(index < 0)
			index = 0;
		if(index < scores.size())
			return value.getScore() >= scores.get(index) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
		else return value.getScore() >= scores.get(scores.size()-1) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}

	@Override
	public String toCompactString() {
		int index = (int)(threshold*scores.size())-1;
		if(index < 0)
			index = 0;
		else if(index == scores.size())
			index--;
		return "THR(" + threshold + ") - {ANOMALY: value >= " + scores.get(index) + "}";
	}

}
