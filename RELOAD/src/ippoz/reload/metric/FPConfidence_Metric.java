/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class FPConfidence_Metric extends ClassificationConfidenceMetric {

	public FPConfidence_Metric(boolean validAfter) {
		super(MetricType.FP_CONF, false, validAfter);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int classifyMetric(AlgorithmResult tResult) {
		if (!tResult.hasInjection() && tResult.getBooleanScore()) {
			return 1;
		} else return 0;
	}

	@Override
	public String getMetricName() {
		return "FP_CONFIDENCE";
	}

	@Override
	public String getMetricShortName() {
		return "FPCONF";
	}

}
