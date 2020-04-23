/**
 * 
 */
package ippoz.reload.algorithm.type;

import ippoz.reload.commons.algorithm.AlgorithmType;

/**
 * @author Tommy
 *
 */
public class BaseLearner extends LearnerType {
	
	private AlgorithmType algType;

	public BaseLearner(AlgorithmType algType) {
		super();
		this.algType = algType;
	}

	public AlgorithmType getAlgType() {
		return algType;
	}	

}
