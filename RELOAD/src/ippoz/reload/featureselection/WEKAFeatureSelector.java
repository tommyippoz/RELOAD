/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.externalutils.WEKAUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeEvaluator;
import weka.core.Instances;

/**
 * The Class WEKAFeatureSelector. Wrapper for possible feature selection strategies 
 * taken by WEKA framework.
 *
 * @author Tommy
 */
public abstract class WEKAFeatureSelector extends FeatureSelector {

	/**
	 * Instantiates a new WEKA feature selector.
	 *
	 * @param fsType the feature selector type
	 * @param selectorThreshold the selector threshold
	 */
	public WEKAFeatureSelector(FeatureSelectorType fsType, double selectorThreshold) {
		super(fsType, selectorThreshold);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#executeSelector(java.util.List, java.util.List)
	 */
	@Override
	protected Map<DataSeries, Double> executeSelector(List<DataSeries> seriesList, List<Knowledge> kList) {
		if(seriesList != null && seriesList.size() > 0){
			return executeWEKASelector(seriesList, WEKAUtils.translateKnowledge(kList, seriesList));
		} else return null;
	}
	
	/**
	 * Execute weka selector.
	 *
	 * @param seriesList the series list
	 * @param data the data
	 * @return the map
	 */
	private Map<DataSeries, Double> executeWEKASelector(List<DataSeries> seriesList, Instances data){
		ASEvaluation attEval;
		Map<DataSeries, Double> scores = new HashMap<DataSeries, Double>();
		try {
			attEval = instantiateWEKASelector();
			if(attEval instanceof AttributeEvaluator){
				attEval.buildEvaluator(data);
				for(int i=0;i<seriesList.size();i++){
					scores.put(seriesList.get(i), ((AttributeEvaluator)attEval).evaluateAttribute(i));
				}
			} else AppLogger.logError(getClass(), "AttributeSelectorError", "Unable to instantiate correct attribute evaluator");
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to Select Attributes through " + getSelectorName());
		}
		return scores;
	}	
	
	/**
	 * Instantiate WEKA selector, and gets the AS (Attribute Selection) evaluation.
	 *
	 * @return the attribute selection evaluation
	 */
	protected abstract ASEvaluation instantiateWEKASelector();

}
