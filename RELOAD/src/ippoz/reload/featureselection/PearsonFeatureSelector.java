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
public class PearsonFeatureSelector extends FeatureRanker {
	
	private static final int BATCH_SIZE = 5000;

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
		List<Double> corrArray;
		List<Double> values = new LinkedList<>();
		List<Double> labels = new LinkedList<>();
		List<Snapshot> snapList = null;
		Map<DataSeries, Double> outMap = new HashMap<DataSeries, Double>();
		for(DataSeries ds : seriesList){
			values.clear();
			labels.clear();
			corrArray = new LinkedList<>();
			if(ds.size() == 1){
				List<Double> tempValues = new LinkedList<>();
				List<Double> tempLabels = new LinkedList<>();
				for(Knowledge know : kList){
					snapList = toSnapList(know, ds);
					values.addAll(getSnapValues(snapList));
					labels.addAll(getSnapLabels(snapList));
				}
				for(int i=0;i<values.size();i++){
					if(Double.isFinite(values.get(i)))
						tempValues.add(values.get(i));
					else tempValues.add(0.0);
					if(Double.isFinite(labels.get(i)))
						tempLabels.add(labels.get(i));
					else tempLabels.add(0.0);
					if(i > 0 && i % BATCH_SIZE == 0){
						double corr = new PearsonsCorrelation().correlation(
								AppUtility.toPrimitiveArray(tempValues), 
								AppUtility.toPrimitiveArray(tempLabels));
						tempValues.clear();
						tempLabels.clear();
						if(!Double.isFinite(corr))
							corr = 0.0;
						corrArray.add(corr);
					}
				}
				if(tempValues.size() > 0){
					double corr = new PearsonsCorrelation().correlation(
							AppUtility.toPrimitiveArray(tempValues), 
							AppUtility.toPrimitiveArray(tempLabels));
					if(!Double.isFinite(corr))
						corr = 0.0;
					corrArray.add(corr);
				}
				if(corrArray.size() > 0)
					outMap.put(ds, AppUtility.calcAvg(corrArray));
				else outMap.put(ds, 0.0);
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
