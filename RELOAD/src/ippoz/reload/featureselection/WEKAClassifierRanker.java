/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.externalutils.WEKAUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeEvaluator;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * @author Tommy
 *
 */
public abstract class WEKAClassifierRanker extends FeatureRanker {

	/**
	 * Instantiates a new WEKA feature selector.
	 *
	 * @param fsType the feature selector type
	 * @param selectorThreshold the selector threshold
	 */
	public WEKAClassifierRanker(FeatureSelectorType fsType, double selectorThreshold, boolean isRankThreshold, boolean higherIsBetter, boolean considerAbsolute) {
		super(fsType, selectorThreshold, isRankThreshold, higherIsBetter, considerAbsolute);
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
		Classifier wcf;
		Map<DataSeries, Double> scores = new HashMap<DataSeries, Double>();
		try {
			wcf = instantiateWEKAClassifier();
			wcf.buildClassifier(data);
			double[] classScores = getClassifierScores(wcf, seriesList.size());
			for(int i=0;i<seriesList.size();i++){
				if(classScores != null && i < classScores.length)
					scores.put(seriesList.get(i), classScores[i]);
				else scores.put(seriesList.get(i), 0.0); 
				AppLogger.logInfo(getClass(), "Feature '" + seriesList.get(i).getName() + "' Score: " + scores.get(seriesList.get(i)));
			}
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to Select Attributes through " + getSelectorName());
		}
		return scores;
	}

	protected abstract double[] getClassifierScores(Classifier wcf, int featureNumber);

	protected abstract Classifier instantiateWEKAClassifier();	

}
