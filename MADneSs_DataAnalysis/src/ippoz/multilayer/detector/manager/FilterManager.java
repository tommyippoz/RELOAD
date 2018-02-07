/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.madness.commons.datacategory.DataCategory;
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
import ippoz.multilayer.detector.trainer.ConfigurationSelectorTrainer;
import ippoz.multilayer.detector.trainer.FixedConfigurationTrainer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * @author Tommy
 *
 */
public class FilterManager extends ThreadScheduler {
	
	/** The preference manager. */
	private PreferencesManager prefManager;
	
	/** The timing manager. */
	private TimingsManager pManager;
	
	/** The experiments list. */
	private LinkedList<ExperimentData> goldenExps;
	
	/** The possible configurations. */
	private HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>> confList;
	
	/** The chosen metric. */
	private Metric metric;
	
	/** The chosen reputation metric. */
	private Reputation reputation;
	
	/** The list of indicators. */
	private LinkedList<DataSeries> seriesList;
	
	/** The algorithm types. */
	private LinkedList<AlgorithmType> algTypes;
	
	private TrainingTiming tTiming;
	
	private double filteringThreshold;
	
	/**
	 * Instantiates a new trainer manager.
	 *
	 * @param prefManager the preference manager
	 * @param pManager the timing manager
	 * @param expList the experiment list
	 * @param confList the configuration list
	 * @param metric the chosen metric
	 * @param reputation the chosen reputation metric
	 * @param algTypes the algorithm types
	 */
	private FilterManager(PreferencesManager prefManager, TimingsManager pManager, LinkedList<ExperimentData> expList, HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, LinkedList<AlgorithmType> algTypes, double ftreshold) {
		super(2);
		this.prefManager = prefManager;
		this.pManager = pManager;
		this.goldenExps = expList;
		this.confList = confList;
		this.metric = metric;
		this.reputation = reputation;
		this.algTypes = algTypes;
		tTiming = new TrainingTiming();
		filteringThreshold = ftreshold;
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
	public FilterManager(PreferencesManager prefManager, TimingsManager pManager, LinkedList<ExperimentData> expList, HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, DataCategory[] dataTypes, LinkedList<AlgorithmType> algTypes, double ftreshold) {
		this(prefManager, pManager, expList, confList, metric, reputation, algTypes, ftreshold);
		if(!checkCorrelationInfo()){
			pearsonCorrelation(DataSeries.simpleCombinations(goldenExps.getFirst().getIndicators(), dataTypes));
		}
		seriesList = generateDataSeries(dataTypes);
		AppLogger.logInfo(getClass(), seriesList.size() + " Data Series Loaded");
	}
	
	private void pearsonCorrelation(LinkedList<DataSeries> list) {
		PearsonCombinationManager pcManager;
		File pearsonFile = new File(prefManager.getPreference(DetectionManager.SETUP_FILE_FOLDER) + "pearsonCombinations.csv");
		pcManager = new PearsonCombinationManager(pearsonFile, list, tTiming, goldenExps);
		pcManager.calculatePearsonIndexes();
		pcManager.flush();
	}

	private boolean checkCorrelationInfo() {
		return new File(prefManager.getPreference(DetectionManager.SETUP_FILE_FOLDER) + "pearsonCombinations.csv").exists();
	}

	private LinkedList<Entry<String, String>> readPossibleIndCombinations(){
		return readIndCombinations("indicatorCouples.csv");	
	}
	
	private LinkedList<Entry<String, String>> readPearsonCombinations(double treshold){
		LinkedList<Entry<String, String>> comb = new LinkedList<Entry<String,String>>();
		File pFile = new File(prefManager.getPreference(DetectionManager.SETUP_FILE_FOLDER) + "pearsonCombinations.csv");
		BufferedReader reader;
		String readed;
		try {
			if(pFile.exists()){
				reader = new BufferedReader(new FileReader(pFile));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*") && readed.contains(",")){
							if(Math.abs(Double.valueOf(readed.split(",")[2].trim())) >= treshold)
								comb.add(new SimpleEntry<String, String>(readed.split(",")[0].trim(), readed.split(",")[1].trim()));
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

	private LinkedList<Entry<String, String>> readIndCombinations(String filename){
		return readIndCombinations(new File(prefManager.getPreference(DetectionManager.SETUP_FILE_FOLDER) + filename));
	}
	
	private LinkedList<Entry<String, String>> readIndCombinations(File indCoupleFile){
		LinkedList<Entry<String, String>> comb = new LinkedList<Entry<String,String>>();
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
							comb.add(new SimpleEntry<String, String>(readed.split(";")[0].trim(), readed.split(";")[1].trim()));
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
		String complexDataPreference = prefManager.getPreference(DetectionManager.DATA_SERIES_DOMAIN);
		if(complexDataPreference.equals("ALL")){
			return DataSeries.allCombinations(goldenExps.getFirst().getIndicators(), dataTypes);
		} else if(complexDataPreference.equals("SIMPLE")){
			return DataSeries.simpleCombinations(goldenExps.getFirst().getIndicators(), dataTypes);
		} else if(complexDataPreference.contains("PEARSON") && complexDataPreference.contains("(") && complexDataPreference.contains(")")){
			return DataSeries.selectedCombinations(goldenExps.getFirst().getIndicators(), dataTypes, readPearsonCombinations(Double.parseDouble(complexDataPreference.substring(complexDataPreference.indexOf("(")+1, complexDataPreference.indexOf(")")))));
		} else return DataSeries.selectedCombinations(goldenExps.getFirst().getIndicators(), dataTypes, readPossibleIndCombinations());
	}

	/**
	 * Starts the train process. 
	 * The scores are saved in a file specified in the preferences.
	 */
	@SuppressWarnings("unchecked")
	public LinkedList<DataSeries> filter(){
		LinkedList<DataSeries> filteredSeries = null;
		long start = System.currentTimeMillis();
		try {
			start();
			join();
			Collections.sort((LinkedList<AlgorithmTrainer>)getThreadList());
			AppLogger.logInfo(getClass(), "Filtering executed in " + (System.currentTimeMillis() - start) + "ms");
			filteredSeries = selectDataSeries((LinkedList<AlgorithmTrainer>)getThreadList());
			saveFilteredSeries(filteredSeries, "filtered.csv");
			AppLogger.logInfo(getClass(), "Filtered Checkers Saved");
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to complete training phase");
		}
		return filteredSeries;
	}
	
	private LinkedList<DataSeries> selectDataSeries(LinkedList<AlgorithmTrainer> atList) {
		LinkedList<DataSeries> result = new LinkedList<DataSeries>();
		for(AlgorithmTrainer at : atList){
			if(at.getMetricScore() <= filteringThreshold){
				if(!result.contains(at.getDataSeries()))
					result.add(at.getDataSeries());
			}
		}
		AppLogger.logInfo(getClass(), "Filtered Data Series are " + result.size() + " out of the possible " + seriesList.size());
		return result;
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
						trainerList.add(new FixedConfigurationTrainer(algType, null, metric, reputation, tTiming, goldenExps, confList.get(algType).getFirst()));
						break;
					case PEA:
						PearsonCombinationManager pcManager;
						File pearsonFile = new File(prefManager.getPreference(DetectionManager.SETUP_FILE_FOLDER) + "pearsonCombinations.csv");
						pcManager = new PearsonCombinationManager(pearsonFile, seriesList, tTiming, goldenExps);
						pcManager.calculatePearsonIndexes();
						trainerList.addAll(pcManager.getTrainers(metric, reputation, confList));
						pcManager.flush();
						break;
					default:
						for(DataSeries dataSeries : seriesList){
							if(dataSeries.compliesWith(algType))
								trainerList.add(new ConfigurationSelectorTrainer(algType, dataSeries, metric, reputation, tTiming, goldenExps, confList.get(algType)));
						}
						break;
				}
			} else AppLogger.logError(getClass(), "UnrecognizedConfiguration", algType + " does not have an associated configuration");	
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
	
	/**
	 * Saves scores related to the executed AlgorithmTrainers.
	 *
	 * @param list the list of algorithm trainers
	 */
	private void saveFilteredSeries(LinkedList<DataSeries> list, String filename) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(prefManager.getPreference(DetectionManager.SCORES_FILE_FOLDER) + filename)));
			writer.write("data_series,algorithm_type,reputation_score,metric_score(" + metric.getMetricName() + "),configuration\n");
			for(DataSeries ds : list){
				writer.write(ds.toString() + "\n");			
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write series");
		}
	}
	
}
