/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class FN_Unk_Metric extends ClassificationMetric {

	/**
	 * Instantiates a new fn_ metric.
	 *
	 * @param absolute
	 *            the absolute flag
	 */
	public FN_Unk_Metric(boolean absolute) {
		super(MetricType.FN_UNK, absolute);
	}
	
	public FN_Unk_Metric(boolean absolute, double noPredictionTHR) {
		super(MetricType.FN_UNK, absolute, noPredictionTHR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "False Negatives Unk";
	}

	@Override
	public String getMetricShortName() {
		return "FNUnk";
	}

	@Override
	protected int classifyMetric(AlgorithmResult tResult) {
		if (tResult.isUnknown() && tResult.isAnomalous() && !tResult.getBooleanScore()) {
			return 1;
		} else return 0;
	}

}
