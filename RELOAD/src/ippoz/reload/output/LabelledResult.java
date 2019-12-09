/**
 * 
 */
package ippoz.reload.output;

import ippoz.reload.voter.VotingResult;


/**
 * @author Tommy
 *
 */
public class LabelledResult {
	
	private boolean label;
	private VotingResult value;
	
	public LabelledResult(boolean label, VotingResult value) {
		this.label = label;
		this.value = value;
	}
	
	public boolean getLabel() {
		return label;
	}
	
	public VotingResult getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "LabelledResult [label=" + label + ", value=" + value.getScore() + "]";
	}	
	
	

}
