/**
 * 
 */
package ippoz.multilayer.detector.loader;

import ippoz.multilayer.detector.commons.data.ExperimentData;

import java.util.Collections;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public abstract class SimpleLoader implements Loader {
	
	protected LinkedList<ExperimentData> dataList;
	private LinkedList<Integer> runs;
		
	public SimpleLoader(LinkedList<Integer> runs){
		Collections.sort(runs);
		this.runs = runs;
		dataList = new LinkedList<ExperimentData>();
	}
	
	public synchronized boolean canRead(int index){
		if(runs != null && runs.size() > 0){
			while(runs.size() > 0 && index > runs.getFirst()){
				runs.removeFirst();
			}
			if(runs.size() > 0)
				return index == runs.getFirst();
			else return false;
		} else return false;
	}

}
