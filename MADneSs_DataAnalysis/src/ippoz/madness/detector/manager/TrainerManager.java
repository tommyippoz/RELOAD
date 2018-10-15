/**
 * 
 */
package ippoz.madness.detector.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.algorithm.elki.ABODELKI;
import ippoz.madness.detector.algorithm.elki.COFELKI;
import ippoz.madness.detector.algorithm.elki.FastABODELKI;
import ippoz.madness.detector.algorithm.elki.LOFELKI;
import ippoz.madness.detector.algorithm.elki.ODINELKI;
import ippoz.madness.detector.algorithm.elki.SVMELKI;
import ippoz.madness.detector.algorithm.weka.IsolationForestWEKA;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.dataseries.IndicatorDataSeries;
import ippoz.madness.detector.commons.dataseries.MultipleDataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.KnowledgeType;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.reputation.Reputation;
import ippoz.madness.detector.trainer.AlgorithmTrainer;
import ippoz.madness.detector.trainer.ConfigurationSelectorTrainer;
import ippoz.madness.detector.trainer.FixedConfigurationTrainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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
	private TrainerManager(String setupFolder, String dsDomain, String scoresFolder, String outputFolder, Map<KnowledgeType, List<Knowledge>> map, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, List<AlgorithmType> algTypes) {
		super(map, setupFolder, dsDomain, scoresFolder, confList, metric, reputation, algTypes);
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
	public TrainerManager(String setupFolder, String dsDomain, String scoresFolder, String outputFolder, Map<KnowledgeType, List<Knowledge>> expList, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, DataCategory[] dataTypes, List<AlgorithmType> algTypes, double simplePearson, double complexPearson) {
		super(expList, setupFolder, dsDomain, scoresFolder, confList, metric, reputation, dataTypes, algTypes, simplePearson, complexPearson);
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
	public TrainerManager(String setupFolder, String dsDomain, String scoresFolder, String outputFolder, Map<KnowledgeType, List<Knowledge>> expList, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, List<AlgorithmType> algTypes, List<DataSeries> selectedSeries) {
		super(expList, setupFolder, dsDomain, scoresFolder, confList, metric, reputation, algTypes, selectedSeries);
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
	public TrainerManager(String setupFolder, String dsDomain, String scoresFolder, String outputFolder, Map<KnowledgeType, List<Knowledge>> map, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, DataCategory[] dataTypes, List<AlgorithmType> algTypes, String[] selectedSeriesString) {
		this(setupFolder, dsDomain, scoresFolder, outputFolder, map, confList, metric, reputation, algTypes);
		seriesList = parseSelectedSeries(selectedSeriesString, dataTypes);
		AppLogger.logInfo(getClass(), seriesList.size() + " Data Series Loaded");
	}
	
	public List<DataSeries> generateDataSeries(DataCategory[] dataTypes, double pearsonSimple, double pearsonComplex) {
		if(dsDomain.equals("ALL")){
			return DataSeries.allCombinations(getIndicators(), dataTypes);
		} else if(dsDomain.equals("UNION")){
			return DataSeries.unionCombinations(getIndicators(), dataTypes);
		} else if(dsDomain.equals("SIMPLE")){
			return DataSeries.simpleCombinations(getIndicators(), dataTypes);
		} else if(dsDomain.contains("PEARSON") && dsDomain.contains("(") && dsDomain.contains(")")){
			return DataSeries.selectedCombinations(getIndicators(), dataTypes, readPearsonCombinations(Double.parseDouble(dsDomain.substring(dsDomain.indexOf("(")+1, dsDomain.indexOf(")")))));
		} else return DataSeries.selectedCombinations(getIndicators(), dataTypes, readPossibleIndCombinations());
	}
	
	private void clearTmpFolders(List<AlgorithmType> algTypes) {
		File tempFolder;
		for(AlgorithmType at : algTypes){
			if(at.equals(AlgorithmType.ELKI_ABOD)){
				tempFolder = new File(ABODELKI.DEFAULT_TMP_FOLDER);
				if(tempFolder.exists()){
					tempFolder.delete();
					AppLogger.logInfo(getClass(), "Clearing temporary folder '" + tempFolder.getPath() + "'");
				}
			} else if(at.equals(AlgorithmType.ELKI_FASTABOD)){
				tempFolder = new File(FastABODELKI.DEFAULT_TMP_FOLDER);
				if(tempFolder.exists()){
					tempFolder.delete();
					AppLogger.logInfo(getClass(), "Clearing temporary folder '" + tempFolder.getPath() + "'");
				}
			} else if(at.equals(AlgorithmType.ELKI_LOF)){
				tempFolder = new File(LOFELKI.DEFAULT_TMP_FOLDER);
				if(tempFolder.exists()){
					tempFolder.delete();
					AppLogger.logInfo(getClass(), "Clearing temporary folder '" + tempFolder.getPath() + "'");
				}
			} else if(at.equals(AlgorithmType.ELKI_COF)){
				tempFolder = new File(COFELKI.DEFAULT_TMP_FOLDER);
				if(tempFolder.exists()){
					tempFolder.delete();
					AppLogger.logInfo(getClass(), "Clearing temporary folder '" + tempFolder.getPath() + "'");
				}
			} else if(at.equals(AlgorithmType.ELKI_ODIN)){
				tempFolder = new File(ODINELKI.DEFAULT_TMP_FOLDER);
				if(tempFolder.exists()){
					tempFolder.delete();
					AppLogger.logInfo(getClass(), "Clearing temporary folder '" + tempFolder.getPath() + "'");
				}
			} else if(at.equals(AlgorithmType.ELKI_SVM)){
				tempFolder = new File(SVMELKI.DEFAULT_TMP_FOLDER);
				if(tempFolder.exists()){
					tempFolder.delete();
					AppLogger.logInfo(getClass(), "Clearing temporary folder '" + tempFolder.getPath() + "'");
				}
			} else if(at.equals(AlgorithmType.WEKA_ISOLATIONFOREST)){
				tempFolder = new File(IsolationForestWEKA.DEFAULT_TMP_FOLDER);
				if(tempFolder.exists()){
					tempFolder.delete();
					AppLogger.logInfo(getClass(), "Clearing temporary folder '" + tempFolder.getPath() + "'");
				}
			}
		}
	}
	
	private List<DataSeries> parseSelectedSeries(String[] selectedSeriesString, DataCategory[] dataTypes) {
		List<DataSeries> finalDs = new LinkedList<DataSeries>();
		List<DataSeries> allFeatures = new LinkedList<DataSeries>();
		List<DataSeries> all = generateDataSeries(dataTypes, 0.9, 0.9);
		for(String dsString : selectedSeriesString){
			for(DataSeries ds : all){
				if(ds.toString().equals(dsString)) {
					finalDs.add(ds);
					if(ds instanceof IndicatorDataSeries){
						boolean flag = false;
						for(DataSeries dsall : allFeatures){
							if(dsall.getName().equals(ds.getName())) {
								flag = true;
								break;
							}
						}
						if(!flag)
							allFeatures.add(ds);
					}
					break;
				}
			}
		}
		finalDs.add(new MultipleDataSeries(allFeatures));
		AppLogger.logInfo(getClass(), "Selected Data Series Loaded: " + finalDs.size());
		return finalDs;
	}

	/**
	 * Starts the train process. 
	 * The scores are saved in a file specified in the preferences.
	 */
	@SuppressWarnings("unchecked")
	public void train(){
		long start = System.currentTimeMillis();
		try {
			start();
			join();
			Collections.sort((List<AlgorithmTrainer>)getThreadList());
			AppLogger.logInfo(getClass(), "Training executed in " + (System.currentTimeMillis() - start) + "ms");
			saveScores(filterTrainers(getThreadList()), "scores.csv");
			AppLogger.logInfo(getClass(), "Training scores saved");
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to complete training phase");
		}
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
						pcManager.calculatePearsonIndexes(0.9, 0.9);
						trainerList.addAll(pcManager.getTrainers(getMetric(), getReputation(), confList));
						pcManager.flush();
						break;
					default:
						for(DataSeries dataSeries : seriesList){
							if(DetectionAlgorithm.isSeriesValidFor(algType, dataSeries))
								trainerList.add(new ConfigurationSelectorTrainer(algType, dataSeries, getMetric(), getReputation(), getKnowledge(kType), confList.get(algType)));
						}
						break;
				}
			} else {
				AppLogger.logError(getClass(), "UnrecognizedConfiguration", algType + " does not have an associated configuration");
			}	
		}
		setThreadList(trainerList);
		AppLogger.logInfo(getClass(), "Train is Starting");
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
				" Score: " + at.getMetricScore());		
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
			scoreWriter = new BufferedWriter(new FileWriter(new File(getScoresFolder() + filename)));
			scoreWriter.write("data_series,algorithm_type,reputation_score,metric_score(" + getMetric().getMetricName() + "),configuration\n");
			for(Thread tThread : list){
				trainer = (AlgorithmTrainer)tThread;
				if(trainer.isValidTrain()){
					count++;
					if(trainer.getBestConfiguration() != null) {
						scoreWriter.write(trainer.getSeriesDescription() + "�" + 
								trainer.getAlgType() + "�" +
								trainer.getReputationScore() + "�" + 
								trainer.getMetricScore() + "�" +  
								trainer.getBestConfiguration().toFileRow(false) + "\n");
						statMap.get(trainer.getAlgType())[0] += 1.0;
						statMap.get(trainer.getAlgType())[1] += trainer.getTrainingTime();
						statMap.get(trainer.getAlgType())[2] += trainer.getMetricScore();
						if(count <= 10)
							statMap.get(trainer.getAlgType())[3] += 1.0;
						if(count <= 50)
							statMap.get(trainer.getAlgType())[4] += 1.0;
						if(count <= 100)
							statMap.get(trainer.getAlgType())[5] += 1.0;
						if(count <= 10)
							statMap.get(trainer.getAlgType())[6] += trainer.getMetricScore();
						if(count <= 50)
							statMap.get(trainer.getAlgType())[7] += trainer.getMetricScore();
						if(count <= 100)
							statMap.get(trainer.getAlgType())[8] += trainer.getMetricScore();
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
