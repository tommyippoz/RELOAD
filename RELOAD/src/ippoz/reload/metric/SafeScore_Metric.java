/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

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
	public SafeScore_Metric(double beta, boolean validAfter) {
		super(MetricType.SAFESCORE, validAfter);
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
	public double evaluateAnomalyResults(List<? extends AlgorithmResult> anomalyEvaluations) {
		double fpr = new FalsePositiveRate_Metric(isValidAfter()).evaluateAnomalyResults(anomalyEvaluations);
		double r = new Recall_Metric(isValidAfter()).evaluateAnomalyResults(anomalyEvaluations);
		fpr = Math.pow(1 - fpr, 3);
		if (fpr + r > 0)
			return (1 + beta * beta) * fpr * r / (beta * beta * fpr + r);
		else
			return 0.0;
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
