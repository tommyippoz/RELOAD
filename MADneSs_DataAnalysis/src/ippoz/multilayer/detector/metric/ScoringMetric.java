/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.failure.InjectedElement;

/**
 * @author Tommy
 *
 */
public abstract class ScoringMetric extends Metric {
	
	private boolean validAfter;

	public ScoringMetric(boolean validAfter){
		this.validAfter = validAfter;
	}
	
	protected boolean isValidAfter(){
		return validAfter;
	}
	
	protected boolean isValidSnapshot(Snapshot snap, InjectedElement injElement){
		if(injElement != null) {
			if(injElement.getTimestamp().compareTo(snap.getTimestamp()) == 0)
				return true;
			else if(!validAfter)
				return snap.getTimestamp().before(injElement.getTimestamp()) || snap.getTimestamp().compareTo(injElement.getTimestamp()) == 0;
			else
				return !injElement.compliesWith(snap.getTimestamp());
		} else return true;
	}

}
