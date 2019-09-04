/**
 * 
 */
package ippoz.reload.reputation;

import ippoz.reload.commons.support.TimedResult;
import ippoz.reload.metric.TP_Metric;

import java.util.List;

/**
 * The Class BetaReputation.
 * Constructs a reputation using the Beta calculation.
 *
 * @author Tommy
 */
public class BetaReputation extends Reputation {
	
	private boolean validAfter;

	/**
	 * Instantiates a new Beta reputation.
	 *
	 * @param reputationTag the reputation tag
	 */
	public BetaReputation(String reputationTag, boolean validAfter) {
		super(reputationTag);
		this.validAfter = validAfter;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.reputation.Reputation#evaluateExperimentReputation(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateExperimentReputation(List<TimedResult> anomalyEvaluations) {
		double tp = new TP_Metric(true, validAfter).evaluateAnomalyResults(anomalyEvaluations);
		double nInj = countInjections(anomalyEvaluations);
		double alpha = tp + 1;
		double beta = nInj + 1;
		return alpha*1.0/(alpha + beta);
	}
	
	private int countInjections(List<TimedResult> anomalyEvaluations){
		int count = 0;
		for(TimedResult tr : anomalyEvaluations){
			if(tr.getInjectedElement() != null)
				count++;
		}
		return count;
	}

}
