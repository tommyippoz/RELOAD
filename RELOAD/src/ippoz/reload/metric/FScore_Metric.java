/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.List;

/**
 * The Class FScore_Metric. Implements the F-Score(b) metric
 *
 * @author Tommy
 */
public class FScore_Metric extends BetterMaxMetric {

	/** The beta parameter. */
	private double beta;

	/**
	 * Instantiates a new fscore_metric.
	 *
	 * @param beta
	 *            the beta parameter of f-score
	 */
	public FScore_Metric(double beta, double noPredTHR) {
		super(MetricType.FSCORE, noPredTHR);
		this.beta = beta;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.
	 * multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public MetricResult evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations, ConfusionMatrix confusionMatrix) {
		double p = new Precision_Metric(getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		double r = new Recall_Metric(getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations, confusionMatrix).getDoubleValue();
		if (p + r > 0)
			return new DoubleMetricResult((1 + beta * beta) * p * r / (beta * beta * p + r));
		else return new DoubleMetricResult(0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "FScore(" + beta + ")";
	}

	@Override
	public String getMetricShortName() {
		return "F" + (int) beta;
	}

}
