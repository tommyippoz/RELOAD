/**
 * 
 */
package ippoz.madness.detector.decisionfunction;

import ippoz.madness.detector.commons.support.AppUtility;

import java.util.List;

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
	
	public static DecisionFunction getClassifier(List<Double> scores, String tag){
		if(tag != null && tag.length() > 0){
			if(AppUtility.isNumber(tag)){
				double ratio = Double.parseDouble(tag);
				return new ThresholdDecision(ratio, scores);
			}
			else return null;
		} else return null;
	}

	public String getClassifierName() {
		return classifierName;
	}

	public DecisionFunctionType getClassifierType() {
		return classifierType;
	}

	public abstract AnomalyResult classify(double doubleValue);

}
