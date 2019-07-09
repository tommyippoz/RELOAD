/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ReliefFAttributeEval;

/**
 * @author Tommy
 *
 */
public class ReliefFeatureSelector extends WEKAFeatureSelector {

	/**
	 * Instantiates a new information gain selector.
	 *
	 * @param selectorThreshold the selector threshold
	 */
	public ReliefFeatureSelector(double selectorThreshold) {
		super(FeatureSelectorType.RELIEF, selectorThreshold);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.WEKAFeatureSelector#instantiateWEKASelector()
	 */
	@Override
	protected ASEvaluation instantiateWEKASelector() {
		return new ReliefFAttributeEval();
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#checkSelection(ippoz.reload.commons.dataseries.DataSeries, java.lang.Double, double)
	 */
	@Override
	protected boolean checkSelection(DataSeries ds, Double toCheck, double threshold) {
		if(!Double.isFinite(toCheck))
			return true;
		else return toCheck > threshold;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#getSelectorName()
	 */
	@Override
	public String getSelectorName() {
		return "Relief";
	}

}
