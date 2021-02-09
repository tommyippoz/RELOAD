/**
 * 
 */
package ippoz.reload.manager.train;

import ippoz.reload.algorithm.AlgorithmComplexity;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.info.TrainInfo;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;
import ippoz.reload.trainer.AlgorithmTrainer;
import ippoz.reload.trainer.ConfigurationSelectorTrainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Class TrainerManager.
 * The manager responsible of the training process of the anomaly detector.
 *
 * @author Tommy
 */
public class TrainerManager extends TrainDataManager {
	
	private TrainInfo trainInfo;
	
	private Metric[] validationMetrics;
	
	private boolean allowParallel;
	
	/**
	 * Instantiates a new trainer manager.
	 *
	 * @param prefManager the preference manager
	 * @param pManager the timing manager
	 * @param map the experiment list
	 * @param confList the configuration list
	 * @param metric the chosen metric
	 * @param reputation the chosen reputation metric
	 * @param algTypes the algorithm types
	 */
	private TrainerManager(String setupFolder, String scoresFolder, String datasetName, String outputFolder, Map<KnowledgeType, List<Knowledge>> map, List<BasicConfiguration> confList, Metric metric, Reputation reputation, LearnerType algTypes, int kfold, Metric[] validationMetrics, boolean allowParallel) {
		super(map, setupFolder, scoresFolder, datasetName, confList, metric, reputation, algTypes, kfold);
		this.validationMetrics = validationMetrics;
		this.allowParallel = allowParallel;
		clearTmpFolders();
	}
	
	/**
	 * Instantiates a new trainer manager.
	 *
	 * @param prefManager the preference manager
	 * @param pManager the timing manager
	 * @param expList the experiment list
	 * @param confList the configuration list
	 * @param metric the chosen metric
	 * @param reputation the chosen reputation metric
	 * @param dataTypes the data types
	 * @param algTypes the algorithm types
	 */
	public TrainerManager(String setupFolder, String scoresFolder, String datasetName, String outputFolder, Map<KnowledgeType, List<Knowledge>> expList, List<BasicConfiguration> confList, Metric metric, Reputation reputation, LearnerType algTypes, DataSeries selectedSeries, int kfold, Metric[] validationMetrics, boolean allowParallel) {
		super(expList, setupFolder, scoresFolder, datasetName, confList, metric, reputation, algTypes, selectedSeries, kfold);
		this.validationMetrics = validationMetrics;
		this.allowParallel = allowParallel;
		clearTmpFolders();
	}
	
	/**
	 * Instantiates a new trainer manager.
	 *
	 * @param prefManager the preference manager
	 * @param pManager the timing manager
	 * @param map the experiment list
	 * @param confList the configuration list
	 * @param metric the chosen metric
	 * @param reputation the chosen reputation metric
	 * @param dataTypes the data types
	 * @param algTypes the algorithm types
	 */
	public TrainerManager(String setupFolder, String scoresFolder, String datasetName, String outputFolder, Map<KnowledgeType, List<Knowledge>> map, List<BasicConfiguration> confList, Metric metric, Reputation reputation, LearnerType algTypes, String[] selectedSeriesString, int kfold, Metric[] validationMetrics, boolean allowParallel) {
		this(setupFolder, scoresFolder, datasetName, outputFolder, map, confList, metric, reputation, algTypes, kfold, validationMetrics, allowParallel);
		dataSeries = parseSelectedSeries(selectedSeriesString);
		AppLogger.logInfo(getClass(), dataSeries.size() + " Data Series Loaded");
	}
	
	private void clearTmpFolders() {
		File rootFolder = new File(new File(".").getAbsolutePath().substring(0, new File(".").getAbsolutePath().length()-2));
		for(File file : rootFolder.listFiles()){
			if(file.isDirectory() && file.getName().endsWith("_tmp_RELOAD")){
				if(file.delete())
					AppLogger.logInfo(getClass(), "Clearing temporary folder '" + file.getPath() + "'");
				else AppLogger.logInfo(getClass(), "Failed to clean folder '" + file.getPath() + "'");
			}
		}
	}
	
	private DataSeries parseSelectedSeries(String[] selectedSeriesString) {
		List<DataSeries> selected = DataSeries.fromString(selectedSeriesString, false);
		AppLogger.logInfo(getClass(), "Selected Features Loaded: " + selected.size());
		return new DataSeries(selected);
	}

