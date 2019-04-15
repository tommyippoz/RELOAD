/**
 * 
 */
package ippoz.madness.detector.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.reputation.Reputation;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public abstract class TrainDataManager extends DataManager {
	
	/** The setup folder. */
	private String setupFolder;
	
	/** The data series domain. */
	protected String dsDomain;
	
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
	
	protected int kfold;

	/**
	 * Instantiates a new trainer data manager.
	 *
	 */
	public TrainDataManager(Map<KnowledgeType, List<Knowledge>> map, String setupFolder, String dsDomain, String scoresFolder, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, List<AlgorithmType> algTypes, int kfold) {
		super(map);
		this.setupFolder = setupFolder;
		this.dsDomain = dsDomain;
		this.scoresFolder = scoresFolder;
		this.confList = confList;
		this.metric = metric;
		this.reputation = reputation;
		this.algTypes = algTypes;
		this.kfold = kfold;
	}
	
	/**
	 * Instantiates a new trainer data manager.
	 *
	 */
	public TrainDataManager(Map<KnowledgeType, List<Knowledge>> expList, String setupFolder, String dsDomain, String scoresFolder, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, List<AlgorithmType> algTypes, List<DataSeries> selectedSeries, int kfold) {
		this(expList, setupFolder, dsDomain, scoresFolder, confList, metric, reputation, algTypes, kfold);
		seriesList = selectedSeries;
		AppLogger.logInfo(getClass(), seriesList.size() + " Data Series Loaded");
	}
	
	public TrainDataManager(Map<KnowledgeType, List<Knowledge>> expList, String setupFolder, String dsDomain, String scoresFolder, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, DataCategory[] dataTypes, List<AlgorithmType> algTypes, double pearsonSimple, double pearsonComplex, boolean generatePearson, int kfold) {
		this(expList, setupFolder, dsDomain, scoresFolder, confList, metric, reputation, algTypes, kfold);
		seriesList = generateDataSeries(dataTypes, pearsonComplex, generatePearson);
		AppLogger.logInfo(getClass(), seriesList.size() + " Data Series Loaded");
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
	
	public List<DataSeries> generateDataSeries(DataCategory[] dataTypes, double pearsonComplex, boolean generatePearson) {
		double pearsonSimple;
		if(dsDomain.equals("ALL")){
			return DataSeries.allCombinations(getIndicators(), dataTypes);
		} else if(dsDomain.equals("UNION")){
			return DataSeries.unionCombinations(getIndicators());
		} else if(dsDomain.equals("SIMPLE")){
			return DataSeries.simpleCombinations(getIndicators(), dataTypes);
		} else if(dsDomain.contains("PEARSON") && dsDomain.contains("(") && dsDomain.contains(")")){
			pearsonSimple = Double.valueOf(dsDomain.substring(dsDomain.indexOf("(")+1, dsDomain.indexOf(")")));
			if(generatePearson){
				pearsonCorrelation(DataSeries.simpleCombinations(getIndicators(), dataTypes), pearsonSimple, pearsonComplex);
			}
			return DataSeries.selectedCombinations(getIndicators(), dataTypes, readPearsonCombinations(Double.parseDouble(dsDomain.substring(dsDomain.indexOf("(")+1, dsDomain.indexOf(")")))));
		} else return DataSeries.selectedCombinations(getIndicators(), dataTypes, readPossibleIndCombinations());
	}
	
	/*
	 public List<DataSeries> generateDataSeries(DataCategory[] dataTypes, double pearsonSimple, double pearsonComplex) {
		if(dsDomain.equals("ALL")){
			return DataSeries.allCombinations(getIndicators(), dataTypes);
		} else if(dsDomain.equals("UNION")){
			return DataSeries.unionCombinations(getIndicators(), dataTypes);
		} else if(dsDomain.equals("SIMPLE")){
			return DataSeries.simpleCombinations(getIndicators(), dataTypes);
		} else if(dsDomain.contains("PEARSON") && dsDomain.contains("(") && dsDomain.contains(")")){
			pearsonCorrelation(DataSeries.simpleCombinations(getIndicators(), dataTypes), pearsonSimple, pearsonComplex);
			return DataSeries.selectedCombinations(getIndicators(), dataTypes, readPearsonCombinations(Double.parseDouble(dsDomain.substring(dsDomain.indexOf("(")+1, dsDomain.indexOf(")")))));
		} else return DataSeries.selectedCombinations(getIndicators(), dataTypes, readPossibleIndCombinations());
	}	 */
	
	private void pearsonCorrelation(List<DataSeries> list, double pearsonSimple, double pearsonComplex) {
		PearsonCombinationManager pcManager;
		File pearsonFile = new File(getSetupFolder() + "pearsonCombinations.csv");
		pcManager = new PearsonCombinationManager(pearsonFile, list, getKnowledge(), kfold);
		pcManager.calculatePearsonIndexes(pearsonSimple, pearsonComplex);
		pcManager.flush();
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
