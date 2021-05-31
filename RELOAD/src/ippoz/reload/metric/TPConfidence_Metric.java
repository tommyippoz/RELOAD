/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

/**
 * @author Tommy
 *
 */
public class TPConfidence_Metric extends ClassificationConfidenceMetric {

	public TPConfidence_Metric(double noPredTHR) {
		super(MetricType.TP_CONF, true, noPredTHR);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected int classifyMetric(AlgorithmResult tResult) {
		if (tResult.isAnomalous() && tResult.getBooleanScore()) {
			return 1;
		} else return 0;
	}

	@Override
	public String getMetricName() {
		return "TP_CONFIDENCE";
	}

	@Override
	public String getMetricShortName() {
		return "TPCONF";
	}

}
