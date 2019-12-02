/**
 * 
 */
package ippoz.reload.metric;

/**
 * @author Tommy
 *
 */
public abstract class BetterBigMetric extends ScoringMetric {

	public BetterBigMetric(MetricType mType, boolean validAfter) {
		super(mType, validAfter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ippoz.multilayer.detector.metric.Metric#compareResults(double,
	 * double)
	 */
	@Override
	public int compareResults(double currentMetricValue, double bestMetricValue) {
		return Double.valueOf(Math.abs(currentMetricValue)).compareTo(Math.abs(bestMetricValue));
	}
	
}
