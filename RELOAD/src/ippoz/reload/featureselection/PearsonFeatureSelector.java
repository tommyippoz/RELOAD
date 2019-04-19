/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * @author Tommy
 *
 */
public class PearsonFeatureSelector extends FeatureSelector {

	public PearsonFeatureSelector(double selectorThreshold) {
		super(FeatureSelectorType.PEARSON_CORRELATION, selectorThreshold);
	}

	@Override
	protected Map<DataSeries, Double> executeSelector(List<DataSeries> seriesList, List<Knowledge> kList) {
		double corr = 0;
		double newCorr = 0;
		int count = 0;
		List<Snapshot> snapList = null;
		Map<DataSeries, Double> outMap = new HashMap<DataSeries, Double>();
		for(DataSeries ds : seriesList){
			if(ds.size() == 1){
				corr = 0;
				for(Knowledge know : kList){
					snapList = toSnapList(know, ds);
					newCorr = new PearsonsCorrelation().correlation(
							AppUtility.toPrimitiveArray(getSnapValues(snapList)), 
							AppUtility.toPrimitiveArray(getSnapLabels(snapList)));
					if(Double.isFinite(newCorr)){
						corr = corr + newCorr;
						count++;
					}
				}
				outMap.put(ds, (corr/count));
			}
		}
		return outMap;
	}

	@Override
	protected boolean checkSelection(DataSeries ds, Double toCheck, double threshold) {
		if(!Double.isFinite(toCheck))
			return true;
		else return Math.abs(toCheck) > threshold;
	}

	@Override
	public String getSelectorName() {
		return "PearsonSelector";
	}

}
