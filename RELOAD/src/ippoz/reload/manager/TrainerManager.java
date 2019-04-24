/**
 * 
 */
package ippoz.reload.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;
import ippoz.reload.trainer.AlgorithmTrainer;
import ippoz.reload.trainer.ConfigurationSelectorTrainer;
import ippoz.reload.trainer.FixedConfigurationTrainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
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
	
	/** The output folder. */
	//private String outputFolder;
		
	private InvariantManager iManager;
	
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
	private TrainerManager(String setupFolder, String dsDomain, String scoresFolder, String outputFolder, Map<KnowledgeType, List<Knowledge>> map, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, List<AlgorithmType> algTypes, int kfold) {
		super(map, setupFolder, dsDomain, scoresFolder, confList, metric, reputation, algTypes, kfold);
		clearTmpFolders(algTypes);
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
	 * @param complexPearson 
	 * @param simplePearson 
	 * @param algTypes2 the algorithm types
	 */
	public TrainerManager(String setupFolder, String dsDomain, String scoresFolder, String outputFolder, Map<KnowledgeType, List<Knowledge>> expList, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, DataCategory[] dataTypes, List<AlgorithmType> algTypes, double simplePearson, double complexPearson, int kfold) {
		super(expList, setupFolder, dsDomain, scoresFolder, confList, metric, reputation, dataTypes, algTypes, simplePearson, complexPearson, false, kfold);
		clearTmpFolders(algTypes);
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
	public TrainerManager(String setupFolder, String dsDomain, String scoresFolder, String outputFolder, Map<KnowledgeType, List<Knowledge>> expList, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, List<AlgorithmType> algTypes, List<DataSeries> selectedSeries, int kfold) {
		super(expList, setupFolder, dsDomain, scoresFolder, confList, metric, reputation, algTypes, selectedSeries, kfold);
		clearTmpFolders(algTypes);
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
	public TrainerManager(String setupFolder, String dsDomain, String scoresFolder, String outputFolder, Map<KnowledgeType, List<Knowledge>> map, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, DataCategory[] dataTypes, List<AlgorithmType> algTypes, String[] selectedSeriesString, int kfold) {
		this(setupFolder, dsDomain, scoresFolder, outputFolder, map, confList, metric, reputation, algTypes, kfold);
		seriesList = parseSelectedSeries(selectedSeriesString, dataTypes);
		AppLogger.logInfo(getClass(), seriesList.size() + " Data Series Loaded");
	}
	
	private void clearTmpFolders(List<AlgorithmType> algTypes) {
		File rootFolder = new File(new File(".").getAbsolutePath().substring(0, new File(".").getAbsolutePath().length()-2));
		for(File file : rootFolder.listFiles()){
			if(file.isDirectory() && file.getName().endsWith("_tmp_RELOAD")){
				if(file.delete())
					AppLogger.logInfo(getClass(), "Clearing temporary folder '" + file.getPath() + "'");
				else AppLogger.logInfo(getClass(), "Failed to clean folder '" + file.getPath() + "'");
			}
		}
	}
	
	private List<DataSeries> parseSelectedSeries(String[] selectedSeriesString, DataCategory[] dataTypes) {
		List<DataSeries> finalList = DataSeries.fromString(selectedSeriesString, false);
		AppLogger.logInfo(getClass(), "Selected Data Series Loaded: " + finalList.size());
		return finalList;
	}

	/**
	 * Starts the train process. 
	 * The scores are saved in a file specified in the preferences.
	 */
	@SuppressWarnings("unchecked")
	public void train(String outFilename){
		long start = System.currentTimeMillis();
		try {
			start();
			join();
			Collections.sort((List<AlgorithmTrainer>)getThreadList());
			AppLogger.logInfo(getClass(), "Training executed in " + (System.currentTimeMillis() - start) + "ms");
			saveScores(filterTrainers(getThreadList()), outFilename + "_scores.csv");
			saveThresholdRelevance(filterTrainers(getThreadList()), outFilename + "_thresholdrelevance.csv");
			AppLogger.logInfo(getClass(), "Training scores saved");
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to complete training phase");
		}
	}

	private void saveThresholdRelevance(List<? extends Thread> list, String filename) {
		BufferedWriter thresholdRelevanceWriter;
		AlgorithmConfiguration conf;
		AlgorithmTrainer trainer;
		Map<String,Integer> statMap = new HashMap<String, Integer>();
		try {
			for(Thread tThread : list){
				trainer = (AlgorithmTrainer)tThread;
				if(trainer.isValidTrain()){
					conf = trainer.getBestConfiguration();
					if(conf != null && conf.hasItem(AlgorithmConfiguration.THRESHOLD)) {
						if(statMap.containsKey(conf.getItem(AlgorithmConfiguration.THRESHOLD))){
							statMap.put(conf.getItem(AlgorithmConfiguration.THRESHOLD), statMap.get(conf.getItem(AlgorithmConfiguration.THRESHOLD)) + 1);
						} else statMap.put(conf.getItem(AlgorithmConfiguration.THRESHOLD), 1);
					}
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

	private List<? extends Thread> filterTrainers(List<? extends Thread> trainerList) {
		LinkedList<AlgorithmTrainer> invList = new LinkedList<AlgorithmTrainer>();
		if(iManager != null){
			for(Thread t : trainerList){
				if(((AlgorithmTrainer)t).getAlgType().equals(AlgorithmType.INV))
					invList.add((AlgorithmTrainer)t);
			}
			trainerList.removeAll(iManager.filterInvType(invList));
		}
		return trainerList;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#initRun()
	 */
	@Override
	protected void initRun(){
		List<AlgorithmTrainer> trainerList = new LinkedList<AlgorithmTrainer>();
		AppLogger.logInfo(getClass(), "Initializing Train...");
		for(AlgorithmType algType : algTypes){
			if(confList.get(algType) != null){
				KnowledgeType kType = DetectionAlgorithm.getKnowledgeType(algType);
				switch(algType){
					case RCC:
						trainerList.add(new FixedConfigurationTrainer(algType, null, getMetric(), getReputation(), getKnowledge(kType), confList.get(algType).get(0)));
						break;
					case PEA:
						PearsonCombinationManager pcManager;
						File pearsonFile = new File(getScoresFolder() + "pearsonCombinations.csv");
						pcManager = new PearsonCombinationManager(pearsonFile, seriesList, getKnowledge(kType));
						pcManager.calculatePearsonIndexes(0.9);
						trainerList.addAll(pcManager.getTrainers(getMetric(), getReputation(), confList));
						pcManager.flush();
						break;
					default:
						for(DataSeries dataSeries : seriesList){
							if(DetectionAlgorithm.isSeriesValidFor(algType, dataSeries))
								trainerList.add(new ConfigurationSelectorTrainer(algType, dataSeries, getMetric(), getReputation(), getKnowledge(kType), confList.get(algType), kfold));
						}
						break;
				}
			} else {
				AppLogger.logError(getClass(), "UnrecognizedConfiguration", algType + " does not have an associated configuration");
			}	
		}
		setThreadList(trainerList);
		AppLogger.logInfo(getClass(), "Train of '" + algTypes.toString() + "' is Starting");
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
	
	/*private void saveTrainingTimes(List<? extends Thread> list) {
		BufferedWriter writer;
		try {
			tTiming.addAlgorithmScores(list);
			writer = new BufferedWriter(new FileWriter(new File(outputFolder + "/trainingTimings.csv")));
			writer.write(tTiming.getHeader() + "\n");
			for(AlgorithmType algType : algTypes){
				writer.write(tTiming.toFileRow(algType) + "\n");
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write scores");
		}
	}*/
	
	/**
	 * Saves scores related to the executed AlgorithmTrainers.
	 *
	 * @param list the list of algorithm trainers
	 */
	private void saveScores(List<? extends Thread> list, String filename) {
		BufferedWriter scoreWriter, statWriter;
		AlgorithmTrainer trainer;
		int count = 0;
		Map<AlgorithmType,Double[]> statMap = new HashMap<AlgorithmType, Double[]>();
		try {
			for(AlgorithmType at : algTypes){
				statMap.put(at, new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
			}
			scoreWriter = new BufferedWriter(new FileWriter(new File(filename)));
			scoreWriter.write("data_series,algorithm_type,reputation_score,metric_score(" + getMetric().getMetricName() + "),configuration\n");
			for(Thread tThread : list){
				trainer = (AlgorithmTrainer)tThread;
				if(trainer.isValidTrain()){
					count++;
					if(trainer.getBestConfiguration() != null) {
						scoreWriter.write(trainer.getSeriesDescription() + "§" + 
								trainer.getAlgType() + "§" +
								trainer.getReputationScore() + "§" + 
								trainer.getMetricAvgScore() + "§" +  
								trainer.getMetricStdScore() + "§" + 
								trainer.getBestConfiguration().toFileRow(false) + "\n");
						statMap.get(trainer.getAlgType())[0] += 1.0;
						statMap.get(trainer.getAlgType())[1] += trainer.getTrainingTime();
						statMap.get(trainer.getAlgType())[2] += trainer.getMetricAvgScore();
						if(count <= 10)
							statMap.get(trainer.getAlgType())[3] += 1.0;
						if(count <= 50)
							statMap.get(trainer.getAlgType())[4] += 1.0;
						if(count <= 100)
							statMap.get(trainer.getAlgType())[5] += 1.0;
						if(count <= 10)
							statMap.get(trainer.getAlgType())[6] += trainer.getMetricAvgScore();
						if(count <= 50)
							statMap.get(trainer.getAlgType())[7] += trainer.getMetricAvgScore();
						if(count <= 100)
							statMap.get(trainer.getAlgType())[8] += trainer.getMetricAvgScore();
					}
				}			
			}
			scoreWriter.close();
			for(AlgorithmType at : algTypes){
				if(statMap.get(at)[0] > 0) {
					statMap.get(at)[1] = statMap.get(at)[1] / statMap.get(at)[0];
					statMap.get(at)[2] = statMap.get(at)[2] / statMap.get(at)[0];
				}
				if(statMap.get(at)[3] > 0)
					statMap.get(at)[6] = statMap.get(at)[6] / statMap.get(at)[3];
				if(statMap.get(at)[4] > 0)
					statMap.get(at)[7] = statMap.get(at)[7] / statMap.get(at)[4];
				if(statMap.get(at)[5] > 0)
					statMap.get(at)[8] = statMap.get(at)[8] / statMap.get(at)[5];
				statMap.get(at)[9] = (double) confList.get(at).size();
			}
			statWriter = new BufferedWriter(new FileWriter(new File(getScoresFolder() + "trainingTimings.csv")));
			statWriter.write("algorithm,knowledge,#checkers,avg_time(ms),avg_" + getMetric().getMetricName() + ",#top10,#top50,#top100,avg_" + getMetric().getMetricName() + "_top10,avg_" + getMetric().getMetricName() + "_top50,avg_" + getMetric().getMetricName() + "_top100,#conf\n");
			for(AlgorithmType at : algTypes){
				statWriter.write(at.toString() + "," + DetectionAlgorithm.getKnowledgeType(at) + ",");
				for(double d : statMap.get(at)){
					statWriter.write(d + ",");
				}
				statWriter.write("\n");
			}
			statWriter.close();
			
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write scores");
		}
	}
	
}
