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
	
	public abstract List<MonitoredData> fetch();
	
	public abstract String getRuns();
	
}
