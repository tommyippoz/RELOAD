/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.multilayer.commons.datacategory.DataCategory;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.PreferencesManager;
import ippoz.multilayer.detector.commons.support.ThreadScheduler;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.performance.TrainingTiming;
import ippoz.multilayer.detector.reputation.Reputation;
import ippoz.multilayer.detector.trainer.AlgorithmTrainer;
import ippoz.multilayer.detector.trainer.ConfigurationFinderTrainer;
import ippoz.multilayer.detector.trainer.ConfigurationSelectorTrainer;
import ippoz.multilayer.detector.trainer.FixedConfigurationTrainer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class TrainerManager.
 * The manager responsible of the training process of the anomaly detector.
 *
 * @author Tommy
 */
public class TrainerManager extends ThreadScheduler {
	
	/** The preference manager. */
	private PreferencesManager prefManager;
	
	/** The timing manager. */
	private TimingsManager pManager;
	
	/** The experiments list. */
	private LinkedList<ExperimentData> expList;
	
	/** The possible configurations. */
	private HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>> confList;
	
	/** The chosen metric. */
	private Metric metric;
	
	/** The chosen reputation metric. */
	private Reputation reputation;
	
	/** The list of indicators. */
	private LinkedList<DataSeries> seriesList;
	
	/** The algorithm types. */
	private AlgorithmType[] algTypes;
	
	private TrainingTiming tTiming;
	
	private InvariantManager iManager;
	
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
	public TrainerManager(PreferencesManager prefManager, TimingsManager pManager, LinkedList<ExperimentData> expList, HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, DataCategory[] dataTypes, AlgorithmType[] algTypes) {
		super(1);
		this.prefManager = prefManager;
		this.pManager = pManager;
		this.expList = expList;
		this.confList = confList;
		this.metric = metric;
		this.reputation = reputation;
		this.algTypes = algTypes;
		tTiming = new TrainingTiming();
		seriesList = generateDataSeries(dataTypes);
	}
	
	private HashMap<String, String> readPossibleIndCombinations(){
		return readIndCombinations("indicatorCouples.csv");	
	}

	private HashMap<String, String> readIndCombinations(String filename){
		return readIndCombinations(new File(prefManager.getPreference(DetectionManager.SETUP_FILE_FOLDER) + filename));
	}
	
