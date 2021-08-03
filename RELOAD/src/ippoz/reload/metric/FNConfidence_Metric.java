/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class FNConfidence_Metric extends ClassificationConfidenceMetric {

	public FNConfidence_Metric(double noPredTHR) {
		super(MetricType.FN_CONF, false, noPredTHR);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int classifyMetric(AlgorithmResult tResult) {
		if (tResult.isAnomalous() && !tResult.getBooleanScore()) {
			return 1;
		} else return 0;
	}

	@Override
	public String getMetricName() {
		return "FN_CONFIDENCE";
	}

	@Override
	public String getMetricShortName() {
		return "FNCONF";
	}

}
