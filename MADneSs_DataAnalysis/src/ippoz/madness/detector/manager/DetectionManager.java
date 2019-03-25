/**
 * 
 */
package ippoz.madness.detector.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.GlobalKnowledge;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.KnowledgeType;
import ippoz.madness.detector.commons.knowledge.SingleKnowledge;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.knowledge.data.MonitoredData;
import ippoz.madness.detector.commons.knowledge.sliding.SlidingPolicy;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.commons.support.PreferencesManager;
import ippoz.madness.detector.loader.Loader;
import ippoz.madness.detector.metric.FalsePositiveRate_Metric;
import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.output.DetectorOutput;
import ippoz.madness.detector.reputation.Reputation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Class DetectionManager.
 * This is the manager for the detection. Coordinates all the other managers, especially the ones responsible for training and validation phases.
 *
 * @author Tommy
 */
public class DetectionManager {
		
	/** The input manager. */
	private InputManager iManager;
	
	/** The chosen metric. */
	private Metric metric;
	
	/** The chosen reputation metric. */
	private Reputation reputation;
	
	/** The used data types (plain, diff...). */
	private DataCategory[] dataTypes;
	
	/** The algorithm types (SPS, Historical...). */
	private List<AlgorithmType> algTypes;
	
	private PreferencesManager loaderPref;
	
	private Integer windowSize;
	
	private SlidingPolicy sPolicy;
	
	private List<DataSeries> selectedDataSeries;
	
	/**
	 * Instantiates a new detection manager.
	 * @param loaderPref 
	 *
	 * @param prefManager the main preference manager
	 */
	public DetectionManager(InputManager iManager, List<AlgorithmType> algTypes, PreferencesManager loaderPref){
		this(iManager, algTypes, loaderPref, null, null);
	}
	
	public DetectionManager(InputManager iManager, List<AlgorithmType> algTypes, PreferencesManager loaderPref, Integer windowSize, SlidingPolicy sPolicy) {
		this.iManager = iManager;
		this.algTypes = algTypes;
		this.loaderPref = loaderPref;
		this.windowSize = windowSize;
		this.sPolicy = sPolicy;
		metric = iManager.getMetricName();
		reputation = iManager.getReputation(metric);
		dataTypes = iManager.getDataTypes();
	}
	
	public Metric[] getMetrics() {
		return iManager.loadValidationMetrics();
	}

	public String getTag() {
		return getWritableTag().replace(",", " ").trim();
	}
	
	private String getWritableTag() {
		String tag = "";
		if(loaderPref != null)
			tag = tag + loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.'));
		tag = tag + ",";
		if(loaderPref != null)
			tag = tag + loaderPref.getPreference(Loader.VALIDATION_RUN_PREFERENCE).replace(",", "");
		tag = tag + ",";
		if(algTypes != null)
			tag = tag + Arrays.toString(algTypes.toArray()).replace(",", "");
		tag = tag + ",";
		if(windowSize != null)
			tag = tag + windowSize;
		tag = tag + ",";
		if(sPolicy != null)
			tag = tag + sPolicy.toString();
		return tag;
	}
	
	private List<Loader> buildLoader(String loaderTag){
		if(loaderPref != null){
			return buildLoaderList(loaderTag, iManager.getAnomalyWindow());
		} else return new LinkedList<>();
	}
	
	private List<Loader> buildLoaderList(String loaderTag, int anomalyWindow){
		List<Loader> lList = new LinkedList<>();
		Loader newLoader;
		List<Integer> runs;
		int nRuns;
		String runsString = loaderPref.getPreference(loaderTag.equals("validation") ? Loader.VALIDATION_RUN_PREFERENCE : (loaderTag.equals("filter") ? Loader.FILTERING_RUN_PREFERENCE : Loader.TRAIN_RUN_PREFERENCE));
		if(runsString != null && runsString.length() > 0){
			if(runsString.startsWith("@") && runsString.contains("(") && runsString.contains(")")){
				nRuns = Integer.parseInt(runsString.substring(runsString.indexOf('@')+1, runsString.indexOf('(')));
				runs = iManager.readRunIds(runsString.substring(runsString.indexOf('(')+1, runsString.indexOf(')')));
				for(int i=0;i<runs.size();i=i+nRuns){
					newLoader = iManager.buildSingleLoader(loaderPref, new LinkedList<Integer>(runs.subList(i, i+nRuns > runs.size() ? runs.size() : i+nRuns)), loaderTag, anomalyWindow);
					if(newLoader != null)
						lList.add(newLoader);
				}
			} else {
				newLoader = iManager.buildSingleLoader(loaderPref, iManager.readRunIds(runsString), loaderTag, anomalyWindow);
				if(newLoader != null)
					lList.add(newLoader);
			}
		} else AppLogger.logError(getClass(), "LoaderError", "Unable to find run preference");
		return lList;
	}
	
