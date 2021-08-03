/**
 * 
 */
package ippoz.reload.reputation;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.ConfusionMatrix;
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
	public BetaReputation(String reputationTag) {
		super(reputationTag);
	}
	
	private int countInjections(List<AlgorithmResult> anomalyEvaluations){
		int count = 0;
		for(AlgorithmResult tr : anomalyEvaluations){
			if(tr.isAnomalous())
				count++;
		}
		return count;
	}

	@Override
	protected double evaluateExperimentReputation(List<AlgorithmResult> anomalyEvaluations, ConfusionMatrix confusionMatrix) {
		double tp = new TP_Metric(true, Double.NaN).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		double nInj = countInjections(anomalyEvaluations);
		double alpha = tp + 1;
		double beta = nInj + 1;
		return alpha*1.0/(alpha + beta);
	}

}
