/**
 * 
 */
package ippoz.madness.detector.algorithm;

import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;

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
		return evaluateDataSeriesSnapshot(knowledge, knowledge.get(getAlgorithmType(), currentIndex, getDataSeries()), currentIndex);
	}

	protected abstract double evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex);
	
}
