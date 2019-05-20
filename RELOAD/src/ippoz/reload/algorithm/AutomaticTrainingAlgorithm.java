/**
 * 
 */
package ippoz.reload.algorithm;

import ippoz.reload.commons.knowledge.Knowledge;

import java.util.List;

/**
 * The Interface AutomaticTrainingAlgorithm.
 * Implement this if you are creating an algorithm which is needing of a custom training phase (e.g., clustering, decision trees, pattern recognition)
 *
 * @author Tommy
 */
public interface AutomaticTrainingAlgorithm {

	/**
	 * Automatic training.
	 *
	 * @param kList the training data
	 * @return the chosen algorithm configuration
	 */
	public boolean automaticTraining(List<Knowledge> kList, boolean createOutput);
	
}
