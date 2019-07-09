/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.knowledge.data.MonitoredData;

import java.util.List;

/**
 * The Interface Loader. This allows loading Knowledge from external data sources, either 
 * datasets, streams and databases.
 *
 * @author Tommy
 */
public interface Loader {
	
	/** The Constant FILTERING_RUN_PREFERENCE. */
	public static final String FILTERING_RUN_PREFERENCE = "GOLDEN_RUN_IDS";
	
	/** The Constant TRAIN_RUN_PREFERENCE. */
	public static final String TRAIN_RUN_PREFERENCE = "TRAIN_RUN_IDS";
	
	/** The Constant VALIDATION_RUN_PREFERENCE. */
	public static final String VALIDATION_RUN_PREFERENCE = "VALIDATION_RUN_IDS";

	/** The Constant LOADER_TYPE. */
	public static final String LOADER_TYPE = "LOADER_TYPE";

	/** The Constant CONSIDERED_LAYERS. */
	public static final String CONSIDERED_LAYERS = "CONSIDERED_LAYERS";
	
	/**
	 * Abstract function to fetch data, returning a list of MonitoredData.
	 *
	 * @return the list
	 */
	public abstract List<MonitoredData> fetch();
	
	/**
	 * Gets the runs used to fetch data.
	 *
	 * @return the runs
	 */
	public abstract String getRuns();

	/**
	 * Gets the name of the Loader.
	 *
	 * @return the name
	 */
	public abstract String getName();
	
}
