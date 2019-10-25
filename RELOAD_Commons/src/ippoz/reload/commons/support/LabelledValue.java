/**
 * 
 */
package ippoz.reload.commons.support;


/**
 * @author Tommy
 *
 */
public class LabelledValue {
	
	private boolean label;
	
	private Double value;
	
	public LabelledValue(Double value, boolean label) {
		this.label = label;
		this.value = value;
	}
	
	public boolean getLabel() {
		return label;
	}
	
	public Double getValue() {
		return value;
	}	

}
