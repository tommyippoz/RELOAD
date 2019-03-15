/**
 * 
 */
package ippoz.madness.detector.algorithm;

import ippoz.madness.detector.algorithm.result.AlgorithmResult;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class IndicatorDetectionAlgorithm.
 *
 * @author Tommy
 */
public abstract class DataSeriesDetectionAlgorithm extends DetectionAlgorithm {
	
	protected final static int DEFAULT_MINIMUM_ITEMS = 5;
	
	/** The indicator. */
	protected DataSeries dataSeries;

	/**
	 * Instantiates a new indicator detection algorithm.
	 *
	 * @param indicator the indicator
	 * @param categoryTag the data category tag
	 * @param conf the configuration
	 */
	public DataSeriesDetectionAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(conf);
		this.dataSeries = dataSeries;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + dataSeries.getName();
	}

	@Override
	public DataSeries getDataSeries() {
		return dataSeries;
	}
	
	protected List<Snapshot> toSnapList(List<Knowledge> kList){
		List<Snapshot> kSnapList = null;
		for(Knowledge knowledge : kList){
			if(kSnapList == null)
				kSnapList = knowledge.toArray(getAlgorithmType(), getDataSeries());
			else kSnapList.addAll(knowledge.toArray(getAlgorithmType(), getDataSeries()));
		}
		return kSnapList;
	}

	@Override
	protected AlgorithmResult evaluateSnapshot(Knowledge knowledge, int currentIndex) {
		return evaluateDataSeriesSnapshot(knowledge, knowledge.get(getAlgorithmType(), currentIndex, getDataSeries()), currentIndex);
	}

	protected abstract AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex);
	
}
