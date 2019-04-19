/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.InfoGainAttributeEval;

/**
 * @author Tommy
 *
 */
public class InformationGainSelector extends WEKAFeatureSelector {

	public InformationGainSelector(double selectorThreshold) {
		super(FeatureSelectorType.INFORMATION_GAIN, selectorThreshold);
	}

	@Override
	protected ASEvaluation instantiateWEKASelector() {
		return new InfoGainAttributeEval();
	}

	@Override
	protected boolean checkSelection(DataSeries ds, Double toCheck, double threshold) {
		if(!Double.isFinite(toCheck))
			return true;
		else return toCheck > threshold;
	}

	@Override
	public String getSelectorName() {
		return "InformationGain";
	}
	
}
