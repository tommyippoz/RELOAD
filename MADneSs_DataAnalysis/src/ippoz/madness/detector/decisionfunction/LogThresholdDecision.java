/**
 * 
 */
package ippoz.madness.detector.decisionfunction;

/**
 * @author Tommy
 *
 */
public class LogThresholdDecision extends DecisionFunction {
	
	private double perc;
	
	private double size;

	public LogThresholdDecision(double perc, double size) {
		super("logthreshold", DecisionFunctionType.LOG_THRESHOLD);
		this.perc = perc;
		this.size = size;
	}

	@Override
	public AnomalyResult classify(double doubleValue) {
		double threshold = size*Math.log(1.0/(perc));
		return doubleValue > threshold ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}

}
