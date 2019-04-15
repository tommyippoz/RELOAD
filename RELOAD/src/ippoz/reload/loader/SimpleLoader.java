/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.knowledge.data.MonitoredData;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class SimpleLoader implements Loader {
	
	protected List<MonitoredData> dataList;
	private List<Integer> runs;

	public SimpleLoader(List<Integer> runs){
		Collections.sort(runs);
		this.runs = runs;
		dataList = new LinkedList<MonitoredData>();
	}
	
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
