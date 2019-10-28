/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;

// TODO: Auto-generated Javadoc
/**
 * The Class LogThresholdDecision.
 *
 * @author Tommy
 */
public class LogThresholdDecision extends DecisionFunction {
	
	/** The perc. */
	private double perc;
	
	/** The size. */
	private double size;

	/**
	 * Instantiates a new log threshold decision.
	 *
	 * @param perc the perc
	 * @param size the size
	 */
	public LogThresholdDecision(double perc, double size, boolean revertFlag) {
		super("logthreshold", DecisionFunctionType.LOG_THRESHOLD, revertFlag);
		this.perc = perc;
		this.size = size;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult value) {
		double threshold = size*Math.log(1.0/(perc));
		return value.getScore() > threshold ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		return "LOG(" + perc + "% " + size + ") - {ANOMALY: value > " + (size*Math.log(1.0/(perc))) + "}";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "LOG_THRESHOLD(" + perc + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		return new double[]{size*Math.log(1.0/(perc))};
	}

}
