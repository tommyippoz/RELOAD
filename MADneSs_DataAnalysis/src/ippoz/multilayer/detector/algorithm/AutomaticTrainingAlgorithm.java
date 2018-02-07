/**
 * 
 */
package ippoz.multilayer.detector.algorithm;

import ippoz.multilayer.detector.commons.data.ExperimentData;

import java.util.LinkedList;

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
	 * @param expList the training data, formatted as an hashmap with experimentName -> list of snapshots
	 * @return the chosen algorithm configuration
	 */
	public void automaticTraining(LinkedList<ExperimentData> expList);
	
}
