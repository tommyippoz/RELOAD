/**
 * 
 */
package ippoz.reload.featureselection;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.InfoGainAttributeEval;

/**
 * The Class InformationGainSelector. Taken from the WEKA framework.
 *
 * @author Tommy
 */
public class InformationGainSelector extends WEKAFeatureRanker {

	/**
	 * Instantiates a new information gain selector.
	 *
	 * @param selectorThreshold the selector threshold
	 * @param isRankThreshold 
	 */
	public InformationGainSelector(double selectorThreshold, boolean isRankThreshold) {
		super(FeatureSelectorType.INFORMATION_GAIN, selectorThreshold, isRankThreshold, true, false);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.WEKAFeatureSelector#instantiateWEKASelector()
	 */
	@Override
	protected ASEvaluation instantiateWEKASelector() {
		return new InfoGainAttributeEval();
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#getSelectorName()
	 */
	@Override
	public String getSelectorName() {
		return "InformationGain";
	}
	
}
