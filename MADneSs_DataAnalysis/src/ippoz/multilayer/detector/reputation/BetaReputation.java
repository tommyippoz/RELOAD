/**
 * 
 */
package ippoz.multilayer.detector.reputation;

import ippoz.multilayer.detector.commons.knowledge.Knowledge;
import ippoz.multilayer.detector.commons.support.TimedValue;
import ippoz.multilayer.detector.metric.TP_Metric;

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
	public double evaluateExperimentReputation(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		double tp = new TP_Metric(true, validAfter).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double nInj = countInjections(knowledge);
		double alpha = tp + 1;
		double beta = nInj + 1;
		return alpha*1.0/(alpha + beta);
	}
	
	private int countInjections(Knowledge knowledge){
		int count = 0;
		for(int i=0;i<knowledge.size();i++){
			if(knowledge.getInjection(i) != null)
				count++;
		}
		return count;
	}

}
