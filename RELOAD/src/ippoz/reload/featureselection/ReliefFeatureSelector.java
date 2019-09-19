/**
 * 
 */
package ippoz.reload.featureselection;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ReliefFAttributeEval;

/**
 * @author Tommy
 *
 */
public class ReliefFeatureSelector extends WEKAFeatureRanker {

	/**
	 * Instantiates a new information gain selector.
	 *
	 * @param selectorThreshold the selector threshold
	 * @param isRankThreshold 
	 */
	public ReliefFeatureSelector(double selectorThreshold, boolean isRankThreshold) {
		super(FeatureSelectorType.RELIEF, selectorThreshold, isRankThreshold, true, false);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.WEKAFeatureSelector#instantiateWEKASelector()
	 */
	@Override
	protected ASEvaluation instantiateWEKASelector() {
		return new ReliefFAttributeEval();
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#getSelectorName()
	 */
	@Override
	public String getSelectorName() {
		return "Relief";
	}

}
