/**
 * 
 */
package ippoz.multilayer.detector.metric;

import ippoz.multilayer.detector.algorithm.DetectionAlgorithm;
import ippoz.multilayer.detector.commons.knowledge.Knowledge;
import ippoz.multilayer.detector.commons.support.TimedValue;

import java.util.ArrayList;
import java.util.List;

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
	public double[] evaluateMetric(DetectionAlgorithm alg, Knowledge knowledge){
		double average = 0;
		double std = 0;
		List<TimedValue> anomalyEvaluations = new ArrayList<TimedValue>(knowledge.size());
		for(int i=0;i<knowledge.size();i++){
			anomalyEvaluations.add(new TimedValue(knowledge.getTimestamp(i), alg.snapshotAnomalyRate(knowledge, i)));
			average = average + anomalyEvaluations.get(i).getValue();
			std = std + Math.pow(anomalyEvaluations.get(i).getValue(), 2);
		} 
		average = average / knowledge.size();
		std = Math.sqrt((std / knowledge.size()) - Math.pow(average, 2));
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

}
