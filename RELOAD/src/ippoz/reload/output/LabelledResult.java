/**
 * 
 */
package ippoz.reload.output;

import ippoz.reload.algorithm.result.AlgorithmResult;


/**
 * @author Tommy
 *
 */
public class LabelledResult {
	
	private boolean label;
	private AlgorithmResult value;
	
	public LabelledResult(boolean label, AlgorithmResult value) {
		this.label = label;
		this.value = value;
	}
	
	public boolean getLabel() {
		return label;
	}
	
	public AlgorithmResult getValue() {
		return value;
	}	

}