	private HashMap<String, String> readIndCombinations(File indCoupleFile){
		HashMap<String, String> comb = new HashMap<String, String>();
		BufferedReader reader;
		String readed;
		try {
			if(indCoupleFile.exists()){
				reader = new BufferedReader(new FileReader(indCoupleFile));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*") && readed.contains(";")){
							comb.put(readed.split(";")[0].trim(), readed.split(";")[1].trim());
						}
					}
				}
				reader.close();
			} 
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read indicator couples");
		}
		return comb;
	}

	private LinkedList<DataSeries> generateDataSeries(DataCategory[] dataTypes) {
		String complexDataPreference = prefManager.getPreference(DetectionManager.INV_DOMAIN);
		if(complexDataPreference.equals("ALL"))
			return DataSeries.allCombinations(expList.getFirst().getIndicators(), dataTypes);
		else return DataSeries.selectedCombinations(expList.getFirst().getIndicators(), dataTypes, readPossibleIndCombinations());
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
			Collections.sort((LinkedList<AlgorithmTrainer>)getThreadList());
			pManager.addTiming(TimingsManager.TRAIN_RUNS, Double.valueOf(expList.size()));
			pManager.addTiming(TimingsManager.TRAIN_TIME, (double)(System.currentTimeMillis() - start));
			pManager.addTiming(TimingsManager.AVG_TRAIN_TIME, ((System.currentTimeMillis() - start)/threadNumber()*1.0));
			AppLogger.logInfo(getClass(), "Training executed in " + (System.currentTimeMillis() - start) + "ms");
			saveTrainingTimes(filterTrainers(getThreadList()));
			saveScores(filterTrainers(getThreadList()));
			AppLogger.logInfo(getClass(), "Training scores saved");
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to complete training phase");
		}
	}

	private LinkedList<? extends Thread> filterTrainers(LinkedList<? extends Thread> trainerList) {
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
		long initStartTime = System.currentTimeMillis();
		AppLogger.logInfo(getClass(), "Train Started");
		LinkedList<AlgorithmTrainer> trainerList = new LinkedList<AlgorithmTrainer>();
		for(AlgorithmType algType : algTypes){
			if(confList.get(algType) != null){
				switch(algType){
					case RCC:
						trainerList.add(new FixedConfigurationTrainer(algType, null, metric, reputation, tTiming, expList, confList.get(algType).getFirst()));
						break;
					case PEA:
						PearsonCombinationManager pcManager;
						File pearsonFile = new File(prefManager.getPreference(DetectionManager.SETUP_FILE_FOLDER) + "pearsonCombinations.csv");
						pcManager = new PearsonCombinationManager(pearsonFile, seriesList, tTiming, expList);
						pcManager.calculatePearsonIndexes();
						trainerList.addAll(pcManager.getTrainers(metric, reputation, confList));
						pcManager.flush();
						break;
					default:
						for(DataSeries dataSeries : seriesList){
							trainerList.add(new ConfigurationSelectorTrainer(algType, dataSeries, metric, reputation, tTiming, expList, confList.get(algType)));
						}
						break;
				}
			} else {
				switch(algType){
					case INV:
						iManager = new InvariantManager(seriesList, tTiming, expList, metric, reputation, readPossibleIndCombinations());
						trainerList.addAll(iManager.getInvariants(prefManager.getPreference(DetectionManager.INV_DOMAIN).equals("ALL")));
						break;
					default:
						for(DataSeries dataSeries : seriesList){
							trainerList.add(new ConfigurationFinderTrainer(algType, dataSeries, metric, reputation, tTiming, expList));
						}
						break;
				}
			}	
		}
		setThreadList(trainerList);
		pManager.addTiming(TimingsManager.TRAIN_INIT_TIME, Double.valueOf(System.currentTimeMillis() - initStartTime));
		pManager.addTiming(TimingsManager.ANOMALY_CHECKERS, Double.valueOf(trainerList.size()));
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
		AppLogger.logInfo(getClass(), "[" + tIndex + "/" + threadNumber() + "] Found: " + ((AlgorithmTrainer)t).getBestConfiguration().toString());		
		((AlgorithmTrainer)t).flush();
	}
	
	private void saveTrainingTimes(LinkedList<? extends Thread> list) {
		BufferedWriter writer;
		try {
			tTiming.addAlgorithmScores(list);
			writer = new BufferedWriter(new FileWriter(new File(prefManager.getPreference(DetectionManager.OUTPUT_FOLDER) + "/trainingTimings.csv")));
			writer.write(tTiming.getHeader() + "\n");
			for(AlgorithmType algType : algTypes){
				writer.write(tTiming.toFileRow(algType) + "\n");
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write scores");
		}
	}
	
	/**
	 * Saves scores related to the executed AlgorithmTrainers.
	 *
	 * @param list the list of algorithm trainers
	 */
	private void saveScores(LinkedList<? extends Thread> list) {
		BufferedWriter writer;
		AlgorithmTrainer trainer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(prefManager.getPreference(DetectionManager.SCORES_FILE_FOLDER) + "scores.csv")));
			writer.write("data_series,algorithm_type,reputation_score,metric_score(" + metric.getMetricName() + "),configuration\n");
			for(Thread tThread : list){
				trainer = (AlgorithmTrainer)tThread;
				if(trainer.isValidTrain()) {
					writer.write(trainer.getSeriesDescription() + "§" + 
							trainer.getAlgType() + "§" +
							trainer.getReputationScore() + "§" + 
							trainer.getMetricScore() + "§" +  
							trainer.getBestConfiguration().toFileRow(false) + "\n");
				}
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write scores");
		}
	}	
	
}
