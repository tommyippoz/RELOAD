/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * The Class PearsonFeatureSelector. A data series is selected if the absolute value of
 * Pearson correlation between a data series and the true labels exceeds a given threshold.
 *
 * @author Tommy
 */ 
public class PearsonFeatureSelector extends FeatureRanker {

	/**
	 * Instantiates a new pearson feature selector.
	 *
	 * @param selectorThreshold the selector threshold
	 * @param isRankThreshold 
	 */
	public PearsonFeatureSelector(double selectorThreshold, boolean isRankThreshold) {
		super(FeatureSelectorType.PEARSON_CORRELATION, selectorThreshold, isRankThreshold, true, true);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#executeSelector(java.util.List, java.util.List)
	 */
	@Override
	protected Map<DataSeries, Double> executeSelector(List<DataSeries> seriesList, List<Knowledge> kList) {
		Map<DataSeries, Double> outMap = new HashMap<DataSeries, Double>();
		for(DataSeries ds : seriesList){
			if(ds.size() == 1){
				List<Double> tempValues = new ArrayList<>();
				List<Double> tempLabels = new ArrayList<>();
				for(Knowledge know : kList){
					List<Snapshot> snapList = toSnapList(know, ds);
					List<Double> values = getSnapValues(snapList);
					List<Double> labels = getSnapLabels(snapList);
					for(int i=0;i<values.size();i++){
						if(Double.isFinite(values.get(i)))
							tempValues.add(values.get(i));
						else tempValues.add(0.0);
						if(Double.isFinite(labels.get(i)))
							tempLabels.add(labels.get(i));
						else tempLabels.add(0.0);
					}
				}
				double corr = new PearsonsCorrelation().correlation(
						AppUtility.toPrimitiveArray(tempValues), 
						AppUtility.toPrimitiveArray(tempLabels));
				if(!Double.isFinite(corr))
					corr = 0.0;
				outMap.put(ds, Math.abs(corr));
				AppLogger.logInfo(getClass(), "Feature '" + ds.getName() + "' Score: " + outMap.get(ds));
			}
		}
		return outMap;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.featureselection.FeatureSelector#getSelectorName()
	 */
	@Override
	public String getSelectorName() {
		return "PearsonSelector";
	}

}
