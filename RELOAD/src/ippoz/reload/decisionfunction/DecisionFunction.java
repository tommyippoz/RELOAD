/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;
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
								if(thresholdTag.contains("LEFT_IQR"))
									return new LeftIQRFunction(Double.parseDouble(partial), scores.getQ1(), scores.getQ3());
								else if(thresholdTag.contains("RIGHT_IQR"))
									return new RightIQRFunction(Double.parseDouble(partial), scores.getQ1(), scores.getQ3());
								else return new IQRFunction(Double.parseDouble(partial), scores.getQ1(), scores.getQ3());
							} else AppLogger.logInfo(DecisionFunction.class, "Parameters of IQR '" + thresholdTag + "' cannot be parsed");
						} else AppLogger.logInfo(DecisionFunction.class, "Parameters of IQR '" + thresholdTag + "' cannot be parsed");
					} else AppLogger.logError(DecisionFunction.class, "DecisionFunctionCreation", "Unable to create IQR decision function '" + thresholdTag + "'");
				} else if(thresholdTag.contains("CONFIDENCE_INTERVAL")) {
					if(scores != null && scores.size() > 0){
						if(thresholdTag.equals("CONFIDENCE_INTERVAL"))
							return new ConfidenceIntervalFunction(1.0, scores.getAvg(), scores.getStd());
						else if(thresholdTag.equals("LEFT_CONFIDENCE_INTERVAL"))
							return new LeftConfidenceIntervalFunction(1.0, scores.getAvg(), scores.getStd());
						else if(thresholdTag.equals("RIGHT_CONFIDENCE_INTERVAL"))
							return new RightConfidenceIntervalFunction(1.0, scores.getAvg(), scores.getStd());
						else if(thresholdTag.contains("(") && thresholdTag.contains(")")){
							partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
							if(partial != null && partial.length() > 0 && AppUtility.isNumber(partial)){
								if(thresholdTag.contains("LEFT_CONFIDENCE_INTERVAL"))
									return new LeftConfidenceIntervalFunction(Double.parseDouble(partial), scores.getAvg(), scores.getStd());
								else if(thresholdTag.contains("RIGHT_CONFIDENCE_INTERVAL"))
									return new RightConfidenceIntervalFunction(Double.parseDouble(partial), scores.getAvg(), scores.getStd());
								else return new ConfidenceIntervalFunction(Double.parseDouble(partial), scores.getAvg(), scores.getStd());
							} else AppLogger.logInfo(DecisionFunction.class, "Parameters of CONF '" + thresholdTag + "' cannot be parsed");
						} else AppLogger.logInfo(DecisionFunction.class, "Parameters of CONF '" + thresholdTag + "' cannot be parsed");
					} else AppLogger.logError(DecisionFunction.class, "DecisionFunctionCreation", "Unable to create CONF decision function '" + thresholdTag + "'");
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
	
	public static boolean checkClassifier(String thresholdTag){
		String partial;
		if(thresholdTag != null && thresholdTag.length() > 0){
			thresholdTag = thresholdTag.trim();
			if(thresholdTag.length() > 0) {
				if(AppUtility.isNumber(thresholdTag)){
					return true;
				} else if(thresholdTag.endsWith("%")) {
					return true;
				} else if(thresholdTag.contains("IQR")) {
					if(thresholdTag.equals("IQR"))
						return true;
					else if(thresholdTag.equals("LEFT_IQR"))
						return true;
					else if(thresholdTag.equals("RIGHT_IQR"))
						return true;
					else if(thresholdTag.contains("(") && thresholdTag.contains(")")){
						partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
						if(partial != null && partial.length() > 0 && AppUtility.isNumber(partial)){
							return true;
						} else return false;
					} else return false;
				} else if(thresholdTag.contains("CONFIDENCE_INTERVAL")) {
					if(thresholdTag.equals("CONFIDENCE_INTERVAL"))
						return true;
					else if(thresholdTag.equals("LEFT_CONFIDENCE_INTERVAL"))
						return true;
					else if(thresholdTag.equals("RIGHT_CONFIDENCE_INTERVAL"))
						return true;
					else if(thresholdTag.contains("(") && thresholdTag.contains(")")){
						partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
						if(partial != null && partial.length() > 0 && AppUtility.isNumber(partial)){
							return true;
						} else return false;
					} else return false;
				} else if (thresholdTag.contains("CLUSTER")){
					if(thresholdTag.contains("(") && thresholdTag.contains(")")){
						partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
						return true;
					}
				} else return false;
			} else return false;
		}
		return false;
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

	public static String getParameterDetails(String string) {
		try {
			switch(DecisionFunctionType.valueOf(string)){
			case CLUSTER:
				return "Threshold can depend on VAR and STD of the nearest cluster.\n Try with 'VAR' or 'STD' to check if score exceeds such quantities.\n Allowed combinations are also 'nVAR' or 'nSTD' where n is a real positive number.\n Default n = 1";
			case CONFIDENCE_INTERVAL:
				return "Checks if result exceeds the confidence interval avg +/- std.\n Combinations as CONFIDENCE_INTERVAL(n), where n is a real positive number, are accepted.\n Default n = 1.";
			case DOUBLE_THRESHOLD:
				return "Checks if result is inside two thresholds m and n,\n that should be specified as DOUBLE_THRESHOLD(m,n).";
			case IQR:
				return "Checks if result is in the Interquartile Range IQR,\n defined as Q1 - 1.5IQR < result < Q3 + 1.5 IQR,\n where IQR = Q3 - Q1.\n Combinations as IQR(n), where n is a real positive number, are accepted.\n Default n = 1.5.";
			case LEFT_IQR:
				return "Checks if result is before the left threshold of the Interquartile Range IQR,\n defined as Q1 - 1.5IQR < result,\n where IQR = Q3 - Q1.\n Combinations as LEFT_IQR(n), where n is a real positive number, are accepted.\n Default n = 1.5.";
			case RIGHT_IQR:
				return "Checks if result is over the right threshold in the Interquartile Range IQR,\n defined as result < Q3 + 1.5 IQR,\n where IQR = Q3 - Q1.\n Combinations as RIGHT_IQR(n), where n is a real positive number, are accepted.\n Default n = 1.5.";
			case LOG_THRESHOLD:
				return "Checks if result is over the averaged sum of heights of histograms,\n for statistical histogram-based algorithms.";
			case STATIC_THRESHOLD:
				return "Checks if result is greather than n,\n a real number that should be specified as STATIC_THRESHOLD(n).";
			case THRESHOLD:
				return "Checks if result is greather than n,\n a real number that should be specified as THRESHOLD(n).";
			default:
				break;
			}
			return null;
		} catch(Exception ex){
			return "";
		}
	}

	public abstract String toCompactString();

}
