/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.knowledge.data.MonitoredData;

import java.util.List;

/**
 * @author Tommy
 *
 */
public interface Loader {
	
	/** The Constant FILTERING_RUN_PREFERENCE. */
	public static final String FILTERING_RUN_PREFERENCE = "GOLDEN_RUN_IDS";
	
	/** The Constant TRAIN_RUN_PREFERENCE. */
	public static final String TRAIN_RUN_PREFERENCE = "TRAIN_RUN_IDS";
	
	/** The Constant VALIDATION_RUN_PREFERENCE. */
	public static final String VALIDATION_RUN_PREFERENCE = "VALIDATION_RUN_IDS";

	public static final String LOADER_TYPE = "LOADER_TYPE";

	public static final String CONSIDERED_LAYERS = "CONSIDERED_LAYERS";
	
	public abstract List<MonitoredData> fetch();
	
	public abstract String getRuns();

	public abstract String getName();
	
}
