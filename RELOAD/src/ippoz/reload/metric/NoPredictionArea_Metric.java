/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.metric.result.ArrayMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Tommy
 *
 */
public class NoPredictionArea_Metric extends BetterMinMetric {
	
	private double hazardRate;

	public NoPredictionArea_Metric(boolean validAfter, double hazardRate) {
		super(MetricType.NO_PREDICTION, validAfter);
		this.hazardRate = hazardRate;
	}
	
	@Override
	public String getMetricName() {
		return "NO_PREDICTION(" + hazardRate + ")";
	}

	@Override
	public String getMetricShortName() {
		return "NP(" + hazardRate + ")";
	}

	@Override
	public MetricResult evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations) {
		List<Double> fnList = new LinkedList<>();
		List<Double> otherList = new LinkedList<>();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for(AlgorithmResult tr : anomalyEvaluations){
			if(tr.isAnomalous() && tr.getScoreEvaluation() != AnomalyResult.ANOMALY)
				fnList.add(tr.getScore());
			else otherList.add(tr.getScore());
			if(tr.getScore() < min)
				min = tr.getScore();
			if(tr.getScore() > max)
				max = tr.getScore();
		}
		if(fnList.size() == 0 || fnList.size()*100.0/anomalyEvaluations.size() < hazardRate){
			return new ArrayMetricResult(new double[]{fnList.size()*100.0/anomalyEvaluations.size(), 0.0, Double.NaN, Double.NaN});
		}
		Map<Double, Integer> fnMap = toFrequencyMap(fnList);
		Map<Double, Integer> otherMap = toFrequencyMap(otherList);
		return new ArrayMetricResult(calculateNPArea(new ArrayList<>(fnMap.keySet()), fnMap, otherMap, min, max, fnList.size(), otherList.size()));
	}
	
	private double[] calcResidual(List<Double> fnKeys, Map<Double, Integer> fnMap, Map<Double, Integer> otherMap, double min, double max, int fnCount, int otherCount, int indexLeft, int indexRight){
		double left = indexLeft >= 0 ? fnKeys.get(indexLeft) : min;
		double right = indexRight < fnKeys.size() ? fnKeys.get(indexRight) : max;
		int fnNoPrediction = countBetween(fnMap, left, right);
		int fnPrediction = fnCount - fnNoPrediction;
		int otherNoPrediction = countBetween(otherMap, left, right);
		int otherPrediction = otherCount - otherNoPrediction;
		double npArea = (fnNoPrediction + otherNoPrediction)*100.0 / (fnCount + otherCount);
		double residuals = fnPrediction == 0 ? 0 : fnPrediction*100.0 / (fnPrediction + otherPrediction);
		return new double[]{npArea, residuals, left, right};
	}
	
	private double[] calculateNPArea(List<Double> fnKeys, Map<Double, Integer> fnMap, Map<Double, Integer> otherMap, double min, double max, int fnCount, int otherCount) {
		int indexLeft = 0;
		int indexRight = fnMap.size()-1;
		double[] minResidual = {100.0, 100.0, min, max};
		while(indexLeft < indexRight){
			//System.out.println("Execution with " + fnKeys.size() + " FN Data Points - " + indexLeft + " / " + indexRight);
			double[] leftResidual = calcResidual(fnKeys, fnMap, otherMap, min, max, fnCount, otherCount, indexLeft, indexRight-1);
			double[] rightResidual = calcResidual(fnKeys, fnMap, otherMap, min, max, fnCount, otherCount, indexLeft+1, indexRight);
			//System.out.println("Min Residual: " + Double.min(leftResidual[0], rightResidual[0]));
			if(Double.min(leftResidual[1], rightResidual[1]) > hazardRate)
				break;
			else {
				if(leftResidual[0] < rightResidual[0] || rightResidual[1] > hazardRate){
					minResidual = leftResidual;
					indexRight = indexRight-1;
				} else {
					minResidual = rightResidual;
					indexLeft = indexLeft + 1;
				}
			}
		}
		//System.out.println(Arrays.toString(minResidual));
		return minResidual;
	}

	private int countBetween(Map<Double, Integer> map, double thr1, double thr2){
		int counter = 0;
		if(map != null && map.size() > 0){
			List<Double> keys = new ArrayList<>(map.keySet());
			for(Double key : keys){
				if(key >= thr1){
					if(key > thr2)
						break;
					else {
						counter = counter + map.get(key);
					}
				}
			}
		}
		return counter;
	}
	
	public static double calculateOverlapDetail(List<Double> normalList, List<Double> faultyList){
		if(normalList.size() == 0 || faultyList.size() == 0)
			return 0.0;
		else {
			Collections.sort(normalList);
			Collections.sort(faultyList);
			boolean normalBeforeFaulty = normalList.get(0) < faultyList.get(0) ? true : false;
			if(normalBeforeFaulty)
				return overlapFirstBeforeSecond(normalList, faultyList);
			else return overlapFirstBeforeSecond(faultyList, normalList);
		}
	}
	
	private static Map<Double, Integer> toFrequencyMap(List<Double> list){
		Map<Double, Integer> map = new TreeMap<>();
		if(list != null && list.size() > 0){
			for(Double d : list){
				if(!map.containsKey(d))
					map.put(d, 1);
				else map.put(d, map.get(d)+1);
			}
		}
		return map;
	}
	
	private static double overlapFirstBeforeSecond(List<Double> first, List<Double> second){
		int good = 0;
		int bad = 0;
		Map<Double, Integer> firstMap = toFrequencyMap(first);
		List<Double> sortedFirstKeys = new ArrayList<Double>(firstMap.keySet());
		Collections.sort(sortedFirstKeys);
		Map<Double, Integer> secondMap = toFrequencyMap(second);
		List<Double> sortedSecondKeys = new ArrayList<Double>(secondMap.keySet());
		Collections.sort(sortedSecondKeys);
		int i = 0, j = 0;
		while(i<sortedFirstKeys.size() && j < sortedSecondKeys.size()){
			while(sortedFirstKeys.get(i) == null){
				i++;
			}
			if(i >= sortedFirstKeys.size())
				break;
			while(sortedSecondKeys.get(j) == null){
				j++;
			}
			if(j >= sortedSecondKeys.size())
				break;
			if(sortedFirstKeys.get(i) > sortedSecondKeys.get(j)){
				good = good + secondMap.get(sortedSecondKeys.get(j));
				j++;
			} else if(sortedFirstKeys.get(i) < sortedSecondKeys.get(j)){
				good = good + firstMap.get(sortedFirstKeys.get(i));
				i++;
			} else {
				bad = bad + firstMap.get(sortedFirstKeys.get(i)) + secondMap.get(sortedSecondKeys.get(j));
				j++;
				i++;
			}
		}
		while(i<sortedFirstKeys.size()){
			good = good + firstMap.get(sortedFirstKeys.get(i));
			i++;
		}
		while(j<sortedSecondKeys.size()){
			good = good + secondMap.get(sortedSecondKeys.get(j));
			j++;
		}
		
		return 100.0*bad / (good + bad);
	}

}

