/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.TimedResult;
import ippoz.reload.commons.support.ValueSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class Metric. Defines a base metric. Needs to be extended from concrete
 * metrics' classes.
 *
 * @author Tommy
 */
public abstract class Metric implements Comparable<Metric> {

	private MetricType mType;

	public Metric(MetricType mType) {
		this.mType = mType;
	}

	/**
	 * Evaluates the experiment using the chosen metric.
	 *
	 * @param alg
	 *            the algorithm
	 * @param expData
	 *            the experiment data
	 * @return the anomaly evaluation [metric score, avg algorithm score, std
	 *         algorithm score]
	 */
	public double[] evaluateMetric(DetectionAlgorithm alg, Knowledge know) {
		double average = 0;
		double std = 0;
		double snapValue;
		int undetectable = 0;
		Knowledge knowledge = know.cloneKnowledge();
		List<TimedResult> anomalyEvaluations = new ArrayList<TimedResult>(knowledge.size());
		for (int i = 0; i < knowledge.size(); i++) {
			AlgorithmResult ar = alg.snapshotAnomalyRate(knowledge, i);
			snapValue = DetectionAlgorithm.convertResultIntoDouble(ar.getScoreEvaluation());
			anomalyEvaluations.add(new TimedResult(knowledge.getTimestamp(i), snapValue, ar.getScore(), knowledge.getInjection(i)));
			if (snapValue >= 0.0) {
				average = average + anomalyEvaluations.get(i).getValue();
				std = std + Math.pow(anomalyEvaluations.get(i).getValue(), 2);
			} else
				undetectable++;
			if (knowledge instanceof SlidingKnowledge) {
				((SlidingKnowledge) knowledge).slide(i, snapValue);
			}
		}
		if (knowledge instanceof SlidingKnowledge) {
			((SlidingKnowledge) knowledge).reset();
		}
		average = average / (knowledge.size() - undetectable);
		std = Math.sqrt((std / (knowledge.size() - undetectable))
				- Math.pow(average, 2));
		return new double[] {evaluateAnomalyResults(anomalyEvaluations), average, std};
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Metric)
			return ((Metric) obj).getMetricName().equals(getMetricName());
		else
			return super.equals(obj);
	}

	/**
	 * Evaluates anomaly results coming from evaluations of all the snapshot of
	 * an experiment.
	 *
	 * @param expData
	 *            the experiment data
	 * @param anomalyEvaluations
	 *            the anomaly evaluations
	 * @return the global anomaly evaluation
	 */
	public abstract double evaluateAnomalyResults(List<TimedResult> anomalyEvaluations);

	/**
	 * Returns the anomaly evaluation for the given input data.
	 *
	 * @param expData
	 *            the experiment data
	 * @param voting
	 *            the voting results for each snapshot
	 * @param anomalyTreshold
	 *            the anomaly threshold
	 * @return the global anomaly evaluation
	 */
	public double evaluateAnomalyResults(List<TimedResult> voting, double anomalyTreshold) {
		List<TimedResult> votingWithTreshold = new ArrayList<TimedResult>(voting.size());
		for (TimedResult vResult : voting) {
			votingWithTreshold.add(new TimedResult(vResult.getDate(), vResult.getValue() / anomalyTreshold * 1.0, vResult.getAlgorithmScore(), vResult.getInjectedElement()));
		}
		return evaluateAnomalyResults(votingWithTreshold);
	}

	/**
	 * Compares metric results.
	 *
	 * @param currentMetricValue
	 *            the current metric value
	 * @param bestMetricValue
	 *            the best metric value
	 * @return the comparison result
	 */
	public abstract int compareResults(double currentMetricValue,
			double bestMetricValue);

	/**
	 * Converts numeric into boolean anomaly evaluation.
	 *
	 * @param anomalyValue
	 *            the anomaly value
	 * @return true if anomaly value is over 1.0
	 */
	public static boolean anomalyTrueFalse(double anomalyValue) {
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

	public MetricType getMetricType() {
		return mType;
	}

	public static String getAverageMetricValue(List<Map<Metric, Double>> list,
			Metric met) {
		List<Double> dataList = new ArrayList<Double>();
		if (list != null) {
			for (Map<Metric, Double> map : list) {
				if(map != null) {
					if (map.get(met) != null)
						dataList.add(map.get(met));
					else {
						for (Metric m : map.keySet()) {
							if (m.equals(met)) {
								dataList.add(map.get(m));
								break;
							}
						}
					}
				}
			}
			return String.valueOf(AppUtility.calcAvg(dataList));
		} else
			return String.valueOf(Double.NaN);
	}

	@Override
	public int compareTo(Metric o) {
		return o.getMetricName().compareTo(getMetricName());
	}

	public int compareResults(ValueSeries m1, ValueSeries m2) {
		return compareResults(m1.getAvg(), m2.getAvg());
	}

}
