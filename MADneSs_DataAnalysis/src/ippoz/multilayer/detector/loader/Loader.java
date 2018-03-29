/**
 * 
 */
package ippoz.multilayer.detector.loader;

import ippoz.multilayer.detector.commons.knowledge.data.MonitoredData;

import java.util.List;

/**
 * @author Tommy
 *
 */
public interface Loader {
	
	public abstract List<MonitoredData> fetch();
	
	public abstract String getRuns();
	
}
