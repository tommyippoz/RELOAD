/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

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
	
	private double noPredTHR;

	public Metric(MetricType mType) {
		this.mType = mType;
		this.noPredTHR = Double.NaN;
	}
	
	public Metric(MetricType mType, double noPredTHR) {
		this.mType = mType;
		this.noPredTHR = noPredTHR;
	}
	
	protected double getNoPredictionThreshold(){
		return noPredTHR;
	}
	
	private static String[] processParams(String str){
		String[] splitted = new String[0];
		if(str != null && str.contains("(")){
			String params = str.substring(str.indexOf('(')+1, str.indexOf(')'));
			splitted = params.split(",");
			for(int i=0;i<splitted.length;i++){
				if(splitted[i] != null)
					splitted[i] = splitted[i].trim();
			}
		}
		return splitted;
	}
	
	/**
	 * Gets the metric.
	 *
	 * @param metricType the metric tag
	 * @return the obtained metric
	 */
	public static Metric fromString(String metricType, String mType){
		String[] params = processParams(metricType);
		boolean absolute = mType != null && mType.equals("absolute") ? true : false;
		if(metricType != null && metricType.contains("(")){
			metricType = metricType.substring(0, metricType.indexOf("("));
		}
		double noPredictionTHR = processNPRParam(params, metricType);
		switch(metricType.toUpperCase()){
			case "TP":
			case "TRUEPOSITIVE":
				return new TP_Metric(absolute, noPredictionTHR);
			case "TN":
			case "TRUENEGATIVE":
				return new TN_Metric(absolute, noPredictionTHR);
			case "FN":
			case "FALSENEGATIVE":
				return new FN_Metric(absolute, noPredictionTHR);
			case "FP":
			case "FALSEPOSITIVE":
				return new FP_Metric(absolute, noPredictionTHR);
			case "TPUNK":
			case "TP_UNK":
			case "TRUEPOSITIVE_UNK":
				return new TP_Unk_Metric(absolute, noPredictionTHR);
			case "FNUNK":
			case "FN_UNK":
			case "FALSENEGATIVE_UNK":
				return new FN_Unk_Metric(absolute, noPredictionTHR);
			case "PRECISION":
				return new Precision_Metric(noPredictionTHR);
			case "RECALL":
				return new Recall_Metric(noPredictionTHR);
			case "F-MEASURE":
			case "FMEASURE":
				return new FMeasure_Metric(noPredictionTHR);
			case "G-MEAN":
			case "GMEAN":
			case "GMEANS":
				return new GMean_Metric(noPredictionTHR);
			case "F-SCORE":
			case "FSCORE":
				if(params != null && params.length > 0 && AppUtility.isNumber(params[0].trim()))
					return new FScore_Metric(Double.valueOf(params[0]), noPredictionTHR);
				else return new FMeasure_Metric(noPredictionTHR);
			case "FPR":
				return new FalsePositiveRate_Metric(noPredictionTHR);
			case "MCC":
			case "MATTHEWS":
			case "MATTHEWSCORRELATIONCOEFFICIENT":
				return new Matthews_Coefficient(noPredictionTHR);
			case "AUC":
				return new AUC_Metric(noPredictionTHR);
			case "ACCURACY":
				return new Accuracy_Metric(noPredictionTHR);
			case "SSCORE":
			case "SAFESCORE":
			case "SAFE_SCORE":
				if(params != null && params.length > 0 && AppUtility.isNumber(params[0].trim()))
					return new SafeScore_Metric(Double.valueOf(params[0]), noPredictionTHR);
				else return new SafeScore_Metric(2.0, noPredictionTHR);
			case "CUSTOM":
				return new Custom_Metric(noPredictionTHR);
			case "OVERLAP":
				return new Overlap_Metric(noPredictionTHR);
			case "OVERLAPD":
			case "OVERLAPDETAIL":
			case "OVERLAP_DETAIL":
			case "NOPREDICTION":
			case "NO_PREDICTION":
			case "NOP":
			case "NPR":
				return new NoPredictionArea_Metric(noPredictionTHR);
			case "THRESHOLD":
			case "THRESHOLDS":
			case "THRESHOLDS_AMOUNT":
				return new ThresholdAmount_Metric(noPredictionTHR);
			case "TPCONF":
			case "TP_CONFIDENCE":
				return new TPConfidence_Metric(noPredictionTHR);
			case "TNCONF":
			case "TN_CONFIDENCE":
				return new TNConfidence_Metric(noPredictionTHR);
			case "FPCONF":
			case "FP_CONFIDENCE":
				return new FPConfidence_Metric(noPredictionTHR);
			case "FNCONF":
			case "FN_CONFIDENCE":
				return new FNConfidence_Metric(noPredictionTHR);
			case "CONFIDENCE_ERROR":
			case "CONFERROR":
				if(params != null && params.length > 0 && AppUtility.isNumber(params[0].trim()))
					return new ConfidenceErrorMetric(Double.valueOf(params[0]), noPredictionTHR);
				else return new ConfidenceErrorMetric(1.0, noPredictionTHR);
			default:
				AppLogger.logError(Metric.class, "MissingPreferenceError", "Metric '" + metricType + "' cannot be defined. Default FMeasure will be used");
				return new FMeasure_Metric(noPredictionTHR);
		}
	}

	private static double processNPRParam(String[] params, String metricType) {
		switch(metricType.toUpperCase()){
			case "F-SCORE":
			case "FSCORE":
			case "SSCORE":
			case "SAFESCORE":
			case "SAFE_SCORE":
			case "CONFIDENCE_ERROR":
			case "CONFERROR":
				if(params != null && params.length > 1 && AppUtility.isNumber(params[1].trim()))
					return Double.valueOf(params[1]);
				else return Double.NaN;
			case "OVERLAPD":
			case "OVERLAPDETAIL":
			case "OVERLAP_DETAIL":
			case "NOPREDICTION":
			case "NO_PREDICTION":
			case "NOP":
			case "NPR":
				if(params != null && params.length > 0 && AppUtility.isNumber(params[0].trim()))
					return Double.valueOf(params[0]);
				else return 1.0;
			default:
				if(params != null && params.length > 0 && AppUtility.isNumber(params[0].trim()))
					return Double.valueOf(params[0]);
				else return Double.NaN;	
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Metric)
			return ((Metric) obj).getName().equals(getName());
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
	public abstract MetricResult evaluateAnomalyResults(List<AlgorithmResult> anomalyEvaluations);

	/**
	 * Compares metric results.
	 *
	 * @param currentMetricValue
	 *            the current metric value
	 * @param bestMetricValue
	 *            the best metric value
	 * @return the comparison result
	 */
	public abstract int compareResults(MetricResult currentMetricValue, MetricResult bestMetricValue);

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
	protected abstract String getMetricName();
	
	public String getName(){
		String metName = getMetricName();
		if(metName != null && Double.isFinite(noPredTHR) && !mType.equals(MetricType.NO_PREDICTION)){
			if(metName.length() > 0 && metName.endsWith("("))
				metName = metName.substring(0, metName.length()-1) + "," + noPredTHR + ")";
			else metName = metName + "(" + noPredTHR + ")";
		}
		return metName;
	}

	/**
	 * Gets the metric short name.
	 *
	 * @return the metric short name
	 */
	protected abstract String getMetricShortName();
	
	public String getShortName(){
		String metName = getMetricShortName();
		if(metName != null && Double.isFinite(noPredTHR) && !mType.equals(MetricType.NO_PREDICTION)){
			if(metName.length() > 0 && metName.endsWith("("))
				metName = metName.substring(0, metName.length()-1) + "," + noPredTHR + ")";
			else metName = metName + "(" + noPredTHR + ")";
		}
		return metName;
	}

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
		return o.getName().compareTo(getName());
	}

	public int compareResults(ValueSeries m1, ValueSeries m2) {
		return compareResults(new DoubleMetricResult(m1.getAvg()), new DoubleMetricResult(m2.getAvg()));
	}

}
