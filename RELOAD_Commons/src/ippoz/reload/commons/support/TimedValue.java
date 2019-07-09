/**
 * 
 */
package ippoz.reload.commons.support;

import java.util.Date;

/**
 * @author Tommy
 *
 */
public class TimedValue {
	
	private Date vDate;
	private Double value;
	
	public TimedValue(Date vDate, Double value) {
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
