/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.result.ClusteringResult;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;

/**
 * The Class KMeansDecision. Defines data point as anomalous if its distance is bigger than either 
 * std or var of the data points of the nearest cluster.
 *
 * @author Tommy
 */
public class ClusterDecision extends DecisionFunction {
	
	/** The function text. */
	private String functionText;

	/**
	 * Instantiates a new k means decision.
	 *
	 * @param functionText the function text
	 */
	public ClusterDecision(String functionText, boolean revertFlag) {
		super("cluster", DecisionFunctionType.CLUSTER, revertFlag);
		this.functionText = functionText;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#classify(ippoz.reload.algorithm.result.AlgorithmResult)
	 */
	@Override
	public AnomalyResult classify(AlgorithmResult aResult) {
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

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#toCompactString()
	 */
	@Override
	public String toCompactString() {
		return "CLUSTER(" + functionText + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getClassifierTag()
	 */
	@Override
	public String getClassifierTag() {
		return "CLUSTER(" + functionText + ")";
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.decisionfunction.DecisionFunction#getThresholds()
	 */
	@Override
	public double[] getThresholds() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
