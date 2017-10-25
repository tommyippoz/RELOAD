/**
 * 
 */
package ippoz.multilayer.detector.loader;

import ippoz.multilayer.detector.commons.data.ExperimentData;

import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public interface Loader {
	
	public abstract LinkedList<ExperimentData> fetch();
	
}
