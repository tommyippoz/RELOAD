/**
 * 
 */
package ippoz.multilayer.detector.loader;

import ippoz.multilayer.detector.commons.data.ExperimentData;

import java.util.List;

/**
 * @author Tommy
 *
 */
public interface Loader {
	
	public abstract List<ExperimentData> fetch();
	
	public abstract String getRuns();
	
}
