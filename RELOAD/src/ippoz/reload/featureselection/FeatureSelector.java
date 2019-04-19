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
 * @author Tommy
 *
 */
public abstract class FeatureSelector {
	
	private FeatureSelectorType fsType;
	
	private double selectorThreshold;
	
	private Map<DataSeries, Double> scoresMap;
	
	public FeatureSelector(FeatureSelectorType fsType, double selectorThreshold){
		this.fsType = fsType;
		this.selectorThreshold = selectorThreshold;
		scoresMap = null;
	}
	
	public FeatureSelectorType getFeatureSelectorType(){
		return fsType;
	}
	
	public Double getSelectorThreshold(){
		return selectorThreshold;
	}
	
	public void applyFeatureSelection(List<DataSeries> seriesList, List<Knowledge> kList){
		scoresMap = executeSelector(seriesList, kList);
	}
	
	protected abstract Map<DataSeries, Double> executeSelector(List<DataSeries> seriesList, List<Knowledge> kList);

	public Map<DataSeries, Double> getScoresMap(){
		return scoresMap;
	}
	
	public Double getScoreFor(DataSeries ds){
		if(scoresMap != null && scoresMap.size() > 0){
			return scoresMap.get(ds);
		} else return null;
	}
	
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
	
	protected List<Snapshot> toSnapList(List<Knowledge> kList, DataSeries ds){
		List<Snapshot> kSnapList = null;
		for(Knowledge knowledge : kList){
			if(kSnapList == null)
				kSnapList = toSnapList(knowledge, ds);
			else kSnapList.addAll( toSnapList(knowledge, ds));
		}
		return kSnapList;
	}
	
	protected List<Double> getSnapValues(List<Snapshot> snapList){
		List<Double> values = new ArrayList<Double>(snapList.size());
		for(Snapshot snap : snapList){
			values.add(((DataSeriesSnapshot)snap).getSnapValue().getFirst());
		}
		return values;
	}
	
	protected List<Double> getSnapLabels(List<Snapshot> snapList){
		List<Double> labels = new ArrayList<Double>(snapList.size());
		for(Snapshot snap : snapList){
			labels.add(snap.getInjectedElement() != null ? 1.0 : 0.0);
		}
		return labels;
	}
	
	protected List<Snapshot> toSnapList(Knowledge know, DataSeries ds) {
		return know.toArray(ds);
	}

	protected abstract boolean checkSelection(DataSeries ds, Double toCheck, double threshold);
	
	public abstract String getSelectorName();

	public static String explainSelectors() {
		return FeatureSelectorType.VARIANCE + ": used to cut out features that are too static. "
				+ "<br>   Threshold represents the ratio that is used to check if the variance is big enough (var > threshold*avg) <br>" +
				FeatureSelectorType.PEARSON_CORRELATION + ": used to cut out features that are not correlated to the label. "
				+ "<br>   Threshold represents the absolute value (0 < threshold <= 1) that is used to check if the correlation is strong enough <br>" +
				FeatureSelectorType.INFORMATION_GAIN + ": used to cut out features that embed too much entropy. "
				+ "<br>   Threshold represents the absolute value (0 < threshold <= 1) that is used to check if the information gain is big enough <br>";
	}

	public static FeatureSelector createSelector(FeatureSelectorType fst, double threshold) {
		if(fst == null)
			return null;
		else {
			switch(fst){
				case INFORMATION_GAIN:
					return new InformationGainSelector(threshold);
				case PEARSON_CORRELATION:
					return new PearsonFeatureSelector(threshold);
				case VARIANCE:
					return new VarianceFeatureSelector(threshold);
				default:
					return null;
			}
		}
	}

	public void updateSelectorThreshold(double threshold) {
		selectorThreshold = threshold;
	}

}
