/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;

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

	protected boolean isValidSnapshot(Snapshot snap, InjectedElement injElement) {
		if (injElement != null) {
			if (injElement.getTimestamp().compareTo(snap.getTimestamp()) == 0)
				return true;
			else {
				if (!validAfter)
					return snap.getTimestamp()
							.before(injElement.getTimestamp());
				else
					return !injElement.compliesWith(snap.getTimestamp());
			}
		} else
			return true;
	}

}
