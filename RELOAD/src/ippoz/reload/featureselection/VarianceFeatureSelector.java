/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.utils.MathUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class VarianceFeatureSelector. It selects features that have values with enough variability.
 * More in detail, it calculates variance, that has to be greater than selectorThreshold*avg 
 *
 * @author Tommy
 */
public class VarianceFeatureSelector extends FeatureSelector {
	
	/** The map of averages of data series. */
	private Map<DataSeries, Double> avgMap;

	/**
	 * Instantiates a new variance feature selector.
	 *
	 * @param selectorThreshold the selector threshold
	 * @param isRankThreshold 
	 */
	public VarianceFeatureSelector(double selectorThreshold, boolean isRankThreshold) {
		super(FeatureSelectorType.VARIANCE, selectorThreshold, isRankThreshold);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#executeSelector(java.util.List, java.util.List)
	 */
	@Override
	protected Map<DataSeries, Double> executeSelector(List<DataSeries> seriesList, List<Knowledge> kList) {
		double var = 0, avg = 0, val;
		int countAvg = 0, countVar = 0;
		List<Snapshot> snapList = null;
		Map<DataSeries, Double> outMap = new HashMap<DataSeries, Double>();
		avgMap = new HashMap<DataSeries, Double>();
		for(DataSeries ds : seriesList){
			if(ds.size() == 1){
				var = 0;
				avg = 0;
				countVar = 0;
				countAvg = 0;
				for(Knowledge know : kList){
					snapList = toSnapList(know, ds);
					val = MathUtils.calcVar(getSnapValues(snapList));
					if(Double.isFinite(val)){
						var = var + val;
						countVar++;
					}
					val = MathUtils.calcAvg(getSnapValues(snapList));
					if(Double.isFinite(val)){
						avg = avg + val;
						countAvg++;
					}
				}
				avgMap.put(ds, (avg/countAvg));
				outMap.put(ds, (var/countVar));
			}
		}
		return outMap;
	}	

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#checkSelection(ippoz.reload.commons.dataseries.DataSeries, java.lang.Double, double)
	 */
	@Override
	protected boolean checkSelection(DataSeries ds, Double toCheck, double threshold) {
		if(!Double.isFinite(toCheck))
			return true;
		else if(avgMap.containsKey(ds))
			return toCheck > threshold*Math.sqrt(avgMap.get(ds));
		else return toCheck > 0;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#getSelectorName()
	 */
	@Override
	public String getSelectorName() {
		return "VarianceSelector";
	}

}
