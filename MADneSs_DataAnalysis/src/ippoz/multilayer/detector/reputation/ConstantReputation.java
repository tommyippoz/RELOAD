/**
 * 
 */
package ippoz.multilayer.detector.reputation;

import ippoz.multilayer.detector.commons.knowledge.Knowledge;
import ippoz.multilayer.detector.commons.support.TimedValue;

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

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.reputation.Reputation#evaluateExperimentReputation(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	protected double evaluateExperimentReputation(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		return repValue;
	}

}
