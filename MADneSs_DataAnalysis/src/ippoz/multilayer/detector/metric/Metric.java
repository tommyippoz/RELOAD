/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.algorithm.DetectionAlgorithm;
import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.support.AppUtility;
import ippoz.multilayer.detector.voter.VotingResult;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	public double[] evaluateMetric(DetectionAlgorithm alg, List<Snapshot> snapList){
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
	public abstract double evaluateAnomalyResults(List<Snapshot> snapList, Map<Date, Double> anomalyEvaluations);

	/**
	 * Returns the anomaly evaluation for the given input data.
	 *
	 * @param expData the experiment data
	 * @param voting the voting results for each snapshot
	 * @param anomalyTreshold the anomaly threshold
	 * @return the global anomaly evaluation
	 */
	public double evaluateAnomalyResults(List<Snapshot> customArrayList, List<VotingResult> voting, double anomalyTreshold) {
		Map<Date, Double> convertedMap = new HashMap<Date, Double>(); 
		for(VotingResult vResult : voting){
			convertedMap.put(vResult.getDate(), vResult.getValue()/anomalyTreshold*1.0);
		}
		return evaluateAnomalyResults(customArrayList, convertedMap);
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
