/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppUtility;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * The Class PearsonFeatureSelector. A data series is selected if the absolute value of
 * Pearson correlation between a data series and the true labels exceeds a given threshold.
 *
 * @author Tommy
 */
public class PearsonFeatureSelector extends FeatureSelector {

	/**
	 * Instantiates a new pearson feature selector.
	 *
	 * @param selectorThreshold the selector threshold
	 */
	public PearsonFeatureSelector(double selectorThreshold) {
		super(FeatureSelectorType.PEARSON_CORRELATION, selectorThreshold);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#executeSelector(java.util.List, java.util.List)
	 */
	@Override
	protected Map<DataSeries, Double> executeSelector(List<DataSeries> seriesList, List<Knowledge> kList) {
		double corr = 0;
		List<Double> values = new LinkedList<Double>();
		List<Double> labels = new LinkedList<Double>();
		List<Snapshot> snapList = null;
		Map<DataSeries, Double> outMap = new HashMap<DataSeries, Double>();
		for(DataSeries ds : seriesList){
			if(ds.size() == 1){
				corr = 0;
				for(Knowledge know : kList){
					snapList = toSnapList(know, ds);
					values.addAll(getSnapValues(snapList));
					labels.addAll(getSnapLabels(snapList));
				}
				for(int i=0;i<values.size();i++){
					if(!Double.isFinite(values.get(i))){
						values.remove(i);
						values.add(i, 0.0);
					} 
					if(!Double.isFinite(labels.get(i))){
						labels.remove(i);
						labels.add(i, 0.0);
					} 
				}
				corr = new PearsonsCorrelation().correlation(
						AppUtility.toPrimitiveArray(values), 
						AppUtility.toPrimitiveArray(labels));
				outMap.put(ds, corr);
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
		else return Math.abs(toCheck) > threshold;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#getSelectorName()
	 */
	@Override
	public String getSelectorName() {
		return "PearsonSelector";
	}

}
