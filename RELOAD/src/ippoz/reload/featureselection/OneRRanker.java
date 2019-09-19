/**
 * 
 */
package ippoz.reload.featureselection;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ClassifierAttributeEval;
import weka.attributeSelection.OneRAttributeEval;
import weka.attributeSelection.WrapperSubsetEval;

/**
 * @author Tommy
 *
 */
public class OneRRanker extends WEKAFeatureRanker {

	public OneRRanker(double selectorThreshold, boolean isRankThreshold) {
		super(FeatureSelectorType.ONER, selectorThreshold, isRankThreshold, true, false);
	}

	@Override
	protected ASEvaluation instantiateWEKASelector() {
		return new OneRAttributeEval();
	}

	@Override
	public String getSelectorName() {
		return "OneRRanker";
	}

}
