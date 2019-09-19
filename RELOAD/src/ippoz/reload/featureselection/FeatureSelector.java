/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Class FeatureSelector. It allows to choose which features of a knowledge are worth analysing.
 * It allows reducing the amount of data to be used, avoiding pollution of the results.
 *
 * @author Tommy
 */
public abstract class FeatureSelector {
	
	/** The feature selector type. */
	private FeatureSelectorType fsType;
	
	/** The selector threshold. */
	private double selectorThreshold;
	
	/** The rank threshold. */
	private boolean rankThresholdFlag;
	
	/** The scores map. */
	private Map<DataSeries, Double> scoresMap;
	
	/**
	 * Instantiates a new feature selector.
	 *
	 * @param fsType the fs type
	 * @param selectorThreshold the selector threshold
	 */
	public FeatureSelector(FeatureSelectorType fsType, double selectorThreshold, boolean rankThresholdFlag){
		this.fsType = fsType;
		this.selectorThreshold = selectorThreshold;
		this.rankThresholdFlag = rankThresholdFlag;
		scoresMap = null;
	}
	
	/**
	 * Gets the feature selector type.
	 *
	 * @return the feature selector type
	 */
	public FeatureSelectorType getFeatureSelectorType(){
		return fsType;
	}
	
	/**
	 * Gets the selector threshold.
	 *
	 * @return the selector threshold
	 */
	public Double getSelectorThreshold(){
		return selectorThreshold;
	}
	
	/**
	 * Gets the selector threshold.
	 *
	 * @return the selector threshold
	 */
	public boolean isRankedThreshold(){
		return rankThresholdFlag;
	}
	
	/**
	 * Applies feature selection, storing results in the scoresMap. Selector is an abstract function.
	 *
	 * @param seriesList the series list
	 * @param kList the knowledge list
	 */
	public void applyFeatureSelection(List<DataSeries> seriesList, List<Knowledge> kList){
		scoresMap = executeSelector(seriesList, kList);
	}
	
	/**
	 * Executes feature selection strategy. Needs overriding.
	 *
	 * @param seriesList the series list
	 * @param kList the k list
	 * @return the map
	 */
	protected abstract Map<DataSeries, Double> executeSelector(List<DataSeries> seriesList, List<Knowledge> kList);

	/**
	 * Gets the scores map.
	 *
	 * @return the scores map
	 */
	public Map<DataSeries, Double> getScoresMap(){
		return scoresMap;
	}
	
	/**
	 * Gets the score for a given data series.
	 *
	 * @param ds the dataseries
	 * @return the score for the dataseries
	 */
	public Double getScoreFor(DataSeries ds){
		if(scoresMap != null && scoresMap.size() > 0){
			return scoresMap.get(ds);
		} else return null;
	}
	
	/**
	 * Gets the scores string for a list of dataseries.
	 *
	 * @param seriesList the series list
	 * @return the scores string for dataseries'
	 */
	public String getScoresStringFor(List<DataSeries> seriesList){
		String outString = "";
		if(seriesList != null && scoresMap != null){
			for(DataSeries ds : seriesList){
				outString = outString + getScoreFor(ds) + ","; 
			}
			if(outString.trim().length() > 0)
				outString = outString.substring(0, outString.length()-1);
		}
		return outString;
	}
	
	/**
	 * Gets the selected series, or rather series that are selected by the feature selector.
	 *
	 * @return the selected series
	 */
	public List<DataSeries> getSelectedSeries(){
		List<DataSeries> outList = new LinkedList<DataSeries>();
		if(scoresMap != null){
			for(DataSeries ds : scoresMap.keySet()){
				if(checkSelection(ds, scoresMap.get(ds), selectorThreshold))
					outList.add(ds);
			}
		} 
		return outList;
	}
	
