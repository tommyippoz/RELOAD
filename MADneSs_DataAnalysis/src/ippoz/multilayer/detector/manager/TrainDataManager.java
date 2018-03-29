/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.commons.indicator.Indicator;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.knowledge.data.MonitoredData;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.reputation.Reputation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.AbstractMap.SimpleEntry;
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
			return DataSeries.selectedCombinations(getIndicators(), dataTypes, readPearsonCombinations(Double.parseDouble(dsDomain.substring(dsDomain.indexOf("(")+1, dsDomain.indexOf(")")))));
		} else return DataSeries.selectedCombinations(getIndicators(), dataTypes, readPossibleIndCombinations());
	}
	
	private List<Entry<String, String>> readIndCombinations(String filename){
		return readIndCombinations(new File(setupFolder + filename));
	}
	
	private List<Entry<String, String>> readIndCombinations(File indCoupleFile){
		List<Entry<String, String>> comb = new LinkedList<Entry<String,String>>();
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
	
	public List<Entry<String, String>> readPossibleIndCombinations(){
		return readIndCombinations("indicatorCouples.csv");	
	}
	
	public List<Entry<String, String>> readPearsonCombinations(double treshold){
		List<Entry<String, String>> comb = new LinkedList<Entry<String,String>>();
		File pFile = new File(setupFolder + "pearsonCombinations.csv");
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
