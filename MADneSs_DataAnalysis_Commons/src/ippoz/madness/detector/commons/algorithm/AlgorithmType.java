/**
 * 
 */
package ippoz.madness.detector.commons.algorithm;

/**
 * @author Tommy
 *
 */
public enum AlgorithmType {
	
	RCC, HIST, CONF, WER, INV, PEA, TEST, 
	
	ELKI_KMEANS, ELKI_ABOD, ELKI_FASTABOD, ELKI_LOF, ELKI_COF, ELKI_ODIN, ELKI_SVM,
	
	WEKA_ISOLATIONFOREST,
	
	SLIDING_SPS,
	SLIDING_WEKA_ISOLATIONFOREST
	
}
