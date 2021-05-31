/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.metric.result.ArrayMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.ArrayList;
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

	public NoPredictionArea_Metric(double hazardRate) {
		super(MetricType.NO_PREDICTION, hazardRate);
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
		int trues = 0;
		for(AlgorithmResult tr : anomalyEvaluations){
			if(tr.getScoreEvaluation() != AnomalyResult.ANOMALY){
				if(tr.isAnomalous())
					fnList.add(tr.getScore());
				else otherList.add(tr.getScore());
			} else trues++;
			if(tr.getScore() < min)
				min = tr.getScore();
			if(tr.getScore() > max)
				max = tr.getScore();
		}
		if(fnList.size() == 0 || fnList.size()*100.0/anomalyEvaluations.size() < hazardRate){
			return new ArrayMetricResult(new double[]{0.0, fnList.size()*100.0/anomalyEvaluations.size(), Double.NaN, Double.NaN});
		}
		Map<Double, Integer> fnMap = toFrequencyMap(fnList);
		Map<Double, Integer> otherMap = toFrequencyMap(otherList);
		return new ArrayMetricResult(calculateNPArea(new ArrayList<>(fnMap.keySet()), (fnList.size() + otherList.size())*100.0/anomalyEvaluations.size(), trues, fnMap, otherMap, min, max, fnList.size(), anomalyEvaluations.size() - fnList.size()));
	}
	
	private double[] calculateNPArea(List<Double> fnKeys, double startingNp, int trues, Map<Double, Integer> fnMap, Map<Double, Integer> otherMap, double min, double max, int fnCount, int otherCount) {
		int indexLeft = 0;
		int indexRight = fnMap.size()-1;
		double[] minResidual = {startingNp, 100.0, min, max};
		while(indexLeft < indexRight){
			//System.out.println("Execution with " + fnKeys.size() + " FN Data Points - " + indexLeft + " / " + indexRight);
			double[] leftResidual = calcResidual(fnKeys, trues, fnMap, otherMap, min, max, fnCount, otherCount, indexLeft, indexRight-1);
			double[] rightResidual = calcResidual(fnKeys, trues, fnMap, otherMap, min, max, fnCount, otherCount, indexLeft+1, indexRight);
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
	
	private double[] calcResidual(List<Double> fnKeys, int trues, Map<Double, Integer> fnMap, Map<Double, Integer> otherMap, double min, double max, int fnCount, int otherCount, int indexLeft, int indexRight){
		double left = indexLeft >= 0 ? fnKeys.get(indexLeft) : min;
		double right = indexRight < fnKeys.size() ? fnKeys.get(indexRight) : max;
		int fnNoPrediction = countBetween(fnMap, left, right);
		int fnPrediction = fnCount - fnNoPrediction;
		int otherNoPrediction = countBetween(otherMap, left, right);
		//int otherPrediction = otherCount - otherNoPrediction;
		double npArea = (fnNoPrediction + otherNoPrediction)*100.0 / (fnCount + otherCount + trues);
		double residuals = fnPrediction == 0 ? 0 : fnPrediction*100.0 / (fnCount + otherCount);
		return new double[]{npArea, residuals, left, right};
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
	
}
