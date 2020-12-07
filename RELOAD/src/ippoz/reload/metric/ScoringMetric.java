/**
 * 
 */
package ippoz.reload.metric;


/**
 * @author Tommy
 *
 */
public abstract class ScoringMetric extends Metric {

	private boolean validAfter;

	public ScoringMetric(MetricType mType, boolean validAfter) {
		super(mType);
		this.validAfter = validAfter;
	}

	protected boolean isValidAfter() {
		return validAfter;
	}

}
