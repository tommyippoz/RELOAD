/**
 * 
 */
package ippoz.reload.commons.support;

import ippoz.reload.commons.failure.InjectedElement;

import java.util.Date;

/**
 * @author Tommy
 *
 */
public class TimedResult extends TimedValue {
	
	private Double algorithmScore;
	
	private InjectedElement injectedElement;

	public TimedResult(Date vDate, Double value, Double algorithmScore, InjectedElement injectedElement) {
		super(vDate, value);
		this.algorithmScore = algorithmScore;
		this.injectedElement = injectedElement;
	}
	
	public Double getAlgorithmScore(){
		return algorithmScore;
	}
	
	public InjectedElement getInjectedElement(){
		return injectedElement;
	}
 
}
