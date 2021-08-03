/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class TP_Unk_Metric extends ClassificationMetric {

	/**
	 * Instantiates a new tp_ metric.
	 *
	 * @param absolute
	 *            the absolute flag
	 */
	public TP_Unk_Metric(boolean absolute) {
		super(MetricType.TP_UNK, absolute);
	}

	public TP_Unk_Metric(boolean absolute, double noPredictionTHR) {
		super(MetricType.TP_UNK, absolute, noPredictionTHR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "True Positives Unk";
	}

	@Override
	public String getMetricShortName() {
		return "TPUnk";
	}

	@Override
	protected int classifyMetric(AlgorithmResult tResult) {
		if(tResult.isUnknown() && tResult.isAnomalous() && tResult.getBooleanScore()) {
			return 1;
		} else return 0;
	}

}
