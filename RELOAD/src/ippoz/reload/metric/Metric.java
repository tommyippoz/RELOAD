/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
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
	 * Gets the metric.
	 *
	 * @param metricType the metric tag
	 * @return the obtained metric
	 */
	public static Metric fromString(String metricType, String mType, boolean validAfter){
		String param = null;
		boolean absolute = mType != null && mType.equals("absolute") ? true : false;
		if(metricType.contains("(")){
			param = metricType.substring(metricType.indexOf('(')+1, metricType.indexOf(')'));
			metricType = metricType.substring(0, metricType.indexOf('('));
		}
		switch(metricType.toUpperCase()){
			case "TP":
			case "TRUEPOSITIVE":
				return new TP_Metric(absolute, validAfter);
			case "TN":
			case "TRUENEGATIVE":
				return new TN_Metric(absolute, validAfter);
			case "FN":
			case "FALSENEGATIVE":
				return new FN_Metric(absolute, validAfter);
			case "FP":
			case "FALSEPOSITIVE":
				return new FP_Metric(absolute, validAfter);
			case "PRECISION":
				return new Precision_Metric(validAfter);
			case "RECALL":
				return new Recall_Metric(validAfter);
			case "F-MEASURE":
			case "FMEASURE":
				return new FMeasure_Metric(validAfter);
			case "G-MEAN":
			case "GMEAN":
			case "GMEANS":
				return new GMean_Metric(validAfter);
			case "F-SCORE":
			case "FSCORE":
				if(param != null && param.trim().length() > 0 && AppUtility.isNumber(param.trim()))
					return new FScore_Metric(Double.valueOf(param), validAfter);
				else return new FMeasure_Metric(validAfter);
			case "FPR":
				return new FalsePositiveRate_Metric(validAfter);
			case "MCC":
			case "MATTHEWS":
			case "MATTHEWSCORRELATIONCOEFFICIENT":
				return new Matthews_Coefficient(validAfter);
			case "AUC":
				return new AUC_Metric(validAfter);
			case "ACCURACY":
				return new Accuracy_Metric(validAfter);
			case "SSCORE":
			case "SAFESCORE":
			case "SAFE_SCORE":
				if(param != null && param.trim().length() > 0 && AppUtility.isNumber(param.trim()))
					return new SafeScore_Metric(Double.valueOf(param), validAfter);
				else return new SafeScore_Metric(2.0, validAfter);
			case "CUSTOM":
				return new Custom_Metric(validAfter);
			case "OVERLAP":
				return new Overlap_Metric(validAfter);
			case "OVERLAPD":
			case "OVERLAPDETAIL":
			case "OVERLAP_DETAIL":
			case "NOPREDICTION":
			case "NO_PREDICTION":
			case "NOP":
			case "NPR":
				return new NoPredictionArea_Metric(validAfter);
			case "THRESHOLD":
			case "THRESHOLDS":
			case "THRESHOLDS_AMOUNT":
				return new ThresholdAmount_Metric(validAfter);
			case "TPCONF":
			case "TP_CONFIDENCE":
				return new TPConfidence_Metric(validAfter);
			case "TNCONF":
			case "TN_CONFIDENCE":
				return new TNConfidence_Metric(validAfter);
			case "FPCONF":
			case "FP_CONFIDENCE":
				return new FPConfidence_Metric(validAfter);
			case "FNCONF":
			case "FN_CONFIDENCE":
				return new FNConfidence_Metric(validAfter);
			case "CONFIDENCE_ERROR":
			case "CONFERROR":
				if(param != null && param.trim().length() > 0 && AppUtility.isNumber(param.trim()))
					return new ConfidenceErrorMetric(validAfter, Double.valueOf(param));
				else return new ConfidenceErrorMetric(validAfter, 1.0);
			default:
				AppLogger.logError(Metric.class, "MissingPreferenceError", "Metric '" + metricType + "' cannot be defined. Default FMeasure will be used");
				return new FMeasure_Metric(validAfter);
		}
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
		List<AlgorithmResult> anomalyEvaluations = new ArrayList<AlgorithmResult>(knowledge.size());
		for (int i = 0; i < knowledge.size(); i++) {
			AlgorithmResult ar = alg.snapshotAnomalyRate(knowledge, i);
			snapValue = DetectionAlgorithm.convertResultIntoDouble(ar.getScoreEvaluation());
			anomalyEvaluations.add(ar);
			if (snapValue >= 0.0) {
				average = average + snapValue;
				std = std + Math.pow(snapValue, 2);
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
	public abstract double evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations);

	/**
	 * Compares metric results.
	 *
	 * @param currentMetricValue
	 *            the current metric value
	 * @param bestMetricValue
	 *            the best metric value
	 * @return the comparison result
	 */
	public abstract int compareResults(double currentMetricValue, double bestMetricValue);

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
