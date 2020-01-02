/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.featureselection.support.J48AttributeEval;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ClassifierAttributeEval;
import weka.classifiers.trees.RandomForest;

/**
 * @author Tommy
 *
 */
public class J48Ranker extends WEKAFeatureRanker {

	public J48Ranker(double selectorThreshold, boolean isRankThreshold) {
		super(FeatureSelectorType.J48, selectorThreshold, isRankThreshold, true, false);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ASEvaluation instantiateWEKASelector() {
		return new J48AttributeEval();
	}

	@Override
	public String getSelectorName() {
		return "J48";
	}

}
