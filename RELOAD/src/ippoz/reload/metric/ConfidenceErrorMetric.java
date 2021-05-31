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
public class ConfidenceErrorMetric extends BetterMaxMetric {
	
	private double fpfnRate;

	public ConfidenceErrorMetric(double fpfnRate, double noPredTHR) {
		super(MetricType.CONFIDENCE_ERROR, noPredTHR);
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
	public MetricResult evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations) {
		int fpHits = 0, fnHits = 0, tHits = 0;
		double fpScore = 0, fnScore = 0, tScore = 0;
		AlgorithmResult tResult;
		for (int i = 0; i < anomalyEvaluations.size(); i++) {
			tResult = anomalyEvaluations.get(i);
			if (!tResult.isAnomalous() && tResult.getBooleanScore()) {
				fpHits++;
				fpScore = fpScore + (tResult.getConfidence() > 1 ? 1.0 : tResult.getConfidence());
			}
			else if (tResult.isAnomalous() && !tResult.getBooleanScore()) {
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
			return new DoubleMetricResult(tScore / tHits - val);
		else return new DoubleMetricResult(-val);
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
