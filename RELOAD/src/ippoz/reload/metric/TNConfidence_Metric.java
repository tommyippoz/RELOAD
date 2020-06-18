/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class TNConfidence_Metric extends ClassificationConfidenceMetric {

	public TNConfidence_Metric(boolean validAfter) {
		super(MetricType.TN_CONF, true, validAfter);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int classifyMetric(AlgorithmResult tResult) {
		if (!tResult.hasInjection() && !tResult.getBooleanScore()) {
			return 1;
		} else return 0;
	}

	@Override
	public String getMetricName() {
		return "TN_CONFIDENCE";
	}

	@Override
	public String getMetricShortName() {
		return "TNCONF";
	}

}
