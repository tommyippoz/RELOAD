/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.knowledge.data.MonitoredData;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleLoader.
 *
 * @author Tommy
 */
public abstract class SimpleLoader implements Loader {
	
	/** The data list. */
	protected List<MonitoredData> dataList;
	
	/** The runs. */
	private List<Integer> runs;

	/**
	 * Instantiates a new simple loader.
	 *
	 * @param runs the runs
	 */
	public SimpleLoader(List<Integer> runs){
		Collections.sort(runs);
		this.runs = runs;
		dataList = new LinkedList<MonitoredData>();
	}
	
	/**
	 * True if a given row of the dataset should be read.
	 *
	 * @param index the index
	 * @return true, if successful
	 */
	public synchronized boolean canRead(int index){
		if(runs != null && runs.size() > 0){
			while(runs.size() > 0 && index > runs.get(0)){
				runs.remove(0);
			}
			if(runs.size() > 0)
				return index == runs.get(0);
			else return false;
		} else return false;
	}

}
