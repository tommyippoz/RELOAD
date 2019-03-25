/**
 * 
 */
package ippoz.madness.detector.metric;

import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.commons.support.TimedValue;
import ippoz.madness.detector.commons.support.ValueSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class Metric.
 * Defines a base metric. Needs to be extended from concrete metrics' classes.
 *
 * @author Tommy
 */
public abstract class Metric implements Comparable<Metric> {
	
	private MetricType mType;
	
	public Metric(MetricType mType){
		this.mType = mType;
	}
	
	/**
	 * Evaluates the experiment using the chosen metric.
	 *
	 * @param alg the algorithm
	 * @param expData the experiment data
	 * @return the anomaly evaluation [metric score, avg algorithm score, std algorithm score]
	 */
	public double[] evaluateMetric(DetectionAlgorithm alg, Knowledge know){
		double average = 0;
		double std = 0;
		double snapValue;
		int undetectable = 0;
		Knowledge knowledge = know.cloneKnowledge();
		List<TimedValue> anomalyEvaluations = new ArrayList<TimedValue>(knowledge.size());
		for(int i=0;i<knowledge.size();i++){
			snapValue = DetectionAlgorithm.convertResultIntoDouble(alg.snapshotAnomalyRate(knowledge, i).getScoreEvaluation());
			anomalyEvaluations.add(new TimedValue(knowledge.getTimestamp(i), snapValue));
			if(snapValue >= 0.0) {
				average = average + anomalyEvaluations.get(i).getValue();
				std = std + Math.pow(anomalyEvaluations.get(i).getValue(), 2);
			} else undetectable++;
			if(knowledge instanceof SlidingKnowledge){
				((SlidingKnowledge)knowledge).slide(i, snapValue);
			}
		} 
		if(knowledge instanceof SlidingKnowledge){
			((SlidingKnowledge)knowledge).reset();
		}
		average = average / (knowledge.size() - undetectable);
		std = Math.sqrt((std / (knowledge.size() - undetectable)) - Math.pow(average, 2));
		return new double[]{evaluateAnomalyResults(knowledge, anomalyEvaluations), average, std};
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Metric)
			return ((Metric)obj).getMetricName().equals(getMetricName());
		else return super.equals(obj);
	}

	/**
	 * Evaluates anomaly results coming from evaluations of all the snapshot of an experiment.
	 *
	 * @param expData the experiment data
	 * @param anomalyEvaluations the anomaly evaluations
	 * @return the global anomaly evaluation
	 */
	public abstract double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> anomalyEvaluations);

	/**
	 * Returns the anomaly evaluation for the given input data.
	 *
	 * @param expData the experiment data
	 * @param voting the voting results for each snapshot
	 * @param anomalyTreshold the anomaly threshold
	 * @return the global anomaly evaluation
	 */
	public double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> voting, double anomalyTreshold) {
		List<TimedValue> votingWithTreshold = new ArrayList<TimedValue>(voting.size());
		for(TimedValue vResult : voting){
			votingWithTreshold.add(new TimedValue(vResult.getDate(), vResult.getValue()/anomalyTreshold*1.0));
		}
		return evaluateAnomalyResults(knowledge, votingWithTreshold);
	}
	
	/**
	 * Compares metric results.
	 *
	 * @param currentMetricValue the current metric value
	 * @param bestMetricValue the best metric value
	 * @return the comparison result
	 */
	public abstract int compareResults(double currentMetricValue, double bestMetricValue);
	
	/**
	 * Converts numeric into boolean anomaly evaluation.
	 *
	 * @param anomalyValue the anomaly value
	 * @return true if anomaly value is over 1.0
	 */
	public static boolean anomalyTrueFalse(double anomalyValue){
		return anomalyValue >= 1.0;
	}

	/**
	 * Gets the metric name.
	 *
	 * @return the metric name
	 */
	public abstract String getMetricName();
	
	/**
	 * Gets the metric short name.
	 *
	 * @return the metric short name
	 */
	public abstract String getMetricShortName();

	public MetricType getMetricType(){
		return mType;
	}
	
	public static String getAverageMetricValue(List<Map<Metric, Double>> list, Metric met) {
		List<Double> dataList = new ArrayList<Double>();
		if(list != null){
			for(Map<Metric, Double> map : list){
				if(map.get(met) != null)
					dataList.add(map.get(met));
				else {
					for(Metric m : map.keySet()){
						if(m.equals(met)){
							dataList.add(map.get(m));
							break;
						}
					}
				}
			}
			return String.valueOf(AppUtility.calcAvg(dataList));
		} else return String.valueOf(Double.NaN);
	}

	@Override
	public int compareTo(Metric o) {
		return o.getMetricName().compareTo(getMetricName());
	}

	public int compareResults(ValueSeries m1, ValueSeries m2) {
		return compareResults(m1.getAvg(), m2.getAvg());
	}
	
}
