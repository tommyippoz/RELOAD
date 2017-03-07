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
public class ConstantMember extends InvariantMember {

	private String baseValue;
	
	public ConstantMember(Class<?> memberType, String baseValue) {
		super(memberType, "Constant");
		this.baseValue = baseValue;
	}

	@Override
	public String getStringValue(MultipleSnapshot snapshot) {
		return String.valueOf(getValueFromRaw(baseValue));
	}

	@Override
	public String toString() {
		return baseValue;
	}

	@Override
	public boolean contains(DataSeries serie) {
		return false;
	}

}
