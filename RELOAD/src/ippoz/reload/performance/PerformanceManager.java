/**
 * 
 */
package ippoz.reload.performance;

import ippoz.reload.commons.knowledge.data.MonitoredData;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class PerformanceManager {
	
	private static LinkedList<ExperimentPerformance> expPerformances;
	
	public static void addExperimentPerformance(MonitoredData expData, HashMap<String, Integer> algCategories, HashMap<String, LinkedList<Integer>> timeSeries){
		if(expPerformances == null)
			expPerformances = new LinkedList<ExperimentPerformance>();
		expPerformances.add(new ExperimentPerformance(expData, algCategories, timeSeries));
	}
	
	public static LinkedList<String> getPerformanceSummary(){
		LinkedList<String> perfSummary = new LinkedList<String>();
		for(String header : expPerformances.getFirst().summaryHeader()){
			perfSummary.add(header);
		}
		for(ExperimentPerformance ep : expPerformances){
			perfSummary.add(ep.compactSummary());
		}
		return perfSummary;
	}

}
