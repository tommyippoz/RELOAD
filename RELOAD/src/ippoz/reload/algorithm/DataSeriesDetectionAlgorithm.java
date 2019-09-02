/**
 * 
 */
package ippoz.reload.algorithm;

import java.io.File;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;

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
	
	/**
	 * Gets the filename used to store data about scores and histograms.
	 *
	 * @return the filename
	 */
	protected String getFilename(){
		return getDefaultTmpFolder() + File.separatorChar + getDataSeries().getCompactString().replace("\\", "_").replace("/", "-").replace("*", "_") + "." + getAlgorithmType().toString().toLowerCase();
	}
	
	/**
	 * Gets the default folder used to store temporary data.
	 *
	 * @return the default temporary folder
	 */
	protected String getDefaultTmpFolder(){
		if(conf.hasItem(AlgorithmConfiguration.DATASET_NAME) && conf.getItem(AlgorithmConfiguration.DATASET_NAME).length() > 0)
			return "tmp" + File.separatorChar + conf.getItem(AlgorithmConfiguration.DATASET_NAME) + File.separatorChar + getAlgorithmType().toString().toLowerCase() + "_tmp_RELOAD";
		else return "tmp" + File.separatorChar + getAlgorithmType().toString().toLowerCase() + "_tmp_RELOAD";
	}

	@Override
	public DataSeries getDataSeries() {
		return dataSeries;
	}

	@Override
	protected AlgorithmResult evaluateSnapshot(Knowledge knowledge, int currentIndex) {
		return evaluateDataSeriesSnapshot(knowledge, knowledge.get(getAlgorithmType(), currentIndex, getDataSeries()), currentIndex);
	}

	protected abstract AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex);
	
}
