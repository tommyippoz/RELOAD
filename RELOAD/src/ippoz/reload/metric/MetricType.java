/**
 * 
 */
package ippoz.reload.metric;

/**
 * @author Tommy
 *
 */
public enum MetricType {

	TN, TP, FN, FP, FPR, FNR, TPR, PRECISION, RECALL, ACCURACY, FMEASURE, FSCORE, MATTHEWS, AUC, GMEAN, 
	
	CUSTOM, OVERLAP, NO_PREDICTION, THRESHOLDS, SAFESCORE, CONFIDENCE_ERROR,
	
	TN_CONF, TP_CONF, FP_CONF, FN_CONF, 
	
	TP_UNK, FN_UNK, REC_UNK

}
