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

	public TNConfidence_Metric(double noPredTHR) {
		super(MetricType.TN_CONF, true, noPredTHR);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int classifyMetric(AlgorithmResult tResult) {
		if (!tResult.isAnomalous() && !tResult.getBooleanScore()) {
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
