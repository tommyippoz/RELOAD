/**
 * 
 */
package ippoz.multilayer.detector.commons.invariants;

import ippoz.multilayer.detector.commons.data.MultipleSnapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.support.AppUtility;

/**
 * @author Tommy
 *
 */
public class Invariant {
	
	private final static String[] opList = {">", "<", ">=", "<=", "=", "!="};
	
	private InvariantMember firstMember;
	private InvariantMember secondMember;
	private String operand;
	
	public Invariant(InvariantMember firstMember, InvariantMember secondMember, String operand) {
		this.firstMember = firstMember;
		this.secondMember = secondMember;
		this.operand = operand;
	}

	public Invariant(String readString) {
		int opIndex = -1;
		for(String op : opList){
			if(readString.contains(op)){
				operand = op;
				opIndex = readString.indexOf(op);
				break;
			}
		}
		firstMember = getMemberFromString(readString.substring(0, opIndex).trim());
		secondMember = getMemberFromString(readString.substring(opIndex+1).trim());
	}
	
	private InvariantMember getMemberFromString(String mString){
		if(AppUtility.isNumber(mString))
			return new ConstantMember(Double.class, mString);
		else {
			return new DataSeriesMember(DataSeries.fromString(mString, false));
		}
	}

	public InvariantMember getFirstMember() {
		return firstMember;
	}

	public InvariantMember getSecondMember() {
		return secondMember;
	}
	
	public boolean evaluateInvariant(MultipleSnapshot sysSnapshot){
		return evaluateOperand(firstMember.getDoubleValue(sysSnapshot), secondMember.getDoubleValue(sysSnapshot));
	}
	
	private boolean evaluateOperand(double val1, double val2){
		switch(operand){
			case ">":
				return val1 > val2;
			case "<":
				return val1 < val2;
			case "=":
			case "==":
				return val1 == val2;
			case ">=":
				return val1 >= val2;
			case "<=":
				return val1 <= val2;
			case "!=":
				return val1 != val2;
		}
		return false;
	}

	@Override
	public String toString() {
		return firstMember.toString() + " " + operand + " " + secondMember.toString();
	}

	public boolean contains(DataSeries serie) {
		return firstMember.contains(serie) || secondMember.contains(serie);
	}		

}