/*

@Override
public double evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations) {
	List<Double> normalList = new LinkedList<>();
	List<Double> faultyList = new LinkedList<>();
	for(AlgorithmResult tr : anomalyEvaluations){
		if(tr.isAnomalous())
			faultyList.add(tr.getScore());
		else normalList.add(tr.getScore());
	}
	return calculateOverlapDetail(normalList, faultyList);
}

@Override
public String getMetricName() {
	return "NO_PREDICTION";
}

@Override
public String getMetricShortName() {
	return "NO_PREDICTION";
}

public static double calculateOverlapDetail(List<Double> normalList, List<Double> faultyList){
	if(normalList.size() == 0 || faultyList.size() == 0)
		return 0.0;
	else {
		Collections.sort(normalList);
		Collections.sort(faultyList);
		boolean normalBeforeFaulty = normalList.get(0) < faultyList.get(0) ? true : false;
		if(normalBeforeFaulty)
			return overlapFirstBeforeSecond(normalList, faultyList);
		else return overlapFirstBeforeSecond(faultyList, normalList);
	}
}

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

private static double overlapFirstBeforeSecond(List<Double> first, List<Double> second){
	int good = 0;
	int bad = 0;
	Map<Double, Integer> firstMap = toFrequencyMap(first);
	List<Double> sortedFirstKeys = new ArrayList<Double>(firstMap.keySet());
	Collections.sort(sortedFirstKeys);
	Map<Double, Integer> secondMap = toFrequencyMap(second);
	List<Double> sortedSecondKeys = new ArrayList<Double>(secondMap.keySet());
	Collections.sort(sortedSecondKeys);
	int i = 0, j = 0;
	while(i<sortedFirstKeys.size() && j < sortedSecondKeys.size()){
		while(sortedFirstKeys.get(i) == null){
			i++;
		}
		if(i >= sortedFirstKeys.size())
			break;
		while(sortedSecondKeys.get(j) == null){
			j++;
		}
		if(j >= sortedSecondKeys.size())
			break;
		if(sortedFirstKeys.get(i) > sortedSecondKeys.get(j)){
			good = good + secondMap.get(sortedSecondKeys.get(j));
			j++;
		} else if(sortedFirstKeys.get(i) < sortedSecondKeys.get(j)){
			good = good + firstMap.get(sortedFirstKeys.get(i));
			i++;
		} else {
			bad = bad + firstMap.get(sortedFirstKeys.get(i)) + secondMap.get(sortedSecondKeys.get(j));
			j++;
			i++;
		}
	}
	while(i<sortedFirstKeys.size()){
		good = good + firstMap.get(sortedFirstKeys.get(i));
		i++;
	}
	while(j<sortedSecondKeys.size()){
		good = good + secondMap.get(sortedSecondKeys.get(j));
		j++;
	}
	
	return 100.0*bad / (good + bad);
}


private static double overlapFirstBeforeSecond(List<Double> first, List<Double> second){
	int good = 0;
	int bad = 0;
	Map<Double, Integer> firstMap = toFrequencyMap(first);
	List<Double> sortedFirstKeys = new ArrayList<Double>(firstMap.keySet());
	Collections.sort(sortedFirstKeys);
	Map<Double, Integer> secondMap = toFrequencyMap(second);
	List<Double> sortedSecondKeys = new ArrayList<Double>(secondMap.keySet());
	Collections.sort(sortedSecondKeys);
	int i = 0, j = 0;
	while(i<sortedFirstKeys.size() && j < sortedSecondKeys.size()){
		while(sortedFirstKeys.get(i) == null){
			i++;
		}
		if(i >= sortedFirstKeys.size())
			break;
		while(sortedSecondKeys.get(j) == null){
			j++;
		}
		if(j >= sortedSecondKeys.size())
			break;
		if(sortedFirstKeys.get(i) > sortedSecondKeys.get(j)){
			good = good + secondMap.get(sortedSecondKeys.get(j));
			j++;
		} else if(sortedFirstKeys.get(i) < sortedSecondKeys.get(j)){
			good = good + firstMap.get(sortedFirstKeys.get(i));
			i++;
		} else if(firstMap.get(sortedFirstKeys.get(i)) > secondMap.get(sortedSecondKeys.get(j))){
			good = good + firstMap.get(sortedFirstKeys.get(i));
			bad = bad + secondMap.get(sortedSecondKeys.get(j));
			j++;
			i++;
		} else {
			bad = bad + firstMap.get(sortedFirstKeys.get(i));
			good = good + secondMap.get(sortedSecondKeys.get(j));
			j++;
			i++;
		}
	}
	while(i<sortedFirstKeys.size()){
		good = good + firstMap.get(sortedFirstKeys.get(i));
		i++;
	}
	while(j<sortedSecondKeys.size()){
		good = good + secondMap.get(sortedSecondKeys.get(j));
		j++;
	}
	
	return 100.0*bad / (good + bad);
}*/
