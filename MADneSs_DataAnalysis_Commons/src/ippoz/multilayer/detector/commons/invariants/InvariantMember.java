/**
 * 
 */
package ippoz.multilayer.detector.commons.invariants;

import ippoz.multilayer.detector.commons.data.MultipleSnapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;

/**
 * @author Tommy
 *
 */
public abstract class InvariantMember {
	
	private Class<?> memberType;
	private String memberName;
		
	public InvariantMember(Class<?> memberType, String memberName) {
		this.memberType = memberType;
		this.memberName = memberName;
	}
	
	public String getMemberName(){
		return memberName;
	}

	public Object getValue(MultipleSnapshot snapshot){
		return memberType.cast(getStringValue(snapshot));
	}
	
	public Object getValueFromRaw(String rawData){
		return memberType.cast(rawData);
	}
	
	public abstract String getStringValue(MultipleSnapshot snapshot);
	
	public Double getDoubleValue(MultipleSnapshot snapshot){
		return Double.parseDouble(getStringValue(snapshot));
	}
	
	public Long getLongValue(MultipleSnapshot snapshot){
		return Long.parseLong(getStringValue(snapshot));
	}
	
	public abstract String toString();

	public abstract boolean contains(DataSeries serie);

}
