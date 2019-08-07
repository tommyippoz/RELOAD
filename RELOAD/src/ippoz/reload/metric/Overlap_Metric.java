/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.TimedResult;
import ippoz.reload.commons.support.TimedValue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class Overlap_Metric extends BetterMinMetric {

	public Overlap_Metric(boolean validAfter) {
		super(MetricType.OVERLAP, validAfter);
	}

	@Override
	public double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		List<Double> normalList = new LinkedList<>();
		List<Double> faultyList = new LinkedList<>();
		for(TimedValue tv : anomalyEvaluations){
			TimedResult tr = (TimedResult)tv;
			if(tr.getInjectedElement() != null)
				faultyList.add(tr.getAlgorithmScore());
			else normalList.add(tr.getAlgorithmScore());
		}
		return calculateOverlap(normalList, faultyList);
	}

	@Override
	public String getMetricName() {
		return "OVERLAP";
	}

	@Override
	public String getMetricShortName() {
		return "OVERLAP";
	}
	
	public static double calculateOverlap(List<Double> normalList, List<Double> faultyList){
		if(normalList.size() == 0 || faultyList.size() == 0)
			return 0.0;
		else {
			Collections.sort(normalList);
			Collections.sort(faultyList);
			boolean minSeries = normalList.get(0) < faultyList.get(0) ? true : false;
			int good = 0;
			int bad = 0;
			for(Double d : normalList){
				if(minSeries){ 
					if(d < faultyList.get(0))
						good++;
					else bad++;
				} else {
					if(d > faultyList.get(faultyList.size()-1))
						good++;
					else bad++;
				}
			}
			for(Double d : faultyList){
				if(minSeries){ 
					if(d > normalList.get(normalList.size()-1))
						good++;
					else bad++;
				} else {
					if(d < normalList.get(0))
						good++;
					else bad++;
				}
			}
			return 1.0*bad / (good + bad);
		}
	}

}
