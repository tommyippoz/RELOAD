/**
 * 
 */
package ippoz.reload.reputation;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.ConfusionMatrix;
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

	@Override
	protected double evaluateExperimentReputation(List<AlgorithmResult> anomalyEvaluations, ConfusionMatrix confusionMatrix) {
		return metric.evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
	}

}
