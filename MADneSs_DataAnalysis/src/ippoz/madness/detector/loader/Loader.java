/**
 * 
 */
package ippoz.madness.detector.loader;

import ippoz.madness.detector.commons.knowledge.data.MonitoredData;

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
	
	public abstract List<MonitoredData> fetch();
	
	public abstract String getRuns();
	
}
