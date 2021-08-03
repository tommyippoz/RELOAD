/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.metric.result.MetricResult;

/**
 * @author Tommy
 *
 */
public abstract class BetterBigMetric extends Metric {

	public BetterBigMetric(MetricType mType) {
		super(mType);
	}
	
	public BetterBigMetric(MetricType mType, double noPredTHR) {
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
		else return Double.valueOf(Math.abs(currentMetricValue.getDoubleValue())).compareTo(Math.abs(bestMetricValue.getDoubleValue()));
	}
	
}