	/**
	 * Check premises for the execution, such as MySQL server status.
	 *
	 * @return true, if premises are satisfied
	 */
	public boolean checkAssumptions(){
		if(loaderPref.getPreference(Loader.LOADER_TYPE) != null && 
				loaderPref.getPreference(Loader.LOADER_TYPE).equalsIgnoreCase("MYSQL") && 
					!AppUtility.isServerUp(3306)){
			AppLogger.logError(getClass(), "MySQLException", "MySQL is not running. Please activate it");
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a boolean targeting the need of training before evaluation.
	 *
	 * @return true if training is needed
	 */
	public boolean needTraining(){
		return iManager.getTrainingFlag();
	}
	
	public boolean needFiltering() {
		return !iManager.filteringResultExists(loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.'))) && iManager.getFilteringFlag();
	}

	/**
	 * Starts the train process.
	 */
	public void filterIndicators(){
		FilterManager fManager;
		try {
			if(needFiltering()) {
				fManager = new FilterManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), generateKnowledge(buildLoader("filter").get(0).fetch()), iManager.loadConfiguration(AlgorithmType.ELKI_KMEANS, windowSize, sPolicy), new FalsePositiveRate_Metric(true), reputation, dataTypes, iManager.getFilteringTreshold(), iManager.getSimplePearsonThreshold(), iManager.getComplexPearsonThreshold(), iManager.getKFoldCounter());
				selectedDataSeries = fManager.filter(loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.')) + "_filtered.csv");
				fManager.flush();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to filter indicators");
		}
	}	
	
	public String buildOutFilePrequel(){
		return loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.'));
	}
	
	/**
	 * Starts the train process.
	 */
	public void train(){
		TrainerManager tManager;
		try {
			if(needTraining()) {
				if(selectedDataSeries == null && !iManager.filteringResultExists(loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.'))))
					tManager = new TrainerManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), iManager.getOutputFolder(), generateKnowledge(buildLoader("train").iterator().next().fetch()), iManager.loadConfigurations(algTypes, windowSize, sPolicy), metric, reputation, dataTypes, algTypes, iManager.getSimplePearsonThreshold(), iManager.getComplexPearsonThreshold(), iManager.getKFoldCounter());
				else {
					if(selectedDataSeries == null){
						tManager = new TrainerManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), iManager.getOutputFolder(), generateKnowledge(buildLoader("train").iterator().next().fetch()), iManager.loadConfigurations(algTypes, windowSize, sPolicy), metric, reputation, dataTypes, algTypes, loadSelectedDataSeriesString(), iManager.getKFoldCounter());
					} else tManager = new TrainerManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), iManager.getOutputFolder(), generateKnowledge(buildLoader("train").iterator().next().fetch()), iManager.loadConfigurations(algTypes, windowSize, sPolicy), metric, reputation, algTypes, selectedDataSeries, iManager.getKFoldCounter()); 
				}
				tManager.train(buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1) + "_scores.csv");
				tManager.flush();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to train detector");
		}
	}
	
	private Map<KnowledgeType, List<Knowledge>> generateKnowledge(List<MonitoredData> expList) {
		Map<KnowledgeType, List<Knowledge>> map = new HashMap<KnowledgeType, List<Knowledge>>();
		for(AlgorithmType at : algTypes){
			if(!map.containsKey(DetectionAlgorithm.getKnowledgeType(at)))
				map.put(DetectionAlgorithm.getKnowledgeType(at), new ArrayList<Knowledge>(expList.size()));
		}
		if(needFiltering()){
			if(!map.containsKey(DetectionAlgorithm.getKnowledgeType(AlgorithmType.ELKI_KMEANS)))
				map.put(DetectionAlgorithm.getKnowledgeType(AlgorithmType.ELKI_KMEANS), new ArrayList<Knowledge>(expList.size()));
		}
		for(int i=0;i<expList.size();i++){
			if(map.containsKey(KnowledgeType.GLOBAL))
				map.get(KnowledgeType.GLOBAL).add(new GlobalKnowledge(expList.get(i)));
			if(map.containsKey(KnowledgeType.SLIDING))
				map.get(KnowledgeType.SLIDING).add(new SlidingKnowledge(expList.get(i), sPolicy, windowSize));
			if(map.containsKey(KnowledgeType.SINGLE))
				map.get(KnowledgeType.SINGLE).add(new SingleKnowledge(expList.get(i)));
		}
		AppLogger.logInfo(getClass(), expList.size() + " runs loaded (K-Fold:" + iManager.getKFoldCounter() + ")");
		return map;
	}

	private String[] loadSelectedDataSeriesString() {
		LinkedList<String> sSeries = new LinkedList<String>();
		String readed;
		BufferedReader reader = null;
		File dsF = new File(iManager.getScoresFolder() + buildOutFilePrequel() + "_filtered.csv");
		try {
			reader = new BufferedReader(new FileReader(dsF));
			while(reader.ready()){
				readed = reader.readLine();
				if(readed != null){
					readed = readed.trim();
					if(readed.length() > 0 && !readed.trim().startsWith("*")){
						sSeries.add(readed.trim());
					}
				}
			}
			reader.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read Selected data Series");
		} 
		return sSeries.toArray(new String[sSeries.size()]);
	}

	/**
	 * Starts the evaluation process.
	 * @return 
	 */
	public DetectorOutput evaluate(){
		Metric[] metList = iManager.loadValidationMetrics();
		boolean printOutput = iManager.getOutputVisibility();
		List<Loader> lList = buildLoader("validation");
		List<MonitoredData> bestExpList = null;
		List<MonitoredData> expList;
		DetectorOutput dOut = null;
		String bestRuns = null;
		double bestScore = 0;
		double score;
		int index = 0;
		try {
			if(lList.size() > 1){
				for(Loader l : lList){
					expList = l.fetch();
					AppLogger.logInfo(getClass(), "[" + (++index) + "/" + lList.size() + "] Evaluating " + expList.size() + " runs (" + l.getRuns() + ")");
					score = singleEvaluation(metList, generateKnowledge(expList), printOutput).getBestScore();
					if(score > bestScore){
						bestRuns = l.getRuns();
						bestExpList = expList;
						bestScore = score;
					}
					AppLogger.logInfo(getClass(), "Score is " + new DecimalFormat("#.##").format(score) + ", best is " + new DecimalFormat("#.##").format(bestScore));
				}
				dOut = singleEvaluation(metList, generateKnowledge(bestExpList), printOutput);
				dOut.setBestRuns(bestRuns);
			} else {
				Loader l = lList.iterator().next();
				bestExpList = l.fetch();
				dOut = singleEvaluation(metList, generateKnowledge(bestExpList), printOutput);
				dOut.setBestRuns(l.getRuns());
			}	
			dOut.summarizeCSV(iManager.getOutputFolder());
			dOut.printDetailedKnowledgeScores(iManager.getOutputFolder());
			AppLogger.logInfo(getClass(), "Final score is " + new DecimalFormat("#.##").format(dOut.getBestScore()) + ", runs (" + dOut.getBestRuns() + ")");
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate detector");
		}
		return dOut;
	}
	
	private DetectorOutput singleEvaluation(Metric[] metList, Map<KnowledgeType, List<Knowledge>> map, boolean printOutput){
		EvaluatorManager bestEManager = null;
		double bestScore = 0;
		String[] anomalyTresholds = iManager.parseAnomalyTresholds();
		String[] voterTresholds = iManager.parseVoterTresholds();
		Map<String, Integer> nVoters = new HashMap<String, Integer>();
		Map<String, Map<String, List<Map<Metric, Double>>>> evaluations = new HashMap<String, Map<String, List<Map<Metric,Double>>>>();
		for(String voterTreshold : voterTresholds){
			evaluations.put(voterTreshold.trim(), new HashMap<String, List<Map<Metric,Double>>>());
			for(String anomalyTreshold : anomalyTresholds){
				EvaluatorManager eManager = new EvaluatorManager(iManager.getOutputFolder(), iManager.getOutputFormat(), iManager.getScoresFile(buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1)), map, metList, anomalyTreshold.trim(), iManager.getConvergenceTime(), voterTreshold.trim(), printOutput);
				if(eManager.detectAnomalies()) {
					evaluations.get(voterTreshold.trim()).put(anomalyTreshold.trim(), eManager.getMetricsEvaluations());
				}
				nVoters.put(voterTreshold.trim(), eManager.getCheckersNumber());
				String a = Metric.getAverageMetricValue(evaluations.get(voterTreshold.trim()).get(anomalyTreshold.trim()), metric);
				double score = Double.parseDouble(a);
				if(score > bestScore) {
					bestScore = score;
					if(bestEManager != null){
						bestEManager.flush();
					} 
					bestEManager = eManager;
				} else eManager.flush();
			}
		}
		bestScore = getBestScore(evaluations, metList, anomalyTresholds);
		return new DetectorOutput(Double.isFinite(bestScore) ? bestScore : 0.0,
				getBestSetup(evaluations, metList, anomalyTresholds), metric, metList, 
				getMetricScores(evaluations, metList, anomalyTresholds), anomalyTresholds,
				nVoters, bestEManager != null ? bestEManager.getTimedEvaluations() : null, 
				evaluations, bestEManager != null ? bestEManager.getDetailedEvaluations() : null,
				bestEManager != null ? bestEManager.getAnomalyThreshold() : null,
				bestEManager != null ? bestEManager.getFailures() : null,		
				getWritableTag(), bestEManager != null ? bestEManager.getInjectionsRatio() : Double.NaN);
	}
	
	private String getMetricScores(Map<String, Map<String, List<Map<Metric, Double>>>> evaluations, Metric[] metList, String[] anomalyTresholds){
		double score;
		double bestScore = -1;
		String bVoter = null;
		String bAnT = null;
		String out = "";
		for(String voterTreshold : evaluations.keySet()){
			for(String anomalyTreshold : anomalyTresholds){
				for(Metric met : metList){
					score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(voterTreshold).get(anomalyTreshold.trim()), met));
					if(met.equals(metric)){
						if(score > bestScore) {
							bestScore = score;
							bVoter = voterTreshold;
							bAnT = anomalyTreshold;
						}
					}
				}
			}
		}
		for(Metric met : metList){
			if(bestScore >= 0)
				score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(bVoter).get(bAnT.trim()), met));
			else score = Double.NaN;
			out = out + score + ",";
		}
		return out.substring(0, out.length()-1);
	}
	
	private double getBestScore(Map<String, Map<String, List<Map<Metric, Double>>>> evaluations, Metric[] metList, String[] anomalyTresholds) {
		double score;
		double bestScore = 0;
		for(String voterTreshold : evaluations.keySet()){
			for(String anomalyTreshold : anomalyTresholds){
				for(Metric met : metList){
					score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(voterTreshold).get(anomalyTreshold.trim()), met));
					if(met.equals(metric)){
						if(score > bestScore) {
							bestScore = score;
						}
					}
				}
			}
		}
		return bestScore;
	}
	
	private String getBestSetup(Map<String, Map<String, List<Map<Metric, Double>>>> evaluations, Metric[] metList, String[] anomalyTresholds) {
		double score;
		double bestScore = 0;
		String bSetup = null;
		for(String voterTreshold : evaluations.keySet()){
			for(String anomalyTreshold : anomalyTresholds){
				for(Metric met : metList){
					score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(voterTreshold).get(anomalyTreshold.trim()), met));
					if(met.equals(metric)){
						if(score > bestScore) {
							bestScore = score;
							bSetup = voterTreshold + " - " + anomalyTreshold;
						}
					}
				}
			}
		}
		return bSetup;
	}

	public void flush() {
		iManager = null;
		selectedDataSeries = null;
	}
		
}
