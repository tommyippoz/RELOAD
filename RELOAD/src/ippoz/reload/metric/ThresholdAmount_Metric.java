/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tommy
 *
 */
public class ThresholdAmount_Metric extends BetterMinMetric {

	public ThresholdAmount_Metric(boolean validAfter) {
		super(MetricType.THRESHOLDS, validAfter);
	}

	@Override
	public double evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations) {
		List<Double> normalList = new LinkedList<>();
		List<Double> faultyList = new LinkedList<>();
		for(AlgorithmResult tr : anomalyEvaluations){
			if(tr.hasInjection())
				faultyList.add(tr.getScore());
			else normalList.add(tr.getScore());
		}
		return calculateThresholdsAmount(toFrequencyMap(normalList).keySet(), toFrequencyMap(faultyList).keySet());
	}

	@Override
	public String getMetricName() {
		return "THRESHOLDS_AMOUNT";
	}

	@Override
	public String getMetricShortName() {
		return "THRESHOLDS_AMOUNT";
	}
	
	public static int calculateThresholdsAmount(Set<Double> normalSet, Set<Double> faultySet){
		int thresholdCount = 0;
		
		if(normalSet.size() == 0 || faultySet.size() == 0)
			return 0;
		
		else {
			List<Double> normalList = new ArrayList<>(normalSet);
			List<Double> faultyList = new ArrayList<>(faultySet);
			
			Collections.sort(normalList);
			Collections.sort(faultyList);
			
			int i = 0, j = 0;
			LastItem lItem = LastItem.BOTH;
			while(i < normalList.size() && j < faultyList.size()){
				if(normalList.get(i) > faultyList.get(j)){
					if(lItem != LastItem.ANOMALY)
						thresholdCount++;
					j++;
					lItem = LastItem.ANOMALY;
				} else if(normalList.get(i) < faultyList.get(j)){
					if(lItem != LastItem.NORMAL)
						thresholdCount++;
					i++;
					lItem = LastItem.NORMAL;
				} else {
					if(lItem != LastItem.BOTH)
						thresholdCount++;
					j++;
					i++;
					lItem = LastItem.BOTH;
				}
			}
			return thresholdCount;
		}
	}
	
	private enum LastItem {NORMAL, ANOMALY, BOTH};
	
	private static Map<Double, Integer> toFrequencyMap(List<Double> list){
		Map<Double, Integer> map = new HashMap<>();
		if(list != null && list.size() > 0){
			for(Double d : list){
				if(!map.containsKey(d))
					map.put(d, 1);
				else map.put(d, map.get(d)+1);
			}
		}
		return map;
	}

}
