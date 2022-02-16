/**
 * 
 */
package ippoz.reload.trainer;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.meta.MetaData;
import ippoz.reload.metric.BetterMaxMetric;
import ippoz.reload.metric.ConfusionMatrix;
import ippoz.reload.metric.Metric;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;
import ippoz.reload.metric.result.MetricResultSeries;
import ippoz.reload.reputation.Reputation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class AlgorithmTrainer.
 * Base class to train each algorithm. Extends Thread.
 *
 * @author Tommy
 */
public abstract class AlgorithmTrainer extends Thread implements Comparable<AlgorithmTrainer> {
	
	private static String[] DECISION_FUNCTIONS = {
		"MODE(3)", "MODE(0.5)", "MODE(0.2)", "MODE(0.05)", 
		"MEDIAN(0.5)", "MEDIAN(0.2)", "MEDIAN(0.05)", 
		"MEDIAN_INTERVAL(0.1)", "MEDIAN_INTERVAL(0.05)", "MEDIAN_INTERVAL(0)", 
		"MODE_INTERVAL(0.1)", "MODE_INTERVAL(0.05)", "MODE_INTERVAL(0)", 
		"IQR", "IQR(1)", "IQR(0.5)", "IQR(0.2)", "IQR(0)", 
		"CONFIDENCE_INTERVAL","CONFIDENCE_INTERVAL(1)", "CONFIDENCE_INTERVAL(0.5)", "CONFIDENCE_INTERVAL(0.2)", 
		"LEFT_POSITIVE_IQR", "LEFT_POSITIVE_IQR(0)", "LEFT_IQR(1)", "LEFT_IQR(0.5)", 
		"RIGHT_IQR(1)", "RIGHT_IQR(0.5)"};
	
	/** The algorithm tag. */
	private LearnerType algTag;	
	
	/** The involved data series. */
	private DataSeries dataSeries;
	
	/** The used metric. */
	private Metric metric;
	
	/** The used reputation metric. */
	private Reputation reputation;
	
	/** The algorithm knowledge. */
	protected List<Knowledge> kList;
	
	/** The best configuration. */
	protected BasicConfiguration bestConf;
	
	/** The reputation score. */
	private double reputationScore;
	
	private String datasetName;
	
	protected int kfold;
	
	private long trainingTime;
	
	private Metric[] validationMetrics;
	
	private String valMetricsString;
	
	/**
	 * Instantiates a new algorithm trainer.
	 *
	 * @param algTag the algorithm tag
	 * @param dataSeries the data series
	 * @param metric the used metric
	 * @param reputation the used reputation metric
	 * @param tTiming the t timing
	 * @param kList the considered train data
	 */
	public AlgorithmTrainer(LearnerType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, String datasetName, int kfold, MetaData metaData, Metric[] validationMetrics) {
		this.algTag = algTag;
		this.dataSeries = dataSeries;
		this.metric = metric;
		this.reputation = reputation;
		this.kList = kList;
		this.kfold = kfold;
		this.datasetName = datasetName;
		this.validationMetrics = validationMetrics;
	}
	
	public AlgorithmTrainer(LearnerType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, String datasetName, int kfold, Metric[] validationMetrics) {
		this(algTag, dataSeries, metric, reputation, kList, datasetName, kfold, null, validationMetrics);	
	}
	
	/**
	 * Instantiates a new algorithm trainer.
	 *
	 * @param algTag the algorithm tag
	 * @param dataSeries the data series
	 * @param metric the used metric
	 * @param reputation the used reputation metric
	 * @param tTiming the t timing
	 * @param kList the considered train data
	 * @param kfold2 
	 * @param datasetName 
	 */
	public AlgorithmTrainer(LearnerType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, String datasetName, Metric[] validationMetrics) {
		this(algTag, dataSeries, metric, reputation, kList, datasetName, 1, validationMetrics);
	}
	
	public String getDatasetName(){
		return datasetName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		ObjectPair<Map<Knowledge, List<AlgorithmResult>>, MetricResult> confResults;
		trainingTime = System.currentTimeMillis();
		confResults = lookForBestConfiguration();
		trainingTime = System.currentTimeMillis() - trainingTime;
		if(confResults != null && bestConf != null){
			valMetricsString = calculateMetrics(validationMetrics, confResults.getKey());
			if(getReputationScore() > 0.0)
				bestConf.addItem(BasicConfiguration.WEIGHT, String.valueOf(getReputationScore()));
			else bestConf.addItem(BasicConfiguration.WEIGHT, "1.0");
			bestConf.addItem(BasicConfiguration.AVG_SCORE, confResults.getValue().toString());//String.valueOf(getMetricAvgScore()));
			bestConf.addItem(BasicConfiguration.STD_SCORE, 0);//String.valueOf(getMetricStdScore()));
			bestConf.addItem(BasicConfiguration.DATASET_NAME, getDatasetName());
		}
	}
	
	public String getDecisionFunctionString(){
		return bestConf.getItem(BasicConfiguration.THRESHOLD);
	}

	public long getTrainingTime() {
		return trainingTime;
	}
	
	/**
	 * Look for best configuration.
	 *
	 * @param algExpSnapshots the alg exp snapshots
	 * @param tTiming the t timing
	 * @return the algorithm configuration
	 */
	protected abstract ObjectPair<Map<Knowledge, List<AlgorithmResult>>, MetricResult> lookForBestConfiguration();

