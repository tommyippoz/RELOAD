/**
 * 
 */
package ippoz.multilayer.detector.trainer;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.commons.layers.LayerType;
import ippoz.multilayer.detector.algorithm.DetectionAlgorithm;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.knowledge.Knowledge;
import ippoz.multilayer.detector.commons.support.AppUtility;
import ippoz.multilayer.detector.metric.BetterMaxMetric;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.performance.TrainingTiming;
import ippoz.multilayer.detector.reputation.Reputation;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AlgorithmTrainer.
 * Base class to train each algorithm. Extends Thread.
 *
 * @author Tommy
 */
public abstract class AlgorithmTrainer extends Thread implements Comparable<AlgorithmTrainer> {
	
	/** The algorithm tag. */
	private AlgorithmType algTag;	
	
	/** The involved data series. */
	private DataSeries dataSeries;
	
	/** The used metric. */
	private Metric metric;
	
	/** The used reputation metric. */
	private Reputation reputation;
	
	/** The algorithm knowledge. */
	private List<Knowledge> kList;
	
	/** The best configuration. */
	private AlgorithmConfiguration bestConf;
	
	/** The metric score. */
	private double metricScore;
	
	/** The reputation score. */
	private double reputationScore;
	
	/** Flag that indicates if the trained algorithm retrieves different values (e.g., not always true / false). */
	private boolean sameResultFlag;
	
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
	public AlgorithmTrainer(AlgorithmType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList) {
		this.algTag = algTag;
		this.dataSeries = dataSeries;
		this.metric = metric;
		this.reputation = reputation;
		this.kList = kList;
	}
	
	/**
	 * Loads the snapshots of all the training experiments.
	 *
	 * @return the hash map of the snapshots
	 */
	/*private HashMap<String, LinkedList<Snapshot>> loadAlgExpSnapshots() {
		HashMap<String, LinkedList<Snapshot>> expAlgMap = new HashMap<String, LinkedList<Snapshot>>();
		for(ExperimentData expData : expList){
			expAlgMap.put(expData.getName(), expData.buildSnapshotsFor(algTag, dataSeries, bestConf));
		}
		return expAlgMap;
	}*/
	
	/**
	 * Deep clone of the experiment list.
	 *
	 * @param trainData the train data
	 * @return the cloned experiment list
	 */
	/*private List<ExperimentData> deepClone(List<ExperimentData> trainData) {
		List<ExperimentData> list = new ArrayList<ExperimentData>(trainData.size());
		try {
			for(ExperimentData eData : trainData){
				list.add(eData.clone());
			}
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone Experiment");
		}
		return list;
	}*/
	
	/**
	 * Checks if is valid train.
	 *
	 * @return true, if is valid train
	 */
	public boolean isValidTrain(){
		return !sameResultFlag;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		bestConf = lookForBestConfiguration();
		System.out.println("T " + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		metricScore = evaluateMetricScore();
		System.out.println("MS " + (System.currentTimeMillis()-startTime));
		//reputationScore = evaluateReputationScore();
		bestConf.addItem(AlgorithmConfiguration.WEIGHT, String.valueOf(getReputationScore()));
		bestConf.addItem(AlgorithmConfiguration.SCORE, String.valueOf(getMetricScore()));
	}
	
	/**
	 * Look for best configuration.
	 *
	 * @param algExpSnapshots the alg exp snapshots
	 * @param tTiming the t timing
	 * @return the algorithm configuration
	 */
	protected abstract AlgorithmConfiguration lookForBestConfiguration();

	/**
	 * Evaluates metric score on a specified set of experiments.
	 *
	 * @param list the train data
	 * @param algExpSnapshots the alg exp snapshots
	 * @return the metric score
	 */
	private double evaluateMetricScore(){
		double[] metricEvaluation = null;
		List<Double> metricResults = new ArrayList<Double>(kList.size());
		List<Double> algResults = new ArrayList<Double>(kList.size());
		DetectionAlgorithm algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), dataSeries, bestConf);
		for(Knowledge knowledge : kList){
			metricEvaluation = metric.evaluateMetric(algorithm, knowledge);
			metricResults.add(metricEvaluation[0]);
			algResults.add(metricEvaluation[1]);
		}
		sameResultFlag = AppUtility.calcStd(algResults, AppUtility.calcAvg(algResults)) == 0.0;
		return AppUtility.calcAvg(metricResults.toArray(new Double[metricResults.size()]));
	}
	
	/**
	 * Evaluate reputation score on a specified set of experiments.
	 *
	 * @param list the train data
	 * @param algExpSnapshots the alg exp snapshots
	 * @return the reputation score
	 */
	private double evaluateReputationScore(){
		List<Double> reputationResults = new ArrayList<Double>(kList.size());
		DetectionAlgorithm algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), dataSeries, bestConf);
		for(Knowledge knowledge : kList){
			reputationResults.add(reputation.evaluateReputation(algorithm, knowledge));
		}
		return AppUtility.calcAvg(reputationResults.toArray(new Double[reputationResults.size()]));
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
	 * Gets the exp list.
	 *
	 * @return the exp list
	 */
	protected List<Knowledge> getKnowledgeList() {
		return kList;
	}
	
	public int getExpNumber(){
		return kList.size();
	}

	/**
	 * Gets the metric score.
	 *
	 * @return the metric score
	 */
	public double getMetricScore() {
		return metricScore;
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
	public AlgorithmConfiguration getBestConfiguration(){
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
	public AlgorithmType getAlgType(){
		return algTag;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AlgorithmTrainer other) {
		if(metric instanceof BetterMaxMetric)
			return Double.compare(other.getMetricScore(), getMetricScore());
		else return -1*Double.compare(other.getMetricScore(), getMetricScore());
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
