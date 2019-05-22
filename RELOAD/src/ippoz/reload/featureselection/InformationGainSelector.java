/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.InfoGainAttributeEval;

/**
 * The Class InformationGainSelector. Taken from the WEKA framework.
 *
 * @author Tommy
 */
public class InformationGainSelector extends WEKAFeatureSelector {

	/**
	 * Instantiates a new information gain selector.
	 *
	 * @param selectorThreshold the selector threshold
	 */
	public InformationGainSelector(double selectorThreshold) {
		super(FeatureSelectorType.INFORMATION_GAIN, selectorThreshold);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.WEKAFeatureSelector#instantiateWEKASelector()
	 */
	@Override
	protected ASEvaluation instantiateWEKASelector() {
		return new InfoGainAttributeEval();
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
		return "InformationGain";
	}
	
}
