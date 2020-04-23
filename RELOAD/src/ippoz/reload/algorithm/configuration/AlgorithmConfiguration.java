/**
 * 
 */
package ippoz.reload.algorithm.configuration;

import java.util.HashMap;

import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.algorithm.AlgorithmType;

/**
 * @author Tommy
 *
 */
public class AlgorithmConfiguration extends BasicConfiguration {
	
	/** The algorithm type */
	private AlgorithmType algType;
	
	/**
	 * Instantiates a new algorithm configuration.
	 */
	public AlgorithmConfiguration(AlgorithmType algType){
		super();
		this.algType = algType;
	}
	
	/**
	 * Gets the algorithm type.
	 *
	 * @return the algType
	 */
	public AlgorithmType getAlgorithmType(){
		return algType;
	}
	
	@Override
	public String toString() {
		return algType + ":" + super.toString();
	}

	@Override
	public LearnerType getLearnerType() {
		return new BaseLearner(algType);
	}

}
