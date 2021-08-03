/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class ClassificationConfidenceMetric extends BetterMaxMetric {

	/** The absolute flag. */
	private boolean betterMax;

	/**
	 * Instantiates a new binary classification metric.
	 *
	 * @param absolute
	 *            the absolute flag
	 * @param absolute
	 *            the validAfter flag
	 */
	public ClassificationConfidenceMetric(MetricType mType, boolean betterMax, double noPredTHR) {
		super(mType, noPredTHR);
		this.betterMax = betterMax;
	}
		
	@Override
	public MetricResult evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations, ConfusionMatrix confusionMatrix) {
		int detectionHits = 0;
		double confSum = 0.0;
		for (int i = 0; i < anomalyEvaluations.size(); i++) {
			AlgorithmResult ar = anomalyEvaluations.get(i);
			int d = classifyMetric(ar);
			if(d > 0){
				detectionHits = detectionHits + d;
				confSum = confSum + ar.getConfidence(); 
			}
			
		}
		if (anomalyEvaluations.size() > 0) {
			return new DoubleMetricResult(1.0 * confSum / detectionHits);
		} else return new DoubleMetricResult(Double.NaN);
	}
	
	

	@Override
	public int compareResults(MetricResult currentMetricValue, MetricResult bestMetricValue) {
		int res = super.compareResults(currentMetricValue, bestMetricValue);
		if(betterMax)
			return res;
		else return -res;
	}

	protected abstract int classifyMetric(AlgorithmResult tResult);

}
