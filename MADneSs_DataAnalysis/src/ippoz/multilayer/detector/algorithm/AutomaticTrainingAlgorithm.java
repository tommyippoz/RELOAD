/**
 * 
 */
package ippoz.multilayer.detector.algorithm;

import java.util.HashMap;
import java.util.LinkedList;

import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.Snapshot;

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
	 * @param algExpSnapshots the training data, formatted as an hashmap with experimentName -> list of snapshots
	 * @return the chosen algorithm configuration
	 */
	public AlgorithmConfiguration automaticTraining(HashMap<String, LinkedList<Snapshot>> algExpSnapshots);
	
}