	/**
	 * Converts knowledge to list of snapshots.
	 *
	 * @param kList the k list
	 * @param ds the ds
	 * @return the list
	 */
	protected List<Snapshot> toSnapList(List<Knowledge> kList, DataSeries ds){
		List<Snapshot> kSnapList = null;
		for(Knowledge knowledge : kList){
			if(kSnapList == null)
				kSnapList = toSnapList(knowledge, ds);
			else kSnapList.addAll( toSnapList(knowledge, ds));
		}
		return kSnapList;
	}
	
	/**
	 * Gets the snap values.
	 *
	 * @param snapList the snap list
	 * @return the snap values
	 */
	protected List<Double> getSnapValues(List<Snapshot> snapList){
		List<Double> values = new ArrayList<Double>(snapList.size());
		for(Snapshot snap : snapList){
			values.add(((DataSeriesSnapshot)snap).getSnapValue().getFirst());
		}
		return values;
	}
	
	/**
	 * Gets the snap labels.
	 *
	 * @param snapList the snap list
	 * @return the snap labels
	 */
	protected List<Double> getSnapLabels(List<Snapshot> snapList){
		List<Double> labels = new ArrayList<Double>(snapList.size());
		for(Snapshot snap : snapList){
			labels.add(snap.getInjectedElement() != null ? 1.0 : 0.0);
		}
		return labels;
	}
	
	/**
	 * To snap list.
	 *
	 * @param know the know
	 * @param ds the ds
	 * @return the list
	 */
	protected List<Snapshot> toSnapList(Knowledge know, DataSeries ds) {
		return know.toArray(ds);
	}

	/**
	 * Check selection.
	 *
	 * @param ds the ds
	 * @param toCheck the to check
	 * @param threshold the threshold
	 * @return true, if successful
	 */
	protected abstract boolean checkSelection(DataSeries ds, Double toCheck, double threshold);
	
	/**
	 * Gets the selector name.
	 *
	 * @return the selector name
	 */
	public abstract String getSelectorName();

	/**
	 * Explain selectors.
	 *
	 * @return the string
	 */
	public static String explainSelectors() {
		return FeatureSelectorType.VARIANCE + ": used to cut out features that are too static. "
				+ "<br>   Threshold represents the ratio that is used to check if the variance is big enough (var > threshold*avg) <br>" +
				FeatureSelectorType.PEARSON_CORRELATION + ": used to cut out features that are not correlated to the label. "
				+ "<br>   Threshold represents the absolute value (0 < threshold <= 1) that is used to check if the correlation is strong enough <br>" +
				FeatureSelectorType.RELIEF + ": used to cut out features that are ranked low by ReliefF Algorithm. "
				+ "<br>   Threshold represents the absolute value (0 < threshold <= 1) that is used to check if the ReliefF score is high enough <br>" +
				FeatureSelectorType.INFORMATION_GAIN + ": used to cut out features that embed too much entropy. "
				+ "<br>   Threshold represents the absolute value (0 < threshold <= 1) that is used to check if the information gain is big enough <br>";
	}

	/**
	 * Creates the selector.
	 *
	 * @param fst the fst
	 * @param threshold the threshold
	 * @return the feature selector
	 */
	public static FeatureSelector createSelector(FeatureSelectorType fst, double threshold, boolean isRankThreshold) {
		if(fst == null)
			return null;
		else {
			switch(fst){
				case RELIEF: 
					return new ReliefFeatureSelector(threshold, isRankThreshold);
				case INFORMATION_GAIN:
					return new InformationGainSelector(threshold, isRankThreshold);
				case PEARSON_CORRELATION:
					return new PearsonFeatureSelector(threshold, isRankThreshold);
				case VARIANCE:
					return new VarianceFeatureSelector(threshold, isRankThreshold);	
				case ONER:
					return new OneRRanker(threshold, isRankThreshold);
				case PCA:
					return new PrincipalComponentRanker(threshold, isRankThreshold);
				default:
					return null;
			} 
		}
	}

	/**
	 * Update selector threshold.
	 *
	 * @param threshold the threshold
	 */
	public void updateSelectorThreshold(double threshold) {
		selectorThreshold = threshold;
	}

	public void updateRankedThreshold(boolean b) {
		rankThresholdFlag = b;
	}

}
