/**
 * 
 */
package ippoz.madness.detector.decisionfunction;

import ippoz.madness.detector.algorithm.result.AlgorithmResult;
import ippoz.madness.detector.algorithm.result.ClusteringResult;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;

/**
 * @author Tommy
 *
 */
public class ClusterDecision extends DecisionFunction {
	
	private String functionText;

	public ClusterDecision(String functionText) {
		super("cluster", DecisionFunctionType.CLUSTER);
		this.functionText = functionText;
	}

	@Override
	protected AnomalyResult classify(AlgorithmResult aResult) {
		String partialText;
		ClusteringResult cr = (ClusteringResult)aResult;
		double clusterVariance = cr.getClusterVariance();
		double score = cr.getScore();
		if(functionText.contains("VAR")){
			partialText = functionText.replace("VAR", "");
			if(partialText.trim().length() == 0){
				return score > clusterVariance ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
			} else if(AppUtility.isNumber(partialText.trim())){
				return score > Double.parseDouble(partialText.trim())*clusterVariance ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
			} else {
				AppLogger.logError(getClass(), "InputError", "Unable to process '" + partialText + "' threshold");
				return AnomalyResult.ERROR;
			}
		} else if(functionText.contains("STD")){
			partialText = functionText.replace("STD", "");
			if(partialText.trim().length() == 0){
				return score > Math.sqrt(Math.abs(clusterVariance)) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
			} else if(AppUtility.isNumber(partialText.trim())){
				return score > Double.parseDouble(partialText.trim())*Math.sqrt(Math.abs(clusterVariance)) ? AnomalyResult.ANOMALY : AnomalyResult.NORMAL;
			} else {
				AppLogger.logError(getClass(), "InputError", "Unable to process '" + partialText + "' threshold");
				return AnomalyResult.ERROR;
			}
		} else {
			AppLogger.logError(getClass(), "InputError", "Unable to process '" + functionText + "' threshold");
			return AnomalyResult.ERROR;
		}
	}

	@Override
	public String toCompactString() {
		return "CLUSTER(" + functionText + ")";
	}

}
