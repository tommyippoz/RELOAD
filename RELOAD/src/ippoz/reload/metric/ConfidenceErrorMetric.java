/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.support.TimedResult;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class ConfidenceErrorMetric extends BetterMinMetric {
	
	private double fpfnRate;

	public ConfidenceErrorMetric(boolean validAfter, double fpfnRate) {
		super(MetricType.CONFIDENCE_ERROR, validAfter);
		this.fpfnRate = fpfnRate;
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
		int fpHits = 0, fnHits = 0, tHits = 0;
		double fpScore = 0, fnScore = 0, tScore = 0;
		AlgorithmResult tResult;
		for (int i = 0; i < anomalyEvaluations.size(); i++) {
			tResult = anomalyEvaluations.get(i);
			if (tResult.getInjection() == null && tResult.getBooleanScore()) {
				fpHits++;
				fpScore = fpScore + (tResult.getConfidence() > 1 ? 1.0 : tResult.getConfidence());
			}
			else if (tResult.getInjection() != null && !tResult.getBooleanScore()) {
				fnHits++;
				fnScore = fnScore + (tResult.getConfidence() > 1 ? 1.0 : tResult.getConfidence());
			} else {
				tHits++;
				tScore = tScore + (tResult.getConfidence() > 1 ? 1.0 : tResult.getConfidence());
			}
		}
		if(fpHits > 0)
			fpScore = fpScore / fpHits;
		if(fnHits > 0)
			fnScore = fnScore / fnHits;
		if(tHits > 0)
			tScore = tScore / tHits;
		double v = (fnScore*fpfnRate + fpScore) / (fpfnRate + 1);
		System.out.println(fpHits + " _ " + fnHits + " : " + v + " - " + tScore);
		return (1- new Accuracy_Metric(isValidAfter()).evaluateAnomalyResults(anomalyEvaluations)) * v / tScore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "ConfErr";
	}

	@Override
	public String getMetricShortName() {
		return "ConfidenceError";
	}


}
