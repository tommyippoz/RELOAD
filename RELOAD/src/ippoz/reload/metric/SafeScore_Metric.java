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
public class SafeScore_Metric extends BetterMaxMetric {

	/** The beta parameter. */
	private double beta;

	/**
	 * Instantiates a new fscore_metric.
	 *
	 * @param beta
	 *            the beta parameter of f-score
	 */
	public SafeScore_Metric(double beta, double noPredTHR) {
		super(MetricType.SAFESCORE, noPredTHR);
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
	public MetricResult evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations) {
		double fpr = new FalsePositiveRate_Metric(getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		double r = new Recall_Metric(getNoPredictionThreshold()).evaluateAnomalyResults(anomalyEvaluations).getDoubleValue();
		fpr = Math.pow(1 - fpr, 3);
		if (fpr + r > 0)
			return new DoubleMetricResult((1 + beta * beta) * fpr * r / (beta * beta * fpr + r));
		else return new DoubleMetricResult(0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "SScore(" + beta + ")";
	}

	@Override
	public String getMetricShortName() {
		return "S" + (int) beta;
	}

}
