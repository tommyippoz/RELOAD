/**
 * 
 */
package ippoz.multilayer.detector.reputation;

import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.metric.Metric;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class MetricReputation.
 * Calculates a reputation depending on a chosen metric.
 *
 * @author Tommy
 */
public class MetricReputation extends Reputation {
	
	/** The metric linked to the reputation. */
	private Metric metric;

	/**
	 * Instantiates a new metric reputation.
	 *
	 * @param reputationTag the reputation tag
	 * @param metric the linked metric
	 */
	public MetricReputation(String reputationTag, Metric metric) {
		super(reputationTag);
		this.metric = metric;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.reputation.Reputation#evaluateExperimentReputation(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	protected double evaluateExperimentReputation(LinkedList<Snapshot> snapList, HashMap<Date, Double> anomalyEvaluations) {
		return metric.evaluateAnomalyResults(snapList, anomalyEvaluations);
	}

}
