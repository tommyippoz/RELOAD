/**
 * 
 */
package ippoz.reload.trainer;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.meta.MetaData;
import ippoz.reload.metric.BetterMaxMetric;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;

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
		"RIGHT_IQR(1)", "RIGHT_IQR(0.5)", 
		"STATIC_THRESHOLD_GREATERTHAN(0.9)", "STATIC_THRESHOLD_GREATERTHAN(2.9)", "STATIC_THRESHOLD_GREATERTHAN(4.9)"};
	
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
		Pair<Map<Knowledge, List<AlgorithmResult>>, Double> confResults;
		trainingTime = System.currentTimeMillis();
		confResults = lookForBestConfiguration();
		trainingTime = System.currentTimeMillis() - trainingTime;
		if(confResults != null){
			
			valMetricsString = calculateMetrics(validationMetrics, confResults.getKey());
			//printTrainingResults();
			//metricScore = evaluateMetricScore(metric);
			
			//reputationScore = evaluateReputationScore();
			if(getReputationScore() > 0.0)
				bestConf.addItem(BasicConfiguration.WEIGHT, String.valueOf(getReputationScore()));
			else bestConf.addItem(BasicConfiguration.WEIGHT, "1.0");
			bestConf.addItem(BasicConfiguration.AVG_SCORE, confResults.getValue());//String.valueOf(getMetricAvgScore()));
			bestConf.addItem(BasicConfiguration.STD_SCORE, 0);//String.valueOf(getMetricStdScore()));
			bestConf.addItem(BasicConfiguration.DATASET_NAME, getDatasetName());
		}
	}
	
	/*public String printTrainingResults(Metric[] validationMetrics){
		DetectionAlgorithm algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), bestConf);
		if(algorithm instanceof AutomaticTrainingAlgorithm) {
			((AutomaticTrainingAlgorithm)algorithm).automaticTraining(getKnowledgeList().get(0).get("TEST"), true);
		} else {
			for(Knowledge knowledge : getKnowledgeList().get(0).get("TEST")){
				//algorithm.setDecisionFunction(dFunctionString);
				getMetric().evaluateMetric(algorithm, knowledge);
			}
		}
		Map<Knowledge, List<AlgorithmResult>> trainResult = new HashMap<>();
		for(Knowledge know : kList){
			trainResult.put(know, calculateResults(algorithm, know));
		}
		/*trainMetricScore = algorithm.getLoggedScores();
		bestConf.addItem(BasicConfiguration.TRAIN_AVG, trainMetricScore.getAvg());
		bestConf.addItem(BasicConfiguration.TRAIN_STD, trainMetricScore.getStd());
		bestConf.addItem(BasicConfiguration.TRAIN_Q0, trainMetricScore.getMin());
		bestConf.addItem(BasicConfiguration.TRAIN_Q1, trainMetricScore.getQ1());
		bestConf.addItem(BasicConfiguration.TRAIN_Q2, trainMetricScore.getMedian());
		bestConf.addItem(BasicConfiguration.TRAIN_Q3, trainMetricScore.getQ3());
		bestConf.addItem(BasicConfiguration.TRAIN_Q4, trainMetricScore.getMax());
		
		if(validationMetrics != null)
			return calculateMetrics(validationMetrics, trainResult);
		else return null;
	}*/
	
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
	protected abstract Pair<Map<Knowledge, List<AlgorithmResult>>, Double> lookForBestConfiguration();

	/**
	 * Evaluates metric score on a specified set of experiments.
	 *
	 * @return the metric score
	 */
	private ValueSeries evaluateMetricScore(Metric met, Map<Knowledge, List<AlgorithmResult>> trainResults){
		ValueSeries metricResults = new ValueSeries();
		for(Knowledge knowledge : trainResults.keySet()){
			metricResults.addValue(met.evaluateAnomalyResults(trainResults.get(knowledge)));
		}
		return metricResults;
	}
	
	protected String calculateMetrics(Metric[] validationMetrics, Map<Knowledge, List<AlgorithmResult>> trainResults) {
		String toReturn = "";
		if(validationMetrics != null){
			for(Metric met : validationMetrics){
				toReturn = toReturn + met.getMetricShortName() + ":" + evaluateMetricScore(met, trainResults).getAvg() + ",";
			}
			return toReturn.substring(0, toReturn.length()-1);
		} else return "Not Calculated";
	}
	
	protected List<AlgorithmResult> calculateResults(DetectionAlgorithm alg, Knowledge know) {
		double snapValue;
		Knowledge knowledge = know.cloneKnowledge();
		List<AlgorithmResult> anomalyEvaluations = new ArrayList<AlgorithmResult>(knowledge.size());
		for (int i = 0; i < knowledge.size(); i++) {
			AlgorithmResult ar = alg.snapshotAnomalyRate(knowledge, i);
			snapValue = DetectionAlgorithm.convertResultIntoDouble(ar.getScoreEvaluation());
			anomalyEvaluations.add(ar);
			if (knowledge instanceof SlidingKnowledge) {
				((SlidingKnowledge) knowledge).slide(i, snapValue);
			}
		}
		if (knowledge instanceof SlidingKnowledge) {
			((SlidingKnowledge) knowledge).reset();
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

	/**
	 * Gets the exp list, considering the kfold parameter.
	 *
	 * @return the exp list
	 */
	protected List<Map<String, List<Knowledge>>> getKnowledgeList() {
		List<Map<String, List<Knowledge>>> outList = new LinkedList<Map<String, List<Knowledge>>>();
		List<List<Knowledge>> subsets = new ArrayList<List<Knowledge>>(kfold);
		List<Knowledge> partialList;
		Map<String, List<Knowledge>> map;
		if(kfold <= 1 || kfold == Integer.MAX_VALUE || kfold > kList.size()){
			map = new HashMap<String, List<Knowledge>>();
			map.put("TRAIN", kList);
			map.put("TEST", kList);
			outList.add(map);
		} else {
			for(int i=0;i<kList.size();i++){
				if(subsets.size() <= i%kfold || subsets.get(i%kfold) == null)
					subsets.add(i%kfold, new LinkedList<Knowledge>());
				subsets.get(i%kfold).add(kList.get(i));
			}
			for(int k=0;k<kfold;k++){
				map = new HashMap<String, List<Knowledge>>();
				partialList = new LinkedList<Knowledge>();
				for(int i=0;i<kfold;i++){
					if(i==k)
						map.put("TEST", subsets.get(k));
					else partialList.addAll(subsets.get(i));
				}
				map.put("TRAIN", partialList);
				outList.add(map);
			}
		}
		return outList;
	}
	
	public int getExpNumber(){
		return kList.size();
	}

	/**
	 * Gets the metric score.
	 *
	 * @return the metric score
	 */
	public double getMetricAvgScore() {
		return Double.valueOf(bestConf.getItem(BasicConfiguration.AVG_SCORE));
	}
	
	/**
	 * Gets the metric score.
	 *
	 * @return the metric score
	 */
	public double getMetricStdScore() {
		return Double.valueOf(bestConf.getItem(BasicConfiguration.STD_SCORE));
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
	 * Gets the layer.
	 *
	 * @return the layer
	 */
	public LayerType getLayerType(){
		if(dataSeries != null)
			return dataSeries.getLayerType();
		else return null;
	}
	
	/**
	 * Gets the series name.
	 *
	 * @return the series name
	 */
	public DataCategory getDataCategory(){
		if(dataSeries != null)
			return dataSeries.getDataCategory();
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
			return Double.compare(other.getMetricAvgScore(), getMetricAvgScore());
		else return -1*Double.compare(other.getMetricAvgScore(), getMetricAvgScore());
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
	
	protected Pair<String, Double> electBestDecisionFunction(DetectionAlgorithm algorithm, List<AlgorithmResult> resultList, ValueSeries vs){
		double bestScore = Double.NaN;
		String bestFunction = null;
		if(resultList != null){
			for(String decFunctString : DECISION_FUNCTIONS){
				if(DecisionFunction.isApplicableTo(getAlgType(), decFunctString)){
					DecisionFunction df = algorithm.setDecisionFunction(decFunctString);
					if(df != null){
						List<AlgorithmResult> updatedList = updateResultWithDecision(df, resultList);
						double val = getMetric().evaluateAnomalyResults(updatedList);
						if(!Double.isFinite(bestScore) || getMetric().compareResults(val, bestScore) > 0){
							bestScore = val;
							bestFunction = decFunctString;
						}
					}
				}
			}
			
		}
		return new Pair<String, Double>(bestFunction, bestScore);
	}
	
	protected static List<AlgorithmResult> updateResultWithDecision(DecisionFunction dFunction, List<AlgorithmResult> oldList){
		List<AlgorithmResult> newList = new LinkedList<AlgorithmResult>();
		for(AlgorithmResult ar : oldList){
			AnomalyResult anr = dFunction.classify(ar);
			newList.add(new AlgorithmResult(ar.hasInjection(), DetectionAlgorithm.convertResultIntoDouble(anr), anr, dFunction, ar.getConfidence()));
		}
		return newList;
	}

	public String getMetricsString() {
		return valMetricsString;
	}
	
}
