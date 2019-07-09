/**
 * 
 */
package ippoz.reload.reputation;

import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.TimedValue;
import ippoz.reload.metric.Metric;

import java.util.List;

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
	protected double evaluateExperimentReputation(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		return metric.evaluateAnomalyResults(knowledge, anomalyEvaluations);
	}

}
