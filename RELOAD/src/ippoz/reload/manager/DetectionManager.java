/**
 * 
 */
package ippoz.reload.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.GlobalKnowledge;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.knowledge.SingleKnowledge;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.knowledge.sliding.SlidingPolicy;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;
import ippoz.reload.loader.Loader;
import ippoz.reload.metric.Metric;
import ippoz.reload.output.DetectorOutput;
import ippoz.reload.reputation.Reputation;

import java.io.File;
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
		metric = iManager.getTargetMetric();
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
		String runsString = loaderPref.getPreference(loaderTag.equals("validation") ? Loader.VALIDATION_RUN_PREFERENCE : Loader.TRAIN_RUN_PREFERENCE);
		if(runsString != null && runsString.length() > 0){
			if(runsString.startsWith("@") && runsString.contains("(") && runsString.contains(")")){
				nRuns = Integer.parseInt(runsString.substring(runsString.indexOf('@')+1, runsString.indexOf('(')));
				runs = iManager.readRunIds(runsString.substring(runsString.indexOf('(')+1, runsString.indexOf(')')));
				for(int i=0;i<runs.size();i=i+nRuns){
					newLoader = iManager.buildSingleLoader(loaderPref, new LinkedList<Integer>(runs.subList(i, i+nRuns > runs.size() ? runs.size() : i+nRuns)), loaderTag, anomalyWindow);
					if(newLoader != null){
						lList.add(newLoader);
					}
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
		return iManager.getFilteringFlag();
	}
	
	public boolean needOptimization() {
		return iManager.getOptimizationFlag();
	}
	
	public boolean needEvaluation() {
		return iManager.getEvaluationFlag();
	}

	/**
	 * Starts the train process.
	 */
	public void filterIndicators(){
		List<Knowledge> kList;
		FeatureSelectorManager fsm;
		String scoresFolderName;
		try {
			if(needFiltering()) {
				scoresFolderName = iManager.getScoresFolder() + buildOutFilePrequel() + File.separatorChar;
				if(!new File(scoresFolderName).exists())
					new File(scoresFolderName).mkdirs();
				kList = Knowledge.generateKnowledge(buildLoader("train").get(0).fetch(), KnowledgeType.SINGLE, null, 0);
				fsm = new FeatureSelectorManager(iManager.getFeatureSelectors(), dataTypes);
				fsm.selectFeatures(kList, scoresFolderName, loaderPref.getFilename());
				fsm.combineSelectedFeatures(kList, iManager.getDataSeriesDomain(), scoresFolderName);
				fsm.finalizeSelection(iManager.getDataSeriesDomain());
				fsm.saveFilteredSeries(scoresFolderName, buildOutFilePrequel() + "_filtered.csv");
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
		String scoresFolderName;
		try {
			if(needTraining()) {
				scoresFolderName = iManager.getScoresFolder() + buildOutFilePrequel();
				if(!new File(scoresFolderName).exists())
					new File(scoresFolderName).mkdirs();
				if(selectedDataSeries == null && !iManager.filteringResultExists(loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.'))))
					tManager = new TrainerManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), iManager.getOutputFolder(), generateKnowledge(buildLoader("train").iterator().next().fetch()), iManager.loadConfigurations(algTypes, windowSize, sPolicy), metric, reputation, dataTypes, algTypes, iManager.getSimplePearsonThreshold(), iManager.getComplexPearsonThreshold(), iManager.getKFoldCounter());
				else {
					if(selectedDataSeries == null){
						tManager = new TrainerManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), iManager.getOutputFolder(), generateKnowledge(buildLoader("train").iterator().next().fetch()), iManager.loadConfigurations(algTypes, windowSize, sPolicy), metric, reputation, dataTypes, algTypes, iManager.loadSelectedDataSeriesString(buildOutFilePrequel()), iManager.getKFoldCounter());
					} else tManager = new TrainerManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), iManager.getOutputFolder(), generateKnowledge(buildLoader("train").iterator().next().fetch()), iManager.loadConfigurations(algTypes, windowSize, sPolicy), metric, reputation, algTypes, selectedDataSeries, iManager.getKFoldCounter()); 
				}
				tManager.train(scoresFolderName + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1));
				tManager.flush();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to train detector");
		}
	}
	
	private Map<KnowledgeType, List<Knowledge>> generateKnowledge(List<MonitoredData> expList) {
		Map<KnowledgeType, List<Knowledge>> map = new HashMap<KnowledgeType, List<Knowledge>>();
		if(expList != null && !expList.isEmpty()){
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
		}
		return map;
	}

	/**
	 * Starts the optimization process.
	 * @return 
	 */
	public DetectorOutput optimize(){
		Metric[] metList = iManager.loadValidationMetrics();
		boolean printOutput = iManager.getOutputVisibility();
		List<Loader> lList = buildLoader("train");
		List<MonitoredData> bestExpList = null;
		List<MonitoredData> expList;
		Loader bestLoader = null;
		DetectorOutput dOut = null;
		String bestRuns = null;
		double bestScore = 0;
		double score;
		int index = 0;
		String scoresFileString = buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1);
		try {
			if(iManager.countAvailableVoters(scoresFileString) > 0){
				if(lList.size() > 1){
					for(Loader l : lList){
						expList = l.fetch();
						if(expList != null && expList.size() > 0){
							AppLogger.logInfo(getClass(), "[" + (++index) + "/" + lList.size() + "] Evaluating " + expList.size() + " runs (" + l.getRuns() + ")");
							score = singleOptimization(l, metList, generateKnowledge(expList), printOutput).getBestScore();
							if(score > bestScore){
								bestRuns = l.getRuns();
								bestExpList = expList;
								bestScore = score;
								bestLoader = l;
							}
							AppLogger.logInfo(getClass(), "Score is " + new DecimalFormat("#.##").format(score) + ", best is " + new DecimalFormat("#.##").format(bestScore));
						} else AppLogger.logError(getClass(), "NoSuchDataError", "Unable to fetch train data");
					}
					dOut = singleOptimization(bestLoader, metList, generateKnowledge(bestExpList), printOutput);
					dOut.setBestRuns(bestRuns);
				} else {
					Loader l = lList.iterator().next();
					bestExpList = l.fetch();
					dOut = singleOptimization(l, metList, generateKnowledge(bestExpList), printOutput);
					dOut.setBestRuns(l.getRuns());
				}	
				dOut.summarizeCSV(iManager.getOutputFolder());
				//dOut.printDetailedKnowledgeScores(iManager.getOutputFolder());
				AppLogger.logInfo(getClass(), "Final Optimized score is " + new DecimalFormat("#.##").format(dOut.getBestScore()) + ", runs (" + dOut.getBestRuns() + ")");
			} else {
				AppLogger.logError(getClass(), "NoVotersFound", "Unable to gather voters as result of train phase.");
				return null;
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate detector");
		}
		return dOut;
	}
	
	private DetectorOutput singleOptimization(Loader l, Metric[] metList, Map<KnowledgeType, List<Knowledge>> map, boolean printOutput){
		List<Knowledge> expKnowledge = null;
		EvaluatorManager bestEManager = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		String[] anomalyTresholds = iManager.parseAnomalyTresholds();
		String[] voterTresholds = iManager.parseVoterTresholds();
		Map<String, Integer> nVoters = new HashMap<String, Integer>();
		Map<String, Map<String, List<Map<Metric, Double>>>> evaluations = new HashMap<String, Map<String, List<Map<Metric,Double>>>>();
		String scoresFileString = buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1);
		if(iManager.countAvailableVoters(scoresFileString) > 0){
			for(String voterTreshold : voterTresholds){
				evaluations.put(voterTreshold.trim(), new HashMap<String, List<Map<Metric,Double>>>());
				for(String anomalyTreshold : anomalyTresholds){
					EvaluatorManager eManager = new EvaluatorManager(iManager.getOutputFolder(), iManager.getOutputFormat(), iManager.getScoresFile(scoresFileString), map, metList, anomalyTreshold.trim(), iManager.getConvergenceTime(), voterTreshold.trim(), printOutput);
					if(expKnowledge == null)
						expKnowledge = eManager.getKnowledge();
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
			return new DetectorOutput(iManager, expKnowledge, Double.isFinite(bestScore) ? bestScore : 0.0,
					getBestSetup(evaluations, metList, anomalyTresholds), iManager.loadVoters(buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1)),
					getMetricScores(evaluations, metList, anomalyTresholds), anomalyTresholds,
					nVoters, bestEManager != null ? bestEManager.getTimedEvaluations() : null, l,
					evaluations, bestEManager != null ? bestEManager.getDetailedEvaluations() : null,
					bestEManager != null ? bestEManager.getAnomalyThreshold() : null,
					bestEManager != null ? bestEManager.getFailures() : null, iManager.extractSelectedFeatures(buildOutFilePrequel(), loaderPref.getFilename()),		
					getWritableTag(), bestEManager != null ? bestEManager.getInjectionsRatio() : Double.NaN);
		} else {
			AppLogger.logError(getClass(), "NoVotersFound", "Unable to gather voters as result of train phase.");
			return null;
		}
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
		double bestScore = Double.NEGATIVE_INFINITY;
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

	public DetectorOutput evaluate(DetectorOutput optOut) {
		String scoresFileString = buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1);
		if(iManager.countAvailableVoters(scoresFileString) > 0){
			if(optOut == null || optOut.getBestSetup() == null){
				return evaluateAll();
			} else {
				return optimizedEvaluation(optOut.getBestSetup());
			}
		} else {
			AppLogger.logError(getClass(), "NoVotersFound", "Unable to gather voters as result of train phase.");
			return null;
		}
	}
	
	private DetectorOutput optimizedEvaluation(String bestSetup){
		Metric[] metList = iManager.loadValidationMetrics();
		boolean printOutput = iManager.getOutputVisibility();
		List<Loader> lList = buildLoader("validation");
		List<MonitoredData> expList;
		DetectorOutput dOut = null;
		try {
			if(bestSetup != null && bestSetup.length() > 0 && bestSetup.contains("-")) {
				if(lList.size() > 1)
					AppLogger.logError(getClass(), "TooManyLoaders", "Too many validation loaders. Evaluating just the first.");
				Loader l = lList.iterator().next();
				expList = l.fetch();
				if(expList != null && expList.size() > 0){
					dOut = singleEvaluation(l, metList, generateKnowledge(expList), bestSetup.split("-")[1].trim(), bestSetup.split("-")[0].trim(), printOutput);
					dOut.setBestRuns(l.getRuns());	
					dOut.printDetailedKnowledgeScores(iManager.getOutputFolder());
					AppLogger.logInfo(getClass(), "Final Evaluated score is " + new DecimalFormat("#.##").format(dOut.getBestScore()) + ", runs (" + dOut.getBestRuns() + ")");
				} else AppLogger.logError(getClass(), "NoSuchDataError", "Unable to fetch validatioon data");
			} else AppLogger.logError(getClass(), "WrongEvaluationSetup", "Unable to decode '" + bestSetup + "' threhsolds");
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate detector");
		}
		return dOut;
	}
	
	public DetectorOutput singleEvaluation(Loader l, Metric[] metList, Map<KnowledgeType, List<Knowledge>> map, String anomalyThreshold, String voterThreshold, boolean printOutput){
		Map<String, Integer> nVoters = new HashMap<String, Integer>();
		Map<String, Map<String, List<Map<Metric, Double>>>> evaluations = new HashMap<String, Map<String, List<Map<Metric,Double>>>>();
		EvaluatorManager eManager = new EvaluatorManager(iManager.getOutputFolder(), iManager.getOutputFormat(), iManager.getScoresFile(buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1)), map, metList, anomalyThreshold.trim(), iManager.getConvergenceTime(), voterThreshold.trim(), printOutput);
		evaluations.put(voterThreshold.trim(), new HashMap<String, List<Map<Metric,Double>>>());
		if(eManager.detectAnomalies()) {
			evaluations.get(voterThreshold.trim()).put(anomalyThreshold.trim(), eManager.getMetricsEvaluations());
		}
		nVoters.put(voterThreshold.trim(), eManager.getCheckersNumber());
		String a = Metric.getAverageMetricValue(evaluations.get(voterThreshold.trim()).get(anomalyThreshold.trim()), metric);
		double score = Double.parseDouble(a);
		return new DetectorOutput(iManager, eManager.getKnowledge(), Double.isFinite(score) ? score : 0.0,
			getBestSetup(evaluations, metList, new String[]{anomalyThreshold}), iManager.loadVoters(buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1)),
			getMetricScores(evaluations, metList, new String[]{anomalyThreshold}), new String[]{anomalyThreshold},
			nVoters, eManager.getTimedEvaluations(), l, 
			evaluations, eManager.getDetailedEvaluations(),
			eManager.getAnomalyThreshold(), eManager.getFailures(),	iManager.extractSelectedFeatures(buildOutFilePrequel(), loaderPref.getFilename()),	
			getWritableTag(), eManager.getInjectionsRatio());
	}

	public DetectorOutput evaluateAll(){
		Metric[] metList = iManager.loadValidationMetrics();
		boolean printOutput = iManager.getOutputVisibility();
		List<Loader> lList = buildLoader("validation");
		List<MonitoredData> bestExpList = null;
		List<MonitoredData> expList;
		Loader bestLoader = null;
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
					score = singleOptimization(l, metList, generateKnowledge(expList), printOutput).getBestScore();
					if(score > bestScore){
						bestRuns = l.getRuns();
						bestExpList = expList;
						bestScore = score;
						bestLoader = l;
					}
					AppLogger.logInfo(getClass(), "Score is " + new DecimalFormat("#.##").format(score) + ", best is " + new DecimalFormat("#.##").format(bestScore));
				}
				dOut = singleOptimization(bestLoader, metList, generateKnowledge(bestExpList), printOutput);
				dOut.setBestRuns(bestRuns);
			} else {
				Loader l = lList.iterator().next();
				bestExpList = l.fetch();
				dOut = singleOptimization(l, metList, generateKnowledge(bestExpList), printOutput);
				dOut.setBestRuns(l.getRuns());
			}	
			//dOut.summarizeCSV(iManager.getOutputFolder());
			dOut.printDetailedKnowledgeScores(iManager.getOutputFolder());
			AppLogger.logInfo(getClass(), "Final Validation score is " + new DecimalFormat("#.##").format(dOut.getBestScore()) + ", runs (" + dOut.getBestRuns() + ")");
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate detector");
		}
		return dOut;
	}
		
}
