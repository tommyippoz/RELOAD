/**
 * 
 */
package ippoz.multilayer.detector.algorithm;

import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.knowledge.Knowledge;
import ippoz.multilayer.detector.commons.knowledge.snapshot.DataSeriesSnapshot;

// TODO: Auto-generated Javadoc
/**
 * The Class IndicatorDetectionAlgorithm.
 *
 * @author Tommy
 */
public abstract class DataSeriesDetectionAlgorithm extends DetectionAlgorithm {
	
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

	@Override
	protected double evaluateSnapshot(Knowledge knowledge, int currentIndex) {
		return evaluateDataSeriesSnapshot(knowledge, (DataSeriesSnapshot)knowledge.get(getAlgorithmType(), currentIndex, getDataSeries()), currentIndex);
	}

	protected abstract double evaluateDataSeriesSnapshot(Knowledge knowledge, DataSeriesSnapshot sysSnapshot, int currentIndex);
	
}
