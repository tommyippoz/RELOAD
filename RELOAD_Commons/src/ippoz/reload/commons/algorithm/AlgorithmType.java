/**
 * 
 */
package ippoz.reload.commons.algorithm;

/**
 * @author Tommy
 *
 */
public enum AlgorithmType {
	
	HBOS, DBSCAN,
	
	ELKI_KMEANS, ELKI_ABOD, ELKI_FASTABOD, ELKI_LOF, ELKI_COF, ELKI_ODIN, ELKI_SVM, ELKI_KNN, ELKI_SOS,
	
	WEKA_ISOLATIONFOREST,
	
	SLIDING_SPS,
	
	SLIDING_ELKI_COF, SLIDING_ELKI_CLUSTERING, SLIDING_ELKI_KNN, SLIDING_ELKI_ABOD,
	
	SLIDING_WEKA_ISOLATIONFOREST 
	
}
