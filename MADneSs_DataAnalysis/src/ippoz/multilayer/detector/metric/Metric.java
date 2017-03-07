/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.algorithm.DetectionAlgorithm;
import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.support.AppUtility;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * The Class Metric.
 * Defines a base metric. Needs to be extended from concrete metrics' classes.
 *
 * @author Tommy
 */
public abstract class Metric {
	
	/**
	 * Evaluates the experiment using the chosen metric.
	 *
	 * @param alg the algorithm
	 * @param expData the experiment data
	 * @return the anomaly evaluation [metric score, avg algorithm score, std algorithm score]
	 */
	public double[] evaluateMetric(DetectionAlgorithm alg, LinkedList<Snapshot> snapList){
		Snapshot currentSnapshot;
		double average;
		HashMap<Date, Double> anomalyEvaluations = new HashMap<Date, Double>();
		for(int i=0;i<snapList.size();i++){
			currentSnapshot = snapList.get(i);
			anomalyEvaluations.put(currentSnapshot.getTimestamp(), alg.snapshotAnomalyRate(currentSnapshot));
		}
		average = AppUtility.calcAvg(anomalyEvaluations.values());
		return new double[]{evaluateAnomalyResults(snapList, anomalyEvaluations), average, AppUtility.calcStd(anomalyEvaluations.values(), average)};
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
	public abstract double evaluateAnomalyResults(LinkedList<Snapshot> snapList, HashMap<Date, Double> anomalyEvaluations);

	/**
	 * Returns the anomaly evaluation for the given input data.
	 *
	 * @param expData the experiment data
	 * @param voting the voting results for each snapshot
	 * @param anomalyTreshold the anomaly threshold
	 * @return the global anomaly evaluation
	 */
	public double evaluateAnomalyResults(LinkedList<Snapshot> snapList, TreeMap<Date, Double> voting, double anomalyTreshold) {
		HashMap<Date, Double> convertedMap = new HashMap<Date, Double>(); 
		for(Date date : voting.keySet()){
			convertedMap.put(date, voting.get(date)/anomalyTreshold*1.0);
		}
		return evaluateAnomalyResults(snapList, convertedMap);
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

	

}
