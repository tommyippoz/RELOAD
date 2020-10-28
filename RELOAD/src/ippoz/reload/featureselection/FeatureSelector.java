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
		return "<p style=\"text-align:center\"><b>" + FeatureSelectorType.VARIANCE + "</b>: used to cut out features that are too static. "
				+ "   Threshold represents the ratio that is used to check if the variance is big enough (var &gt threshold*avg) <br>" +
				"<b>" + FeatureSelectorType.PEARSON_CORRELATION + "</b>: used to cut out features that are not pearson-correlated with the label. "
				+ "   Threshold represents the absolute value (0 &lt threshold &lt= 1) that is used to check if the correlation is strong enough <br>" +
				"<b>" + FeatureSelectorType.RELIEF + "</b>: used to cut out features that are ranked low by ReliefF Algorithm. "
				+ "   Threshold represents the absolute value (0 &lt threshold &lt= 1) that is used to check if the ReliefF score is high enough <br>" +
				"<b>" + FeatureSelectorType.GAIN_RATIO + "</b>: used to cut out features that are ranked low by the GainRatio strategy. "
				+ "   Threshold represents the absolute value (0 &lt threshold &lt= 1) that is used to check if the GainRatio score is high enough <br>" +
				"<b>" + FeatureSelectorType.PCA + "</b>: used to cut out features which are not identified as 'Principal Components'. "
				+ "   Threshold represents the absolute value (0 &lt threshold &lt= 1) that is used to check if the PCA score is high enough <br>" +
				"<b>" + FeatureSelectorType.ONER + "</b>: used to cut out features that are ranked low by the OneR Algorithm, adapted as Feature Ranker. "
				+ "   Threshold represents the absolute value (0 &lt threshold &lt= 100) that is used to check if the OneR score is high enough <br>" +
				"<b>" + FeatureSelectorType.J48 + "</b>: used to cut out features that do not take part to create the J48 (C4.5) tree. "
				+ "   Threshold represents the absolute value (0 &lt threshold &lt= 1) that is used to check if the contribution is big enough <br>" +
				"<b>" + FeatureSelectorType.CHI_SQUARED + "</b>: used to cut out features that contribute to reject chi-squared test. "
				+ "   Threshold represents the absolute value that is used to check if the confidence is big enough <br>" +
				"<b>" + FeatureSelectorType.RANDOM_FORESTS + "</b>: used to cut out features that do not take part to decisions in Random Forests. "
				+ "   Threshold represents the absolute value (0 &lt threshold &lt= 1) that is used to check if feature contribution is high enough <br>" +
				"<b>" + FeatureSelectorType.INFORMATION_GAIN + "</b>: used to cut out features that embed too much entropy. "
				+ "   Threshold represents the absolute value (0 &lt threshold &lt= 1) that is used to check if the information gain is big enough <br>" + "</p>";
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
				case GAIN_RATIO:
					return new GainRatioFeatureRanker(threshold, isRankThreshold);
				case CHI_SQUARED:
					return new ChiSquaredFeatureRanker(threshold, isRankThreshold);
				case RANDOM_FORESTS:
					return new RandomForestFeatureRanker(threshold, isRankThreshold);
				case J48:
					return new J48Ranker(threshold, isRankThreshold);
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
	
	public abstract double getHighestScore();

}
