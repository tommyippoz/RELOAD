/**
 * 
 */
package ippoz.multilayer.detector.reputation;

import ippoz.multilayer.detector.commons.data.Snapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

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
	protected double evaluateExperimentReputation(LinkedList<Snapshot> snapList, HashMap<Date, Double> anomalyEvaluations) {
		return repValue;
	}

}
