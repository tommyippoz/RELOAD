/**
 * 
 */
package ippoz.reload.reputation;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.ConfusionMatrix;

import java.util.List;

/**
 * The Class ConstantReputation.
 *
 * @author Tommy
 */
public class ConstantReputation extends Reputation {

	/** The constant reputation value. */
	private double repValue;
	
	/**
	 * Instantiates a new constant reputation.
	 *
	 * @param reputationTag the reputation tag
	 * @param value the reputation value
	 */
	public ConstantReputation(String reputationTag, double value) {
		super(reputationTag);
		repValue = value;
	}

	@Override
	protected double evaluateExperimentReputation(List<AlgorithmResult> anomalyEvaluations, ConfusionMatrix confusionMatrix) {
		return repValue;
	}

}
