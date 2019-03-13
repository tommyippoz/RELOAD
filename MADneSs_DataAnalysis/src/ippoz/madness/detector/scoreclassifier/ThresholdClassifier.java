/**
 * 
 */
package ippoz.madness.detector.scoreclassifier;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class ThresholdClassifier extends ScoreClassifier {
	
	private double threshold;
	
	private List<Double> scores;

	public ThresholdClassifier(double threshold, List<Double> scores) {
		super("threshold", ClassifierType.THRESHOLD);
		this.threshold = threshold;
		this.scores = scores;
	}

	@Override
	public AnomalyResult classify(double doubleValue) {
		int index = (int)(threshold*scores.size())-1;
		if(index < scores.size())
			return doubleValue >= scores.get(index) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
		else return doubleValue >= scores.get(scores.size()-1) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}

}
