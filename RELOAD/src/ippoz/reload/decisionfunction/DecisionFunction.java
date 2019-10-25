/**
 * 
 */
package ippoz.reload.decisionfunction;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;


/**
 * The Class DecisionFunction. Abstract class for decision functions.
 *
 * @author Tommy
 */
public abstract class DecisionFunction {
	
	/** The classifier name. */
	private String decisionFunctionName;
	
	/** The classifier type. */
	private DecisionFunctionType functionType;
	
	/** Specifies if anomalies are extern or intern */
	private boolean revertFlag;
	
	/**
	 * Instantiates a new decision function.
	 *
	 * @param classifierName the classifier name
	 * @param classifierType the classifier type
	 */
	protected DecisionFunction(String classifierName, DecisionFunctionType classifierType, boolean revertFlag) {
		this.decisionFunctionName = classifierName;
		this.functionType = classifierType;
		this.revertFlag = revertFlag;
	}
	
	protected boolean getRevertFlag(){
		return revertFlag;
	}
	
	/**
	 * Builds a decision function starting from a tag and a list of scores obtained during training.
	 *
	 * @param algorithmScores the scores obtained during training
	 * @param thresholdTag the threshold tag
	 * @return the decision function
	 */
	public static DecisionFunction buildDecisionFunction(ValueSeries algorithmScores, String thresholdTag, boolean flag){
		String partial;
		if(thresholdTag != null && thresholdTag.length() > 0){
			thresholdTag = thresholdTag.trim();
			if(thresholdTag.length() > 0) {
				if(AppUtility.isNumber(thresholdTag)){
					double ratio = Double.parseDouble(thresholdTag);
					return new ThresholdDecision(ratio, algorithmScores);
				} else if(thresholdTag.contains("DOUBLE_THRESHOLD") && thresholdTag.contains("(") && thresholdTag.contains(")")){
					partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
					if(partial.contains(",")){
						String lower = partial.split(",")[0].trim();
						String upper = partial.split(",")[1].trim();
						if(AppUtility.isNumber(lower) && AppUtility.isNumber(upper)){
							if(thresholdTag.contains("EXTERN"))
								return new DoubleThresholdExtern(Double.valueOf(lower), Double.valueOf(upper));
							else return new DoubleThresholdIntern(Double.valueOf(lower), Double.valueOf(upper));
						}
					}
				} else if(thresholdTag.contains("THRESHOLD") && thresholdTag.contains("(") && thresholdTag.contains(")")){
					partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
					if(thresholdTag.contains("STATIC")){
						if(thresholdTag.contains("GREATER"))
							return new StaticThresholdGreaterThanDecision(Double.valueOf(partial.trim()));
						else return new StaticThresholdLowerThanDecision(Double.valueOf(partial.trim()));
					} else return new ThresholdDecision(Double.valueOf(partial.trim()), algorithmScores);
				} else if(thresholdTag.endsWith("%")) {
					partial = thresholdTag.replace("%", "");
					return new ThresholdDecision(Double.valueOf(partial.trim())/100.0, algorithmScores);
				} else if(thresholdTag.contains("IQR")) {
					if(algorithmScores != null && algorithmScores.size() > 0){
						if(thresholdTag.equals("IQR"))
							return new IQRFunction(1.5, algorithmScores.getQ1(), algorithmScores.getQ3(), flag);
						else if(thresholdTag.equals("LEFT_IQR"))
							return new LeftIQRFunction(1.5, algorithmScores.getQ1(), algorithmScores.getQ3(), flag);
						else if(thresholdTag.equals("LEFT_POSITIVE_IQR"))
							return new LeftPositiveIQRFunction(1.5, algorithmScores.getQ1(), algorithmScores.getQ3(), flag);
						else if(thresholdTag.equals("RIGHT_IQR"))
							return new RightIQRFunction(1.5, algorithmScores.getQ1(), algorithmScores.getQ3(), flag);
						else if(thresholdTag.contains("(") && thresholdTag.contains(")")){
							partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
							if(partial != null && partial.length() > 0 && AppUtility.isNumber(partial)){
								if(thresholdTag.contains("LEFT_IQR"))
									return new LeftIQRFunction(Double.parseDouble(partial), algorithmScores.getQ1(), algorithmScores.getQ3(), flag);
								else if(thresholdTag.contains("LEFT_POSITIVE_IQR"))
									return new LeftPositiveIQRFunction(Double.parseDouble(partial), algorithmScores.getQ1(), algorithmScores.getQ3(), flag);
								else if(thresholdTag.contains("RIGHT_IQR"))
									return new RightIQRFunction(Double.parseDouble(partial), algorithmScores.getQ1(), algorithmScores.getQ3(), flag);
								else return new IQRFunction(Double.parseDouble(partial), algorithmScores.getQ1(), algorithmScores.getQ3(), flag);
							} else AppLogger.logInfo(DecisionFunction.class, "Parameters of IQR '" + thresholdTag + "' cannot be parsed");
						} else AppLogger.logInfo(DecisionFunction.class, "Parameters of IQR '" + thresholdTag + "' cannot be parsed");
					} else 
						AppLogger.logError(DecisionFunction.class, "DecisionFunctionCreation", "Unable to create IQR decision function '" + thresholdTag + "'");
				} else if(thresholdTag.contains("CONFIDENCE_INTERVAL")) {
					if(algorithmScores != null && algorithmScores.size() > 0){
						if(thresholdTag.equals("CONFIDENCE_INTERVAL"))
							return new ConfidenceIntervalFunction(1.0, algorithmScores.getAvg(), algorithmScores.getStd(), flag);
						else if(thresholdTag.equals("LEFT_CONFIDENCE_INTERVAL"))
							return new LeftConfidenceIntervalFunction(1.0, algorithmScores.getAvg(), algorithmScores.getStd(), flag);
						else if(thresholdTag.equals("LEFT_POSITIVE_CONFIDENCE_INTERVAL"))
							return new LeftPositiveConfidenceIntervalFunction(1.0, algorithmScores.getAvg(), algorithmScores.getStd(), flag);
						else if(thresholdTag.equals("RIGHT_CONFIDENCE_INTERVAL"))
							return new RightConfidenceIntervalFunction(1.0, algorithmScores.getAvg(), algorithmScores.getStd(), flag);
						else if(thresholdTag.contains("(") && thresholdTag.contains(")")){
							partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
							if(partial != null && partial.length() > 0 && AppUtility.isNumber(partial)){
								if(thresholdTag.contains("LEFT_CONFIDENCE_INTERVAL"))
									return new LeftConfidenceIntervalFunction(Double.parseDouble(partial), algorithmScores.getAvg(), algorithmScores.getStd(), flag);
								else if(thresholdTag.contains("LEFT_POSITIVE_CONFIDENCE_INTERVAL"))
									return new LeftPositiveConfidenceIntervalFunction(Double.parseDouble(partial), algorithmScores.getAvg(), algorithmScores.getStd(), flag);
								else if(thresholdTag.contains("RIGHT_CONFIDENCE_INTERVAL"))
									return new RightConfidenceIntervalFunction(Double.parseDouble(partial), algorithmScores.getAvg(), algorithmScores.getStd(), flag);
								else return new ConfidenceIntervalFunction(Double.parseDouble(partial), algorithmScores.getAvg(), algorithmScores.getStd(), flag);
							} else AppLogger.logInfo(DecisionFunction.class, "Parameters of CONF '" + thresholdTag + "' cannot be parsed");
						} else AppLogger.logInfo(DecisionFunction.class, "Parameters of CONF '" + thresholdTag + "' cannot be parsed");
					} else AppLogger.logError(DecisionFunction.class, "DecisionFunctionCreation", "Unable to create CONF decision function '" + thresholdTag + "'");
				} else if (thresholdTag.contains("CLUSTER")){
					if(thresholdTag.contains("(") && thresholdTag.contains(")")){
						partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
						return new ClusterDecision(partial, flag);
					} AppLogger.logInfo(DecisionFunction.class, "Parameters of cluster '" + thresholdTag + "' cannot be parsed");
				} else AppLogger.logError(DecisionFunction.class, "DecisionFunctionCreation", "Unable to create decision function '" + thresholdTag + "'");
			} else AppLogger.logError(DecisionFunction.class, "DecisionFunctionCreation", "null tag for decision function");
		}
		return null;
	}
	
