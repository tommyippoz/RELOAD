/**
 * 
 */
package ippoz.madness.detector.decisionfunction;

import ippoz.madness.detector.algorithm.result.AlgorithmResult;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.commons.support.ValueSeries;
import ippoz.utils.logging.AppLogger;

/**
 * @author Tommy
 *
 */
public abstract class DecisionFunction {
	
	private String classifierName;
	
	private DecisionFunctionType classifierType;
	
	protected DecisionFunction(String classifierName, DecisionFunctionType classifierType) {
		this.classifierName = classifierName;
		this.classifierType = classifierType;
	}
	
	public static DecisionFunction getClassifier(ValueSeries scores, String thresholdTag){
		String partial;
		if(thresholdTag != null && thresholdTag.length() > 0){
			thresholdTag = thresholdTag.trim();
			if(thresholdTag.length() > 0) {
				if(AppUtility.isNumber(thresholdTag)){
					double ratio = Double.parseDouble(thresholdTag);
					return new ThresholdDecision(ratio, scores);
				} else if(thresholdTag.endsWith("%")) {
					
				} else if(thresholdTag.contains("IQR")) {
					if(scores != null && scores.size() > 0){
						if(thresholdTag.equals("IQR"))
							return new IQRFunction(1.5, scores.getQ1(), scores.getQ3());
						else if(thresholdTag.equals("LEFT_IQR"))
							return new LeftIQRFunction(1.5, scores.getQ1(), scores.getQ3());
						else if(thresholdTag.equals("RIGHT_IQR"))
							return new RightIQRFunction(1.5, scores.getQ1(), scores.getQ3());
						else if(thresholdTag.contains("(") && thresholdTag.contains(")")){
							partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
							if(partial != null && partial.length() > 0 && AppUtility.isNumber(partial)){
								return new IQRFunction(Double.parseDouble(partial), scores.getQ1(), scores.getQ3());
							} else AppLogger.logInfo(DecisionFunction.class, "Parameters of IQR '" + thresholdTag + "' cannot be parsed");
						} else AppLogger.logInfo(DecisionFunction.class, "Parameters of IQR '" + thresholdTag + "' cannot be parsed");
					} else AppLogger.logError(DecisionFunction.class, "DecisionFunctionCreation", "Unable to create IQR decision function '" + thresholdTag + "'");
				} else if (thresholdTag.contains("CLUSTER")){
					if(thresholdTag.contains("(") && thresholdTag.contains(")")){
						partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
						return new ClusterDecision(partial);
					} AppLogger.logInfo(DecisionFunction.class, "Parameters of cluster '" + thresholdTag + "' cannot be parsed");
				} else AppLogger.logError(DecisionFunction.class, "DecisionFunctionCreation", "Unable to create decision function '" + thresholdTag + "'");
			} else AppLogger.logError(DecisionFunction.class, "DecisionFunctionCreation", "null tag for decision function");
		}
		return null;
	}

	public String getClassifierName() {
		return classifierName;
	}

	public DecisionFunctionType getClassifierType() {
		return classifierType;
	}
	
	public void classifyScore(AlgorithmResult aResult){
		if(aResult != null){
			aResult.setDecisionFunction(this);
			aResult.setScoreEvaluation(classify(aResult));
		}
	}

	protected abstract AnomalyResult classify(AlgorithmResult aResult);

}
