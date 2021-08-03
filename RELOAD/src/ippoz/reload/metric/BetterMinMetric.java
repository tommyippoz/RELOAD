/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.metric.result.MetricResult;

/**
 * The Class BetterMinMetric. Identifies a metric that is better if it is low.
 *
 * @author Tommy
 */
public abstract class BetterMinMetric extends Metric {

	public BetterMinMetric(MetricType mType, double noPredTHR) {
		super(mType, noPredTHR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#compareResults(double,
	 * double)
	 */
	@Override
	public int compareResults(MetricResult currentMetricValue, MetricResult bestMetricValue) {
		if(!Double.isFinite(bestMetricValue.getDoubleValue()))
			return 1;
		else if(!Double.isFinite(currentMetricValue.getDoubleValue()))
			return -1;
		else return Double.valueOf(bestMetricValue.getDoubleValue()).compareTo(currentMetricValue.getDoubleValue());
	}

}
