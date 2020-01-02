/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.featureselection.support.ChiSquaredAttributeEval;
import weka.attributeSelection.ASEvaluation;

/**
 * @author Tommy
 *
 */
public class ChiSquaredFeatureRanker extends WEKAFeatureRanker{

	public ChiSquaredFeatureRanker(double selectorThreshold, boolean isRankThreshold) {
		super(FeatureSelectorType.CHI_SQUARED, selectorThreshold, isRankThreshold, true, true);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ASEvaluation instantiateWEKASelector() {
		return new ChiSquaredAttributeEval();
	}

	@Override
	public String getSelectorName() {
		return "ChiSquared";
	}

}
