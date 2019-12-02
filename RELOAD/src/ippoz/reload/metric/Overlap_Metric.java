/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;

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
	public double evaluateAnomalyResults(List<? extends AlgorithmResult> anomalyEvaluations) {
		List<Double> normalList = new LinkedList<>();
		List<Double> faultyList = new LinkedList<>();
		for(AlgorithmResult tr : anomalyEvaluations){
			if(tr.getInjection() != null)
				faultyList.add(tr.getScore());
			else normalList.add(tr.getScore());
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
			boolean normalBeforeFaulty = normalList.get(0) < faultyList.get(0) ? true : false;
			boolean normalEmbedsFaulty = faultyList.get(0) >= normalList.get(0) && 
						faultyList.get(faultyList.size()-1) <= normalList.get(normalList.size()-1);
			boolean faultyEmbedsNormal = normalList.get(0) >= faultyList.get(0) && 
					normalList.get(normalList.size()-1) <= faultyList.get(faultyList.size()-1);
			if(normalEmbedsFaulty)
				return overlapFirstEmbedsSecond(normalList, faultyList);
			else if(faultyEmbedsNormal)
				return overlapFirstEmbedsSecond(faultyList, normalList);
			else if(normalBeforeFaulty)
				return overlapFirstBeforeSecond(normalList, faultyList);
			else return overlapFirstBeforeSecond(faultyList, normalList);
		}
	}
	
	private static double overlapFirstBeforeSecond(List<Double> first, List<Double> second){
		int good = 0;
		int bad = 0;
		for(Double d : first){
			if(d < second.get(0))
				good++;
			else bad++;
		}
		for(Double d : second){
			if(d > first.get(first.size()-1))
				good++;
			else bad++;
		}
		return 100.0*bad / (good + bad);
	}
	
	private static double overlapFirstEmbedsSecond(List<Double> first, List<Double> second){
		int good = 0;
		int bad = 0;
		for(Double d : first){
			if(d < second.get(0) || d > second.get(second.size()-1))
				good++;
			else bad++;
		}
		bad = bad + second.size();
		return 100.0*bad / (good + bad);
	}

}
