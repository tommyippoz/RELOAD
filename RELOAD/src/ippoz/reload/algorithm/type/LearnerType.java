/**
 * 
 */
package ippoz.reload.algorithm.type;

import ippoz.reload.commons.algorithm.AlgorithmType;

/**
 * @author Tommy
 *
 */
public abstract class LearnerType {

	public static LearnerType fromString(String trim) {
		try{
			AlgorithmType at = AlgorithmType.valueOf(trim);
			return new BaseLearner(at);
		} catch(Exception ex){
			return null;
		}
	}

}
