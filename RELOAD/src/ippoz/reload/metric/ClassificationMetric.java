/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.metric.result.ArrayMetricResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class ClassificationMetric extends BetterMaxMetric {

	/** The absolute flag. */
	private boolean absolute;

	/**
	 * Instantiates a new binary classification metric.
	 *
	 * @param absolute
	 *            the absolute flag
	 * @param absolute
	 *            the validAfter flag
	 */
	public ClassificationMetric(MetricType mType, boolean absolute) {
		this(mType, absolute, Double.NaN);
	}
	
	public ClassificationMetric(MetricType mType, boolean absolute,	double noPredictionTHR) {
		super(mType, noPredictionTHR);
		this.absolute = absolute;
	}
		
	@Override
	public MetricResult evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations, ConfusionMatrix confusionMatrix) {
		if(confusionMatrix != null && confusionMatrix.hasMetric(getMetricType()))
			return confusionMatrix.getValueFor(getMetricType(), absolute);
		else return calculateMetric(anomalyEvaluations);
	}
	
	private MetricResult calculateMetric(List<AlgorithmResult> anomalyEvaluations) {
		int detectionHits = 0;
		List<AlgorithmResult> resList = anomalyEvaluations;
		if(Double.isFinite(getNoPredictionThreshold()))
			resList = filterResults(anomalyEvaluations);
		for (int i = 0; i < resList.size(); i++) {
			int d = classifyMetric(resList.get(i));
			detectionHits = detectionHits + d;
		}
		if (resList.size() > 0) {
			if (!absolute){
				return new DoubleMetricResult(100.0 * detectionHits / resList.size());
			} else return new DoubleMetricResult(detectionHits);
		} else return new DoubleMetricResult(0.0);
	}

	private List<AlgorithmResult> filterResults(List<AlgorithmResult> anomalyEvaluations) {
		ArrayMetricResult mr = (ArrayMetricResult) new NoPredictionArea_Metric(getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, null);
		double[] arr = mr.getArray();
		if(mr.getDoubleValue() > 0.0 && !Double.isNaN(arr[2]) && !Double.isNaN(arr[3])){
			List<AlgorithmResult> resList = new LinkedList<>();
			for(AlgorithmResult ar : anomalyEvaluations){
				if(ar.getScoreEvaluation() == AnomalyResult.ANOMALY || ar.getScore() < arr[2] || ar.getScore() > arr[3])
					resList.add(ar);
			}
			return resList;
		} else return anomalyEvaluations;
	}
	protected abstract int classifyMetric(AlgorithmResult tResult);

}
