/**
 * 
 */
package ippoz.multilayer.detector.metric;

/**
 * The Class BetterMaxMetric.
 * Identifies a metric that is better if it is high.
 *
 * @author Tommy
 */
public abstract class BetterMaxMetric extends ScoringMetric {

	public BetterMaxMetric(boolean validAfter) {
		super(validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#compareResults(double, double)
	 */
	@Override
	public int compareResults(double currentMetricValue, double bestMetricValue) {
		return Double.valueOf(currentMetricValue).compareTo(bestMetricValue);
	}
	
}
