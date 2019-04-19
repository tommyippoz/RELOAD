/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.utils.maths.MathUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class VarianceFeatureSelector extends FeatureSelector {
	
	private Map<DataSeries, Double> avgMap;

	public VarianceFeatureSelector(double selectorThreshold) {
		super(FeatureSelectorType.VARIANCE, selectorThreshold);
	}

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

	@Override
	protected boolean checkSelection(DataSeries ds, Double toCheck, double threshold) {
		if(!Double.isFinite(toCheck))
			return true;
		else if(avgMap.containsKey(ds))
			return toCheck > threshold*Math.sqrt(avgMap.get(ds));
		else return toCheck > 0;
	}

	@Override
	public String getSelectorName() {
		return "VarianceSelector";
	}

}
