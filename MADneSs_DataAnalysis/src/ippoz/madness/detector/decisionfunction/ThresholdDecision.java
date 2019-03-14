/**
 * 
 */
package ippoz.madness.detector.decisionfunction;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class ThresholdDecision extends DecisionFunction {
	
	private double threshold;
	
	private List<Double> scores;

	public ThresholdDecision(double threshold, List<Double> scores) {
		super("threshold", DecisionFunctionType.THRESHOLD);
		this.threshold = threshold;
		this.scores = scores;
	}

	@Override
	public AnomalyResult classify(double doubleValue) {
		int index = (int)(threshold*scores.size())-1;
		if(index < 0)
			index = 0;
		if(index < scores.size())
			return doubleValue >= scores.get(index) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
		else return doubleValue >= scores.get(scores.size()-1) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}

}