	/**
	 * Starts the train process. 
	 * The scores are saved in a file specified in the preferences.
	 * @param metaFile 
	 */
	@SuppressWarnings("unchecked")
	public void train(String outFolder){
		long start = System.currentTimeMillis();
		try {
			if(trainInfo == null)
				trainInfo = new TrainInfo();
			trainInfo.setFaultRatio(getInjectionsRatio());
			trainInfo.setSeries(dataSeries);
			trainInfo.setKFold(kfold);
			trainInfo.setAlgorithm(algTypes);
			if(dataSeries != null && dataSeries.size() > 0){
				if(isValidKnowledge()){
					start();
					join();
					Collections.sort((List<AlgorithmTrainer>)getThreadList()); 
					trainInfo.setTrainingTime(System.currentTimeMillis() - start);
					AppLogger.logInfo(getClass(), "Training executed in " + trainInfo.getTrainTime() + "ms");
					AlgorithmTrainer at = bestModel(getThreadList());
					trainInfo.setMetricsString(at.getMetricsString());
					AppLogger.logInfo(getClass(), "Found: " + (at.getBestConfiguration() != null ? at.getBestConfiguration().toString() : "null") + 
							" Score: <" + at.getMetricAvgScore() + ", " + at.getMetricStdScore() + ">");
					if(!new File(outFolder).exists())
						new File(outFolder).mkdirs();
					saveModels(getThreadList(), outFolder + File.separatorChar + "scores.csv");
					//saveTrainScores((List<AlgorithmTrainer>)getThreadList(), metaFile);
					//saveThresholdRelevance(getThreadList(), outFolder + File.separatorChar + "thresholdrelevance.csv");
					AppLogger.logInfo(getClass(), "Training scores saved");
					trainInfo.printFile(new File(outFolder + File.separatorChar + "trainInfo.info"));
				} else AppLogger.logError(getClass(), "NoSuchDataError", "Unable to fetch train data");
			} else AppLogger.logError(getClass(), "NoDataSeriesError", "Unable to find valid dataeries: try running Feature Selection again");
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to complete training phase");
		}
	}

	private AlgorithmTrainer bestModel(List<? extends Thread> threadList) {
		AlgorithmTrainer best = null;
		for(Thread thr : threadList){
			if(thr instanceof AlgorithmTrainer){
				AlgorithmTrainer current = (AlgorithmTrainer)thr;
				if(best == null || getMetric().compareResults(current.getMetricAvgScore(), best.getMetricAvgScore()) > 0)
					best = current;
			}
		}
		return best;
	}

