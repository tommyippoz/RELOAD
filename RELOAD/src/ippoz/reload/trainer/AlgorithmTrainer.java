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
import ippoz.reload.meta.MetaData;
import ippoz.reload.metric.BetterMaxMetric;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Class AlgorithmTrainer.
 * Base class to train each algorithm. Extends Thread.
 *
 * @author Tommy
 */
public abstract class AlgorithmTrainer extends Thread implements Comparable<AlgorithmTrainer> {
	
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
	private BasicConfiguration bestConf;
	
	/** The metric score. */
	protected ValueSeries metricScore;
	
	/** The metric score. */
	protected ValueSeries trainMetricScore;
	
	/** The metric score. */
	protected ValueSeries anomalyTrainScore;
	
	/** Scores assigned to the training set. */
	protected Map<Knowledge, List<AlgorithmResult>> trainResult;
	
	/** The reputation score. */
	private double reputationScore;
	
	private String datasetName;
	
	protected int kfold;
	
	private long trainingTime;
	
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
	public AlgorithmTrainer(LearnerType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, String datasetName, int kfold, MetaData metaData) {
		this.algTag = algTag;
		this.dataSeries = dataSeries;
		this.metric = metric;
		this.reputation = reputation;
		this.kList = kList;
		this.kfold = kfold;
		this.datasetName = datasetName;
	}
	
	public AlgorithmTrainer(LearnerType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, String datasetName, int kfold) {
		this(algTag, dataSeries, metric, reputation, kList, datasetName, kfold, null);	
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
	public AlgorithmTrainer(LearnerType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, String datasetName) {
		this(algTag, dataSeries, metric, reputation, kList, datasetName, 1);
	}
	
	public String getDatasetName(){
		return datasetName;
	}
	
	public Map<Knowledge, List<AlgorithmResult>> getTrainResult(){
		return trainResult;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		trainingTime = System.currentTimeMillis();
		bestConf = lookForBestConfiguration();
		trainingTime = System.currentTimeMillis() - trainingTime;
		if(metricScore.size() > 0 && trainMetricScore.size() > 0){
			metricScore = evaluateMetricScore(metric);
			//reputationScore = evaluateReputationScore();
			if(getReputationScore() > 0.0)
				bestConf.addItem(BasicConfiguration.WEIGHT, String.valueOf(getReputationScore()));
			else bestConf.addItem(BasicConfiguration.WEIGHT, "1.0");
			bestConf.addItem(BasicConfiguration.AVG_SCORE, String.valueOf(getMetricAvgScore()));
			bestConf.addItem(BasicConfiguration.STD_SCORE, String.valueOf(getMetricStdScore()));
			bestConf.addItem(BasicConfiguration.TRAIN_AVG, trainMetricScore.getAvg());
			bestConf.addItem(BasicConfiguration.TRAIN_STD, trainMetricScore.getStd());
			bestConf.addItem(BasicConfiguration.TRAIN_Q0, trainMetricScore.getMin());
			bestConf.addItem(BasicConfiguration.TRAIN_Q1, trainMetricScore.getQ1());
			bestConf.addItem(BasicConfiguration.TRAIN_Q2, trainMetricScore.getMedian());
			bestConf.addItem(BasicConfiguration.TRAIN_Q3, trainMetricScore.getQ3());
			bestConf.addItem(BasicConfiguration.TRAIN_Q4, trainMetricScore.getMax());
			bestConf.addItem(BasicConfiguration.DATASET_NAME, getDatasetName());
			bestConf.addItem(BasicConfiguration.ANOMALY_AVG, getAnomalyAvg());
			bestConf.addItem(BasicConfiguration.ANOMALY_STD, getAnomalyStd());
			bestConf.addItem(BasicConfiguration.ANOMALY_MED, getAnomalyMed());
		}
	}
	
	public String getDecisionFunctionString(){
		return bestConf.getItem(BasicConfiguration.THRESHOLD);
	}
	
	private double getAnomalyMed() {
		if(anomalyTrainScore != null && anomalyTrainScore.size() > 0)
			return anomalyTrainScore.getMedian();
		else return Double.NaN;
	}

	private double getAnomalyStd() {
		if(anomalyTrainScore != null && anomalyTrainScore.size() > 0)
			return anomalyTrainScore.getStd();
		else return Double.NaN;
	}

	private double getAnomalyAvg() {
		if(anomalyTrainScore != null && anomalyTrainScore.size() > 0)
			return anomalyTrainScore.getAvg();
		else return Double.NaN;
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
	protected abstract BasicConfiguration lookForBestConfiguration();

	/**
	 * Evaluates metric score on a specified set of experiments.
	 *
	 * @return the metric score
	 */
	private ValueSeries evaluateMetricScore(Metric met){
		double[] metricEvaluation = null;
		ValueSeries metricResults = new ValueSeries();
		DetectionAlgorithm algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), dataSeries, bestConf);
		for(Knowledge knowledge : kList){
			metricEvaluation = met.evaluateMetric(algorithm, knowledge);
			metricResults.addValue(metricEvaluation[0]);
		}
		return metricResults;
	}
	
	public String calculateMetrics(Metric[] validationMetrics) {
		String toReturn = "";
		for(Metric met : validationMetrics){
			toReturn = toReturn + met.getMetricShortName() + ":" + evaluateMetricScore(met).getAvg() + ",";
		}
		return toReturn.substring(0, toReturn.length()-1);
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
		return metricScore.getAvg();
	}
	
	/**
	 * Gets the metric score.
	 *
	 * @return the metric score
	 */
	public double getMetricStdScore() {
		return metricScore.getStd();
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
	
}
