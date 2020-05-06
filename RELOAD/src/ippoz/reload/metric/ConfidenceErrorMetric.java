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
public class ConfidenceErrorMetric extends BetterMaxMetric {
	
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
	public double evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations) {
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
		double val = 0;
		if(fpHits > 0){
			fpScore = fpScore / fpHits;
			if(fnHits > 0){
				fnScore = fnScore / fnHits;
				val = (fnScore*fpfnRate + fpScore) / (fpfnRate + 1);
			} else val = fpScore;
		} else if(fnHits > 0){
			val = fnScore / fnHits;
		} else val = 0;
		if(tHits > 0)
			return tScore / tHits - val;
		else return - val;
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
