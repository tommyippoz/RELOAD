/**
 * 
 */
package ippoz.reload.featureselection;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.PrincipalComponents;

/**
 * @author Tommy
 *
 */
public class PrincipalComponentRanker extends WEKAFeatureRanker {

	public PrincipalComponentRanker(double selectorThreshold, boolean isRankThreshold) {
		super(FeatureSelectorType.PCA, selectorThreshold, isRankThreshold, true, false);
	}

	@Override
	protected ASEvaluation instantiateWEKASelector() {
		return new PrincipalComponents();
	}

	@Override
	public String getSelectorName() {
		return "PCA";
	}

}
