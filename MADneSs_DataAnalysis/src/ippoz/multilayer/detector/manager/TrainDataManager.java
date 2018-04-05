/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.commons.indicator.Indicator;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.knowledge.KnowledgeType;
import ippoz.multilayer.detector.commons.knowledge.data.MonitoredData;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.reputation.Reputation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Tommy
 *
 */
public abstract class TrainDataManager extends DataManager {
	
	/** The setup folder. */
	private String setupFolder;
	
	/** The data series domain. */
	private String dsDomain;
	
	/** The scores folder. */
	private String scoresFolder;
	
	/** The possible configurations. */
	protected Map<AlgorithmType, List<AlgorithmConfiguration>> confList;
	
	/** The chosen metric. */
	private Metric metric;
	
	/** The chosen reputation metric. */
	private Reputation reputation;
	
	/** The list of indicators. */
	protected List<DataSeries> seriesList;
	
	/** The algorithm types. */
	protected List<AlgorithmType> algTypes;

	/**
	 * Instantiates a new trainer data manager.
	 *
	 */
	public TrainDataManager(Indicator[] indicators, List<MonitoredData> expList, String setupFolder, String dsDomain, String scoresFolder, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, List<AlgorithmType> algTypes) {
		super(indicators, expList);
		this.setupFolder = setupFolder;
		this.dsDomain = dsDomain;
		this.scoresFolder = scoresFolder;
		this.confList = confList;
		this.metric = metric;
		this.reputation = reputation;
		this.algTypes = algTypes;
	}
	
	/**
	 * Instantiates a new trainer data manager.
	 *
	 */
	public TrainDataManager(Indicator[] indicators, List<MonitoredData> expList, String setupFolder, String dsDomain, String scoresFolder, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, List<AlgorithmType> algTypes, List<DataSeries> selectedSeries) {
		this(indicators, expList, setupFolder, dsDomain, scoresFolder, confList, metric, reputation, algTypes);
		seriesList = selectedSeries;
		AppLogger.logInfo(getClass(), seriesList.size() + " Data Series Loaded");
	}
	
	public TrainDataManager(Indicator[] indicators, List<MonitoredData> expList, String setupFolder, String dsDomain, String scoresFolder, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, DataCategory[] dataTypes, List<AlgorithmType> algTypes) {
		this(indicators, expList, setupFolder, dsDomain, scoresFolder, confList, metric, reputation, algTypes);
		seriesList = generateDataSeries(dataTypes);
		AppLogger.logInfo(getClass(), seriesList.size() + " Data Series Loaded");
	}
	
	public List<DataSeries> generateDataSeries(DataCategory[] dataTypes) {
		if(dsDomain.equals("ALL")){
			return DataSeries.allCombinations(getIndicators(), dataTypes);
		} else if(dsDomain.equals("SIMPLE")){
			return DataSeries.simpleCombinations(getIndicators(), dataTypes);
		} else if(dsDomain.contains("PEARSON") && dsDomain.contains("(") && dsDomain.contains(")")){
			if(!checkCorrelationInfo()){
				pearsonCorrelation(DataSeries.simpleCombinations(getIndicators(), dataTypes));
			}
			return DataSeries.selectedCombinations(getIndicators(), dataTypes, readPearsonCombinations(Double.parseDouble(dsDomain.substring(dsDomain.indexOf("(")+1, dsDomain.indexOf(")")))));
		} else return DataSeries.selectedCombinations(getIndicators(), dataTypes, readPossibleIndCombinations());
	}
	
	private void pearsonCorrelation(List<DataSeries> list) {
		PearsonCombinationManager pcManager;
		File pearsonFile = new File(getSetupFolder() + "pearsonCombinations.csv");
		pcManager = new PearsonCombinationManager(pearsonFile, list, getKnowledge(KnowledgeType.GLOBAL));
		pcManager.calculatePearsonIndexes();
		pcManager.flush();
	}

	private boolean checkCorrelationInfo() {
		return new File(getSetupFolder() + "pearsonCombinations.csv").exists();
	}
	
	private List<List<String>> readIndCombinations(String filename){
		return readIndCombinations(new File(setupFolder + filename));
	}
	
	private List<List<String>> readIndCombinations(File indCoupleFile){
		List<List<String>> comb = new LinkedList<List<String>>();
		List<String> nList;
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
							if(readed.split(",").length > 0){
								if(readed.split(",")[0].contains("@")){
									nList = new ArrayList<String>(readed.split(",")[0].split("@").length);
									for(String sName : readed.split(",")[0].split("@")){
										nList.add(sName.trim());
									}
								} else {
									nList = new ArrayList<String>(1);
									nList.add(readed.split(",")[0].trim());
								}
								comb.add(nList);
							}
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
	
	public List<List<String>> readPossibleIndCombinations(){
		return readIndCombinations("indicatorCouples.csv");	
	}
	
	public List<List<String>> readPearsonCombinations(double treshold){
		List<List<String>> comb = new LinkedList<List<String>>();
		File pFile = new File(setupFolder + "pearsonCombinations.csv");
		List<String> nList;
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
							if(readed.split(",").length > 0 && Math.abs(Double.valueOf(readed.split(",")[1].trim())) >= treshold){
								if(readed.split(",")[0].contains("@")){
									nList = new ArrayList<String>(readed.split(",")[0].split("@").length);
									for(String sName : readed.split(",")[0].split("@")){
										nList.add(sName.trim());
									}
								} else {
									nList = new ArrayList<String>(1);
									nList.add(readed.split(",")[0].trim());
								}
								comb.add(nList);
							}
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

	public String getSetupFolder() {
		return setupFolder;
	}

	public String getDsDomain() {
		return dsDomain;
	}

	public String getScoresFolder() {
		return scoresFolder;
	}

	public Metric getMetric() {
		return metric;
	}

	public Reputation getReputation() {
		return reputation;
	}
	
}
