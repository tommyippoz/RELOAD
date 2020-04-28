/**
 * 
 */
package ippoz.reload.manager.train;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.datacategory.DataCategory;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class TrainerManager.
 * The manager responsible of the training process of the anomaly detector.
 *
 * @author Tommy
 */
public class TrainerManager extends TrainDataManager {
	
	private TrainInfo trainInfo;
	
	private String datasetsFolder;
	
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
	private TrainerManager(String setupFolder, String dsDomain, String scoresFolder, String datasetName, String outputFolder, Map<KnowledgeType, List<Knowledge>> map, List<BasicConfiguration> confList, Metric metric, Reputation reputation, LearnerType algTypes, int kfold) {
		super(map, setupFolder, dsDomain, scoresFolder, datasetName, confList, metric, reputation, algTypes, kfold);
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
	public TrainerManager(String setupFolder, String dsDomain, String scoresFolder, String datasetName, String outputFolder, Map<KnowledgeType, List<Knowledge>> expList, List<BasicConfiguration> confList, Metric metric, Reputation reputation, LearnerType algTypes, List<DataSeries> selectedSeries, int kfold) {
		super(expList, setupFolder, dsDomain, scoresFolder, datasetName, confList, metric, reputation, algTypes, selectedSeries, kfold);
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
	public TrainerManager(String setupFolder, String datasetsFolder, String dsDomain, String scoresFolder, String datasetName, String outputFolder, Map<KnowledgeType, List<Knowledge>> map, List<BasicConfiguration> confList, Metric metric, Reputation reputation, DataCategory[] dataTypes, LearnerType algTypes, String[] selectedSeriesString, int kfold) {
		this(setupFolder, dsDomain, scoresFolder, datasetName, outputFolder, map, confList, metric, reputation, algTypes, kfold);
		this.datasetsFolder = datasetsFolder;
		seriesList = parseSelectedSeries(selectedSeriesString, dataTypes, dsDomain);
		AppLogger.logInfo(getClass(), seriesList.size() + " Data Series Loaded");
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
	
	private List<DataSeries> parseSelectedSeries(String[] selectedSeriesString, DataCategory[] dataTypes, String dsDomain) {
		List<DataSeries> selected = DataSeries.fromString(selectedSeriesString, false);
		AppLogger.logInfo(getClass(), "Selected Data Series Loaded: " + selected.size());
		List<DataSeries> finalList = selected;
		List<DataSeries> combined = new LinkedList<DataSeries>();
		if(dsDomain.equals("ALL")){
			combined = DataSeries.allCombinations(selected);
			finalList.addAll(combined);
		} else if(dsDomain.equals("UNION")){
			combined = DataSeries.unionCombinations(selected);
			finalList = combined;
		} else if(dsDomain.equals("MULTIPLE_UNION")){
			combined = DataSeries.multipleUnionCombinations(selected);
			finalList = combined;
		} else if(dsDomain.equals("SIMPLE")){
			combined = DataSeries.unionCombinations(selected);
			finalList.addAll(combined);
		} else if(dsDomain.contains("PEARSON") && dsDomain.contains("(") && dsDomain.contains(")")){
			double pearsonSimple = Double.valueOf(dsDomain.substring(dsDomain.indexOf("(")+1, dsDomain.indexOf(")")));
			combined = DataSeries.pearsonCombinations(getKnowledge(), pearsonSimple, setupFolder, selected);
			finalList.addAll(combined);
		}
		AppLogger.logInfo(getClass(), "Combined Data Series Created: " + combined.size());
		AppLogger.logInfo(getClass(), "Finalized Data Series (" + dsDomain + "): " + finalList.size());
		return finalList;
	}

	/**
	 * Starts the train process. 
	 * The scores are saved in a file specified in the preferences.
	 * @param metaFile 
	 */
	@SuppressWarnings("unchecked")
	public void train(String outFilename, String metaFile){
		long start = System.currentTimeMillis();
		try {
			if(trainInfo == null)
				trainInfo = new TrainInfo();
			trainInfo.setFaultRatio(getInjectionsRatio());
			trainInfo.setSeries(seriesList);
			trainInfo.setKFold(kfold);
			trainInfo.setAlgorithm(algTypes);
			if(isValidKnowledge()){
				start();
				join();
				Collections.sort((List<AlgorithmTrainer>)getThreadList()); 
				trainInfo.setTrainingTime(System.currentTimeMillis() - start);
				AppLogger.logInfo(getClass(), "Training executed in " + trainInfo.getTrainTime() + "ms");
				saveModels(getThreadList(), outFilename + "_scores.csv");
				saveTrainScores((List<AlgorithmTrainer>)getThreadList(), metaFile);
				saveThresholdRelevance(getThreadList(), outFilename + "_thresholdrelevance.csv");
				AppLogger.logInfo(getClass(), "Training scores saved");
				trainInfo.printFile(new File(outFilename + "_trainInfo.info"));
			} else AppLogger.logError(getClass(), "NoSuchDataError", "Unable to fetch train data");
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to complete training phase");
		}
	}

	private void saveThresholdRelevance(List<? extends Thread> list, String filename) {
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
    }

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#initRun()
	 */
	@Override
	protected void initRun(){
		List<AlgorithmTrainer> trainerList = new LinkedList<AlgorithmTrainer>();
		AppLogger.logInfo(getClass(), "Initializing Train...");
		if(confList == null || confList.size() == 0){
			AppLogger.logError(getClass(), "UnrecognizedConfiguration", algTypes + " does not have an associated configuration: basic will be applied");
			confList = new LinkedList<BasicConfiguration>();
			confList.add(BasicConfiguration.buildConfiguration(algTypes));
		}
		KnowledgeType kType = DetectionAlgorithm.getKnowledgeType(algTypes);
		for(DataSeries dataSeries : seriesList){
			trainerList.add(new ConfigurationSelectorTrainer(algTypes, dataSeries, getMetric(), getReputation(), getKnowledge(kType), confList, getDatasetName(), kfold));
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
		AlgorithmTrainer at = ((AlgorithmTrainer)t);
		AppLogger.logInfo(getClass(), "[" + tIndex + "/" + threadNumber() + "] Found: " + 
				(at.getBestConfiguration() != null ? at.getBestConfiguration().toString() : "null") + 
				" Score: <" + at.getMetricAvgScore() + ", " + at.getMetricStdScore() + ">");		
		at.flush();
	}
	
	/**
	 * Saves scores related to the executed AlgorithmTrainers.
	 *
	 * @param list the list of algorithm trainers
	 * @param metaFile 
	 */
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
	}
	
	/**
	 * Saves scores related to the executed AlgorithmTrainers.
	 *
	 * @param list the list of algorithm trainers
	 */
	public static void saveModels(List<? extends Thread> list, String filename, Metric metric) {
		BufferedWriter scoreWriter;
		AlgorithmTrainer trainer;
		try {
			scoreWriter = new BufferedWriter(new FileWriter(new File(filename)));
			scoreWriter.write("*This file contains the details and the scores of each individual anomaly checker that was evaluated during training. \n");
			scoreWriter.write("data_series,algorithm_type,reputation_score,avg_metric_score(" + metric.getMetricName() + "),std_metric_score(" + metric.getMetricName() + "),dataset,configuration\n");
			for(Thread tThread : list){
				trainer = (AlgorithmTrainer)tThread;
				if(trainer.getBestConfiguration() != null) {
					scoreWriter.write(trainer.getSeriesDescription() + "," + 
							trainer.getAlgType().toString() + "," +
							trainer.getReputationScore() + "," + 
							trainer.getMetricAvgScore() + "," +  
							trainer.getMetricStdScore() + "," + 
							trainer.getDatasetName() + "," +
							trainer.getBestConfiguration().toFileRow(false) + "\n");
				}			
			}
			scoreWriter.close();			
		} catch(IOException ex){
			AppLogger.logException(TrainerManager.class, ex, "Unable to write scores");
		}
	}
	
	/**
	 * Saves scores related to the executed AlgorithmTrainers.
	 *
	 * @param list the list of algorithm trainers
	 */
	private void saveModels(List<? extends Thread> list, String filename) {
		BufferedWriter scoreWriter, statWriter;
		AlgorithmTrainer trainer;
		int count = 0;
		Double[] statMap = new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		try {
			scoreWriter = new BufferedWriter(new FileWriter(new File(filename)));
			scoreWriter.write("*This file contains the details and the scores of each individual anomaly checker that was evaluated during training. \n");
			scoreWriter.write("data_series,algorithm_type,reputation_score,avg_metric_score(" + getMetric().getMetricName() + "),std_metric_score(" + getMetric().getMetricName() + "),dataset,configuration\n");
			for(Thread tThread : list){
				trainer = (AlgorithmTrainer)tThread;
				count++;
				if(trainer.getBestConfiguration() != null) {
					scoreWriter.write(trainer.getSeriesDescription() + "§" + 
							trainer.getAlgType() + "§" +
							trainer.getReputationScore() + "§" + 
							trainer.getMetricAvgScore() + "§" +  
							trainer.getMetricStdScore() + "§" + 
							trainer.getDatasetName() + "§" +
							trainer.getBestConfiguration().toFileRow(false) + "\n");
					statMap[0] += 1.0;
					statMap[1] += trainer.getTrainingTime();
					statMap[2] += trainer.getMetricAvgScore();
					if(count <= 10)
						statMap[3] += 1.0;
					if(count <= 50)
						statMap[4] += 1.0;
					if(count <= 100)
						statMap[5] += 1.0;
					if(count <= 10)
						statMap[6] += trainer.getMetricAvgScore();
					if(count <= 50)
						statMap[7] += trainer.getMetricAvgScore();
					if(count <= 100)
						statMap[8] += trainer.getMetricAvgScore();
				}			
			}
			scoreWriter.close();
			if(statMap[0] > 0) {
				statMap[1] = statMap[1] / statMap[0];
				statMap[2] = statMap[2] / statMap[0];
			}
			if(statMap[3] > 0)
				statMap[6] = statMap[6] / statMap[3];
			if(statMap[4] > 0)
				statMap[7] = statMap[7] / statMap[4];
			if(statMap[5] > 0)
				statMap[8] = statMap[8] / statMap[5];
			if(confList != null && confList.size() > 0)
				statMap[9] = (double) confList.size();
			statWriter = new BufferedWriter(new FileWriter(new File(getScoresFolder() + "trainingTimings.csv")));
			statWriter.write("algorithm,knowledge,#checkers,avg_time(ms),avg_" + getMetric().getMetricName() + ",#top10,#top50,#top100,avg_" + getMetric().getMetricName() + "_top10,avg_" + getMetric().getMetricName() + "_top50,avg_" + getMetric().getMetricName() + "_top100,#conf\n");
			statWriter.write(algTypes.toString() + "," + DetectionAlgorithm.getKnowledgeType(algTypes) + ",");
			for(double d : statMap){
				statWriter.write(d + ",");
			}
			statWriter.write("\n");
			statWriter.close();
			
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
