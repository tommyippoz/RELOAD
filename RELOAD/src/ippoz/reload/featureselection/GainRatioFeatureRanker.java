/**
 * 
 */
package ippoz.reload.featureselection;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.GainRatioAttributeEval;

/**
 * @author Tommy
 *
 */
public class GainRatioFeatureRanker extends WEKAFeatureRanker {

	/**
	 * Instantiates a new information gain selector.
	 *
	 * @param selectorThreshold the selector threshold
	 * @param isRankThreshold 
	 */
	public GainRatioFeatureRanker(double selectorThreshold, boolean isRankThreshold) {
		super(FeatureSelectorType.GAIN_RATIO, selectorThreshold, isRankThreshold, true, false);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.WEKAFeatureSelector#instantiateWEKASelector()
	 */
	@Override
	protected ASEvaluation instantiateWEKASelector() {
		return new GainRatioAttributeEval();
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#getSelectorName()
	 */
	@Override
	public String getSelectorName() {
		return "InformationGain";
	}
	

}
