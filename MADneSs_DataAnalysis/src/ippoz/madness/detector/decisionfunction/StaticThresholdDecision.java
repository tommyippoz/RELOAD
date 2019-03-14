/**
 * 
 */
package ippoz.madness.detector.decisionfunction;

/**
 * @author Tommy
 *
 */
public class StaticThresholdDecision extends DecisionFunction {

	private double threshold;
	
	public StaticThresholdDecision(double threshold) {
		super("StaticClassifier", DecisionFunctionType.STATIC_THRESHOLD);
		this.threshold = threshold;
	}

	@Override
	public AnomalyResult classify(double value) {
		if(value < threshold)
			return AnomalyResult.NORMAL;
		else return AnomalyResult.ANOMALY;
	}

}