	/**
	 * Checks if it is possible to build  decision function starting from a tag.
	 *
	 * @param thresholdTag the decision function tag
	 * @return true, if decision function can be created
	 */
	public static boolean checkDecisionFunction(String thresholdTag){
		String partial;
		if(thresholdTag != null && thresholdTag.length() > 0){
			thresholdTag = thresholdTag.trim();
			if(thresholdTag.length() > 0) {
				if(AppUtility.isNumber(thresholdTag)){
					return true;
				} else if(thresholdTag.endsWith("%")) {
					return true;
				} else if(thresholdTag.contains("DOUBLE_THRESHOLD") && thresholdTag.contains("(") && thresholdTag.contains(")")){
					partial = thresholdTag.substring(thresholdTag.indexOf("(")+1, thresholdTag.indexOf(")"));
					if(partial.contains(",")){
						String lower = partial.split(",")[0].trim();
						String upper = partial.split(",")[1].trim();
						if(AppUtility.isNumber(lower) && AppUtility.isNumber(upper)){
							return true;
						} else return false;
					}
				}else if(thresholdTag.contains("IQR")) {
					if(thresholdTag.equals("IQR"))
						return true;
					else if(thresholdTag.equals("LEFT_IQR"))
						return true;
					else if(thresholdTag.equals("LEFT_POSITIVE_IQR"))
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
					else if(thresholdTag.equals("LEFT_POSITIVE_CONFIDENCE_INTERVAL"))
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

	/**
	 * Gets the name of the decision function.
	 *
	 * @return the name
	 */
	public String getName() {
		return decisionFunctionName;
	}
	
	/**
	 * Gets the classifier tag.
	 *
	 * @return the classifier tag
	 */
	public abstract String getClassifierTag();

	/**
	 * Gets the decision function type.
	 *
	 * @return the function type
	 */
	public DecisionFunctionType getDecisionFunctionType() {
		return functionType;
	}
	
	/**
	 * Assigns a score depending on the (this) decision function.
	 *
	 * @param aResult the anomaly result
	 * @param updateResult if true, overrides aResult with this assignment
	 * @return the anomaly result
	 */
	public AnomalyResult assignScore(AlgorithmResult aResult, boolean updateResult){
		AnomalyResult anRes = null;
		if(aResult != null){
			aResult.setDecisionFunction(this);
			anRes = classify(aResult);
			if(updateResult)
				aResult.setScoreEvaluation(anRes);
		}
		return anRes;
	}

	/**
	 * Classify result according to the decision function.
	 *
	 * @param aResult the algorithm result
	 * @return the anomaly result
	 */
	public abstract AnomalyResult classify(AlgorithmResult aResult);

	/**
	 * Gets the parameter details. Used by GUIs.
	 *
	 * @param string the string
	 * @return the parameter details
	 */
	public static String getParameterDetails(String string) {
		try {
			switch(DecisionFunctionType.valueOf(string)){
				case CLUSTER:
					return "Threshold can depend on VAR and STD of the nearest cluster.\n Try with 'VAR' or 'STD' to check if score exceeds such quantities.\n Allowed combinations are also 'nVAR' or 'nSTD' where n is a real positive number.\n Default n = 1";
				case CONFIDENCE_INTERVAL:
					return "Checks if result exceeds the confidence interval avg +/- n*std.\n Combinations as CONFIDENCE_INTERVAL(n), where n is a real positive number, are accepted.\n Default n = 1.";
				case LEFT_CONFIDENCE_INTERVAL:
					return "Checks if result exceeds the confidence interval avg - n*std.\n Combinations as LEFT_CONFIDENCE_INTERVAL(n), where n is a real positive number, are accepted.\n Default n = 1.";
				case LEFT_POSITIVE_CONFIDENCE_INTERVAL:
					return "Checks if result exceeds the confidence interval avg - n*std.\n If avg - n*std < 0, value will be defaulted to 0.\nCombinations as LEFT_CONFIDENCE_INTERVAL(n), where n is a real positive number, are accepted.\n Default n = 1.";
				case RIGHT_CONFIDENCE_INTERVAL:
					return "Checks if result exceeds the confidence interval avg + n*std.\n Combinations as RIGHT_CONFIDENCE_INTERVAL(n), where n is a real positive number, are accepted.\n Default n = 1.";
				case DOUBLE_THRESHOLD_EXTERN:
					return "Checks if result is outside two thresholds m and n,\n that should be specified as DOUBLE_THRESHOLD_EXTERN(m,n).";
				case DOUBLE_THRESHOLD_INTERN:
					return "Checks if result is inside two thresholds m and n,\n that should be specified as DOUBLE_THRESHOLD_INTERN(m,n).";
				case IQR:
					return "Checks if result is in the Interquartile Range IQR,\n defined as Q1 - 1.5IQR < result < Q3 + 1.5 IQR, where IQR = Q3 - Q1.\n Combinations as IQR(n), where n is a real positive number, are accepted.\n Default n = 1.5.";
				case LEFT_IQR:
					return "Checks if result is before the left threshold of the Interquartile Range IQR,\n defined as Q1 - 1.5IQR < result, where IQR = Q3 - Q1.\n Combinations as LEFT_IQR(n), where n is a real positive number, are accepted.\n Default n = 1.5.";
				case LEFT_POSITIVE_IQR:
					return "Checks if result is before the left threshold of the Interquartile Range IQR,\n defined as Q1 - 1.5IQR < result, where IQR = Q3 - Q1.\n If Q1 - 1.5IQR < 0, value will be defaulted to 0.\n Combinations as LEFT_IQR(n), where n is a real positive number, are accepted.\n Default n = 1.5.";
				case RIGHT_IQR:
					return "Checks if result is over the right threshold in the Interquartile Range IQR,\n defined as result < Q3 + 1.5 IQR, where IQR = Q3 - Q1.\n Combinations as RIGHT_IQR(n), where n is a real positive number, are accepted.\n Default n = 1.5.";
				case LOG_THRESHOLD:
					return "Checks if result is over the averaged sum of heights of histograms,\n for statistical histogram-based algorithms.";
				case STATIC_THRESHOLD_GREATERTHAN:
					return "Checks if result is greather than n,\n a real number that should be specified as STATIC_THRESHOLD_GREATERTHAN(n).";
				case STATIC_THRESHOLD_LOWERTHAN:
					return "Checks if result is greather than n,\n a real number that should be specified as STATIC_THRESHOLD_LOWERTHAN(n).";
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

	/**
	 * Shows decision function to compact string.
	 *
	 * @return the string
	 */
	public abstract String toCompactString();
	
	public String toCompactStringComplete(){
		return toCompactString() + " revert:" + revertFlag;
	}

	/**
	 * Gets the thresholds defined by this decision function.
	 *
	 * @return the thresholds
	 */
	public abstract double[] getThresholds();

	public static boolean isApplicableTo(AlgorithmType algType, String decFunctString) {
		if(decFunctString == null || !checkDecisionFunction(decFunctString))
			return false;
		else if(decFunctString.contains("CLUSTER") && (algType.equals(AlgorithmType.DBSCAN) || algType.equals(AlgorithmType.ELKI_KMEANS)))
			return true;
		else if(!decFunctString.contains("CLUSTER") && !algType.equals(AlgorithmType.DBSCAN) && !algType.equals(AlgorithmType.ELKI_KMEANS))
			return true;
		else return false;
	}

}
