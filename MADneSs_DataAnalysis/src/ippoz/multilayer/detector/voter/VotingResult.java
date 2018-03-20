/**
 * 
 */
package ippoz.multilayer.detector.voter;

import ippoz.multilayer.detector.commons.support.AppUtility;

import java.util.Date;

/**
 * @author Tommy
 *
 */
public class VotingResult {
	
	private Date vDate;
	private Double value;
	
	public VotingResult(Date vDate, Double value) {
		this.vDate = vDate;
		this.value = value;
	}
	
	public Date getDate() {
		return vDate;
	}
	
	public Double getValue() {
		return value;
	}
	
	public Double getDateOffset(Date ref){
		if(ref != null)
			return AppUtility.getSecondsBetween(vDate, ref);
		else return 0.0;
	}
	
	

}
