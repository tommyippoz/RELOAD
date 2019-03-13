/**
 * 
 */
package ippoz.madness.detector.scoreclassifier;

import java.util.List;

import ippoz.madness.detector.commons.support.AppUtility;

/**
 * @author Tommy
 *
 */
public abstract class ScoreClassifier {
	
	private String classifierName;
	
	private ClassifierType classifierType;
	
	protected ScoreClassifier(String classifierName, ClassifierType classifierType) {
		this.classifierName = classifierName;
		this.classifierType = classifierType;
	}
	
	public static ScoreClassifier getClassifier(List<Double> scores, String tag){
		if(tag != null && tag.length() > 0){
			if(AppUtility.isNumber(tag)){
				double ratio = Double.parseDouble(tag);
				return new ThresholdClassifier(ratio, scores);
			}
			else return null;
		} else return null;
	}

	public String getClassifierName() {
		return classifierName;
	}

	public ClassifierType getClassifierType() {
		return classifierType;
	}

	public abstract AnomalyResult classify(double doubleValue);

}