	/*private void saveThresholdRelevance(List<? extends Thread> list, String filename) {
		BufferedWriter thresholdRelevanceWriter;
		BasicConfiguration conf;
		AlgorithmTrainer trainer;
		Map<String,Integer> statMap = new HashMap<String, Integer>();
		try {
			for(Thread tThread : list){
				trainer = (AlgorithmTrainer)tThread;
				conf = trainer.getBestConfiguration();
				if(conf != null && conf.hasItem(BasicConfiguration.THRESHOLD)) {
					if(statMap.containsKey(conf.getItem(BasicConfiguration.THRESHOLD))){
						statMap.put(conf.getItem(BasicConfiguration.THRESHOLD), statMap.get(conf.getItem(BasicConfiguration.THRESHOLD)) + 1);
					} else statMap.put(conf.getItem(BasicConfiguration.THRESHOLD), 1);
				}		
			}
			if(statMap.size() > 0) {
				statMap = sortByComparator(statMap, false);
				thresholdRelevanceWriter = new BufferedWriter(new FileWriter(new File(filename)));
				thresholdRelevanceWriter.write("threshold,optimal_configurations\n");
				for(String thresholdKey : statMap.keySet()){
					thresholdRelevanceWriter.write(thresholdKey + "," + statMap.get(thresholdKey) + "\n");
				}
				thresholdRelevanceWriter.close();	
			}
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write scores");
		}
	}
	
	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, boolean ascending)
    {

        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>()
        {
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                if (ascending) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }*/

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#initRun()
	 */
	@Override
	protected void initRun(){
		List<AlgorithmTrainer> trainerList = new LinkedList<AlgorithmTrainer>();
		AppLogger.logInfo(getClass(), "Initializing Train...");
		KnowledgeType kType = DetectionAlgorithm.getKnowledgeType(algTypes);
		if(confList == null || confList.size() == 0){
			AppLogger.logError(getClass(), "UnrecognizedConfiguration", algTypes + " does not have an associated configuration: basic will be applied");
			confList = new LinkedList<BasicConfiguration>();
			confList.add(BasicConfiguration.buildConfiguration(algTypes));
		}
		if(confList != null){
			if(allowParallel && algTypes instanceof BaseLearner && DetectionAlgorithm.getMemoryComplexity(((BaseLearner)algTypes).getAlgType()) == AlgorithmComplexity.LINEAR){
				int step = (int)Math.ceil(1.0*confList.size() / getLoadFactor());
				for(int i=0;i<confList.size();i=i+step){
					List<BasicConfiguration> subList = confList.subList(i, Math.min(i+step, confList.size()));
					trainerList.add(new ConfigurationSelectorTrainer(algTypes, dataSeries, getMetric(), getReputation(), getKnowledge(kType), subList, getDatasetName(), kfold, validationMetrics));
				}
			} else trainerList.add(new ConfigurationSelectorTrainer(algTypes, dataSeries, getMetric(), getReputation(), getKnowledge(kType), confList, getDatasetName(), kfold, validationMetrics));
		}
		setThreadList(trainerList);
		AppLogger.logInfo(getClass(), "Train of '" + algTypes.toString() + "' is Starting at " + new Date());
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#threadStart(java.lang.Thread, int)
	 */
	@Override
	protected void threadStart(Thread t, int tIndex) {
		// TODO
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#threadComplete(java.lang.Thread, int)
	 */
	@Override
	protected void threadComplete(Thread t, int tIndex) {
		//AlgorithmTrainer at = ((AlgorithmTrainer)t);
		/*AppLogger.logInfo(getClass(), "[" + tIndex + "/" + threadNumber() + "] Found: " + 
				(at.getBestConfiguration() != null ? at.getBestConfiguration().toString() : "null") + 
				" Score: <" + at.getMetricAvgScore() + ", " + at.getMetricStdScore() + ">");	*/	
		//at.flush();
	}
	
	/**
	 * Saves scores related to the executed AlgorithmTrainers.
	 *
	 * @param list the list of algorithm trainers
	 * @param metaFile 
	 *//*
	private void saveTrainScores(List<AlgorithmTrainer> list, String metaFile) {
		List<DataSeries> uniqueDs;
		BufferedWriter scoreWriter;
		File metaDatasetsFolder = new File(new File(datasetsFolder).getAbsoluteFile().getPath() + File.separatorChar + "meta");
		try {
			if(!metaDatasetsFolder.exists())
				metaDatasetsFolder.mkdirs();
			scoreWriter = new BufferedWriter(new FileWriter(new File(new File(datasetsFolder).getAbsoluteFile().getPath() + File.separatorChar + metaFile)));
			scoreWriter.write("*This file contains the scores each model obtained during training. Used Algorithms: \n");
			if(seriesList != null && seriesList.size() > 0 && list != null && list.size() > 0){
				for(AlgorithmTrainer trainer : list){
					scoreWriter.write("* " + trainer.getAlgType() + "(" + trainer.getDataSeries().getName().replace("@", "-") + ") - Decision: " + trainer.getDecisionFunctionString() + "\n");
				}
				uniqueDs = DataSeries.uniqueCombinations(seriesList);
				for(DataSeries ds : uniqueDs){
					scoreWriter.write(ds.getName().replace("@", "-") + ",");
				}
				for(AlgorithmTrainer trainer : list){
					scoreWriter.write(trainer.getAlgType() + "(" + trainer.getDataSeries().getCompactName().replace("@", "-") + "),");
				}
				scoreWriter.write("label\n");
				for(Knowledge know : getKnowledge()){
					for(int i=0;i<know.size();i++){
						for(DataSeries ds : uniqueDs){
							scoreWriter.write(know.getDataSeriesValue(ds, i).getFirst() + ",");
						}					
						for(AlgorithmTrainer trainer : list){
							AlgorithmResult ar = trainer.getTrainResult().get(know).get(i);
							scoreWriter.write(ar.getScore() + ",");
						}
						scoreWriter.write(know.getInjection(i) != null ? "Attack" : "Normal");
						scoreWriter.write("\n");
					}
				}
			}
			// TODO
			scoreWriter.close();			
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write train scores");
		}
	}*/
	
	/**
	 * Saves scores related to the executed AlgorithmTrainers.
	 *
	 * @param list the list of algorithm trainers
	 */
	private void saveModels(List<? extends Thread> list, String filename) {
		BufferedWriter scoreWriter;
		AlgorithmTrainer bestTrainer = null;
		try {
			for(Thread tThread : list){
				AlgorithmTrainer trainer = (AlgorithmTrainer)tThread;
				if(bestTrainer == null || bestTrainer.getMetricAvgScore().compareTo(trainer.getMetricAvgScore()) < 0)
					bestTrainer = trainer;
			}
			scoreWriter = new BufferedWriter(new FileWriter(new File(filename)));
			scoreWriter.write("*This file contains the details and the scores of each individual anomaly checker that was evaluated during training. \n");
			scoreWriter.write("data_series,algorithm_type,reputation_score,avg_metric_score(" + getMetric().getMetricName() + "),std_metric_score(" + getMetric().getMetricName() + "),dataset,configuration\n");
			if(bestTrainer != null && bestTrainer.getBestConfiguration() != null) {
				bestTrainer.saveAlgorithmScores();
				scoreWriter.write(bestTrainer.getSeriesDescription() + "§" + 
						bestTrainer.getAlgType() + "§" +
						bestTrainer.getReputationScore() + "§" + 
						bestTrainer.getMetricAvgScore() + "§" +  
						bestTrainer.getMetricStdScore() + "§" + 
						bestTrainer.getDatasetName() + "§" +
						bestTrainer.getBestConfiguration().toFileRow(false) + "\n");		
			}
			scoreWriter.close();			
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write scores");
		}
	}

	public TrainInfo getTrainInfo() {
		return trainInfo;
	}

	public void addLoaderInfo(Loader loader) {
		if(trainInfo == null)
			trainInfo = new TrainInfo();
		trainInfo.setRuns(loader.getRuns());
		trainInfo.setDataPoints(loader.getDataPoints());
	}
	
}