	/**
	 * Evaluates metric score on a specified set of experiments.
	 *
	 * @return the metric score
	 */
	private MetricResultSeries evaluateMetricScore(Metric met, Map<Knowledge, List<AlgorithmResult>> trainResults){
		MetricResultSeries metricResults = new MetricResultSeries();
		for(Knowledge knowledge : trainResults.keySet()){
			metricResults.addValue(met.evaluateAnomalyResults(trainResults.get(knowledge), new ConfusionMatrix(trainResults.get(knowledge))));
		}
		return metricResults;
	}
	
	protected String calculateMetrics(Metric[] validationMetrics, Map<Knowledge, List<AlgorithmResult>> trainResults) {
		String toReturn = "";
		if(validationMetrics != null){
			for(Metric met : validationMetrics){
				toReturn = toReturn + met.getShortName() + ":" + evaluateMetricScore(met, trainResults).getAvg() + ",";
			}
			return toReturn.substring(0, toReturn.length()-1);
		} else return "Not Calculated";
	}
	
	protected List<AlgorithmResult> calculateResults(DetectionAlgorithm alg, Knowledge know) {
		Knowledge knowledge = know.cloneKnowledge();
		List<AlgorithmResult> anomalyEvaluations = new ArrayList<>(knowledge.size());
		for (int i = 0; i < knowledge.size(); i++) {
			anomalyEvaluations.add(alg.snapshotAnomalyRate(knowledge, i));
		}
		return anomalyEvaluations;
	}

	/**
	 * Gets the data series.
	 *
	 * @return the data series
	 */
	public DataSeries getDataSeries() {
		return dataSeries;
	}

	/**
	 * Gets the metric.
	 *
	 * @return the metric
	 */
	public Metric getMetric() {
		return metric;
	}

	/**
	 * Gets the reputation.
	 *
	 * @return the reputation
	 */
	public Reputation getReputation() {
		return reputation;
	}
	
	public int getExpNumber(){
		return kList.size();
	}

	/**
	 * Gets the metric score.
	 *
	 * @return the metric score
	 */
	public MetricResult getMetricAvgScore() {
		if(bestConf != null)
			return MetricResult.valueOf(bestConf.getItem(BasicConfiguration.AVG_SCORE));
		else return new DoubleMetricResult(Double.NaN);
	}
	
	/**
	 * Gets the metric score.
	 *
	 * @return the metric score
	 */
	public double getMetricStdScore() {
		if(bestConf != null)
			return Double.valueOf(bestConf.getItem(BasicConfiguration.STD_SCORE));
		else return Double.NaN;
	}
	
	/**
	 * Gets the reputation score.
	 *
	 * @return the reputation score
	 */
	public double getReputationScore() {
		return reputationScore;
	}
	
	/**
	 * Gets the best configuration.
	 *
	 * @return the best configuration
	 */
	public BasicConfiguration getBestConfiguration(){
		return bestConf;
	}
	
	/**
	 * Gets the series name.
	 *
	 * @return the series name
	 */
	public String getSeriesName(){
		if(dataSeries != null)
			return dataSeries.getName();
		else return null;
	}

	/**
	 * Gets the algorithm type.
	 *
	 * @return the algorithm type
	 */
	public LearnerType getAlgType(){
		return algTag;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AlgorithmTrainer other) {
		if(metric instanceof BetterMaxMetric)
			return getMetricAvgScore().compareTo(other.getMetricAvgScore());
		else return -1*getMetricAvgScore().compareTo(other.getMetricAvgScore());
	}

	/**
	 * Gets the series description.
	 *
	 * @return the series description
	 */
	public String getSeriesDescription() {
		if(dataSeries != null)
			return dataSeries.toString();
		else return "Default";
	}
	
	public void flush(){
		kList = null;
	}
	
	protected ObjectPair<String, MetricResult> electBestDecisionFunction(DetectionAlgorithm algorithm, List<AlgorithmResult> resultList, ValueSeries vs){
		MetricResult bestScore = null;
		String bestFunction = null;
		if(resultList != null && vs != null && vs.size() > 0){
			for(String decFunctString : DECISION_FUNCTIONS){
				if(DecisionFunction.isApplicableTo(getAlgType(), decFunctString)){
					DecisionFunction df = algorithm.setDecisionFunction(decFunctString);
					if(df != null){
						List<AlgorithmResult> updatedList = updateResultWithDecision(df, resultList);
						MetricResult val = getMetric().evaluateAnomalyResults(updatedList, new ConfusionMatrix(updatedList));
						if(bestScore == null || getMetric().compareResults(val, bestScore) > 0){
							bestScore = val;
							bestFunction = decFunctString;
						}
					}
				}
			}
			
		}
		return new ObjectPair<String, MetricResult>(bestFunction, bestScore);
	}
	
	protected static List<AlgorithmResult> updateResultWithDecision(DecisionFunction dFunction, List<AlgorithmResult> oldList){
		List<AlgorithmResult> newList = new ArrayList<>(oldList.size());
		for(AlgorithmResult ar : oldList){
			AnomalyResult anr = dFunction.classify(ar);
			newList.add(new AlgorithmResult(ar.isAnomalous(), DetectionAlgorithm.convertResultIntoDouble(anr), anr, ar.getConfidence()));
		}
		return newList;
	}

	public String getMetricsString() {
		return valMetricsString;
	}
	
	public abstract void saveAlgorithmScores();
	
}
