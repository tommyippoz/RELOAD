/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.datacategory.DataCategory;
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
import ippoz.reload.executable.DetectorMain;
import ippoz.reload.info.FeatureSelectionInfo;
import ippoz.reload.info.TrainInfo;
import ippoz.reload.info.ValidationInfo;
import ippoz.reload.loader.Loader;
import ippoz.reload.manager.evaluate.EvaluatorManager;
import ippoz.reload.manager.evaluate.OptimizatorManager;
import ippoz.reload.manager.evaluate.ValidationManager;
import ippoz.reload.manager.train.TrainerManager;
import ippoz.reload.metric.Metric;
import ippoz.reload.output.DetectorOutput;
import ippoz.reload.reputation.Reputation;
import ippoz.reload.voter.ScoresVoter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
	protected InputManager iManager;
	
	/** The chosen metric. */
	protected Metric metric;
	
	/** The chosen reputation metric. */
	protected Reputation reputation;
	
	/** The used data types (plain, diff...). */
	protected DataCategory[] dataTypes;
	
	/** The algorithm types (SPS, Historical...). */
	protected List<AlgorithmType> algTypes;
	
	protected PreferencesManager loaderPref;
	
	protected Integer windowSize;
	
	protected SlidingPolicy sPolicy;
	
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
		if(new File(getDetectorOutputFolder()).exists()){
			for(File f : new File(getDetectorOutputFolder()).listFiles()){
				if(f.isFile())
					f.delete();
			}
		}
	}
	
	public String getDetectorOutputFolder(){
		 return iManager.getOutputFolder() + loaderPref.getCompactFilename() + algTypes.toString().replace(",", "");
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
	
	protected List<Loader> buildLoader(String loaderTag){
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
	public void featureSelection(){
		List<Knowledge> kList;
		FeatureSelectorManager fsm;
		String scoresFolderName;
		List<Loader> loaders;
		try {
			if(needFiltering()) {
				scoresFolderName = iManager.getScoresFolder() + buildOutFilePrequel() + File.separatorChar;
				if(!new File(scoresFolderName).exists())
					new File(scoresFolderName).mkdirs();
				loaders = buildLoader("train");
				kList = Knowledge.generateKnowledge(loaders.get(0).fetch(), KnowledgeType.SINGLE, null, 0);
				fsm = new FeatureSelectorManager(iManager.getFeatureSelectors(), dataTypes);
				fsm.selectFeatures(kList, scoresFolderName, loaderPref.getFilename());
				fsm.addLoaderInfo(loaders.get(0));
				fsm.saveSelectedFeatures(scoresFolderName, buildOutFilePrequel() + "_filtered.csv");
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
		Map<KnowledgeType, List<Knowledge>> kMap;
		List<Loader> loaders;
		try {
			if(needTraining()) {
				loaders = buildLoader("train");
				kMap = generateKnowledge(loaders.iterator().next().fetch());
				scoresFolderName = iManager.getScoresFolder() + buildOutFilePrequel();
				if(!new File(scoresFolderName).exists())
					new File(scoresFolderName).mkdirs();
				if(!iManager.filteringResultExists(loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.')))){
					iManager.generateDataSeries(kMap, iManager.getScoresFolder() + buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_filtered.csv");
				}
				tManager = new TrainerManager(iManager.getSetupFolder(), 
						iManager.getDatasetsFolder(), 
						iManager.getDataSeriesDomain(), 
						iManager.getScoresFolder(), 
						loaderPref.getCompactFilename(), 
						iManager.getOutputFolder(), 
						kMap, 
						iManager.loadConfigurations(algTypes, windowSize, sPolicy, true), 
						metric, 
						reputation, 
						dataTypes, 
						algTypes, 
						iManager.loadSelectedDataSeriesString(iManager.getScoresFolder(), buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel()), 
						iManager.getKFoldCounter());
				tManager.addLoaderInfo(loaders.get(0));
				tManager.train(scoresFolderName + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1), getMetaLearningCSV());
				tManager.flush();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to train detector");
		}
	}
	
	protected Map<KnowledgeType, List<Knowledge>> generateKnowledge(List<MonitoredData> expList) {
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
	public DetectorOutput optimizeVoting(){
		Metric[] metList = iManager.loadValidationMetrics();
		boolean printOutput = iManager.getOutputVisibility();
		List<Loader> lList = buildLoader("train");
		List<MonitoredData> bestExpList = null;
		List<MonitoredData> expList;
		Loader bestLoader = null;
		DetectorOutput dOut = null;
		String bestRuns = null;
		double bestScore = Double.NaN;
		double score;
		int index = 0;
		String scoresFileString = buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1);
		try {
			if(iManager.countAvailableModels(scoresFileString) > 0){
				if(lList.size() > 1){
					for(Loader l : lList){
						expList = l.fetch();
						if(expList != null && expList.size() > 0){
							AppLogger.logInfo(getClass(), "[" + (++index) + "/" + lList.size() + "] Evaluating " + expList.size() + " runs (" + l.getRuns() + ")");
							score = singleOptimization(l, metList, generateKnowledge(expList), printOutput, false).getBestScore();
							if(metric.compareResults(score, bestScore) > 0){
								bestRuns = l.getRuns();
								bestExpList = expList;
								bestScore = score;
								bestLoader = l;
							}
							AppLogger.logInfo(getClass(), "Score is " + new DecimalFormat("#.##").format(score) + ", best is " + new DecimalFormat("#.##").format(bestScore));
						} else AppLogger.logError(getClass(), "NoSuchDataError", "Unable to fetch train data");
					}
					dOut = singleOptimization(bestLoader, metList, generateKnowledge(bestExpList), printOutput, false);
					dOut.setBestRuns(bestRuns);
				} else {
					Loader l = lList.iterator().next();
					bestExpList = l.fetch();
					dOut = singleOptimization(l, metList, generateKnowledge(bestExpList), printOutput, false);
					dOut.setBestRuns(l.getRuns());
				}	
				AppLogger.logInfo(getClass(), "Voting score is " + new DecimalFormat("#.##").format(dOut.getBestScore()) + ", runs (" + dOut.getBestRuns() + ")");
			} else {
				AppLogger.logError(getClass(), "NoVotersFound", "Unable to gather train results. You may want to try a different training set which includes i) normal data, and ii) some anomalies that RELOAD can use to tune algorithms' parameters.");
				return null;
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate detector");
		}
		return dOut;
	}
	
	private DetectorOutput singleOptimization(Loader l, Metric[] metList, Map<KnowledgeType, List<Knowledge>> map, boolean printOutput, boolean generateValidationInfo){
		List<Knowledge> expKnowledge = null;
		EvaluatorManager bestEManager = null;
		double bestScore = Double.NaN;
		List<ScoresVoter> voterList = iManager.getScoresVoters(buildOutFilePrequel());
		Map<ScoresVoter, Integer> nVoters = new HashMap<ScoresVoter, Integer>();
		Map<ScoresVoter, List<Map<Metric, Double>>> evaluations = new HashMap<ScoresVoter, List<Map<Metric,Double>>>();
		String scoresFileString = buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1);
		if(iManager.countAvailableModels(scoresFileString) > 0){
			for(ScoresVoter voter : voterList){
				EvaluatorManager eManager;
				if(generateValidationInfo)
					eManager = new ValidationManager(voter, getDetectorOutputFolder(), iManager.getScoresFile(scoresFileString), map, metList, printOutput);
				else eManager = new OptimizatorManager(voter, getDetectorOutputFolder(), iManager.getScoresFile(scoresFileString), map, metList, printOutput);
				if(expKnowledge == null)
					expKnowledge = eManager.getKnowledge();
				if(eManager.detectAnomalies()) {
					evaluations.put(voter, eManager.getMetricsValues());
				} else evaluations.put(voter, new LinkedList<Map<Metric,Double>>());
				nVoters.put(voter, eManager.getCheckersNumber());
				double score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(voter), metric));
				if(metric.compareResults(score, bestScore) > 0) {
					bestScore = score;
					if(bestEManager != null){
						bestEManager.flush();
					} 
					bestEManager = eManager;
				} else eManager.flush();
			}
			bestScore = getBestScore(evaluations, metList, voterList);
			if(voterList == null || voterList.size() == 0)
				bestScore = 0;
			if(generateValidationInfo && bestEManager != null){
				ValidationInfo vInfo = new ValidationInfo();
				vInfo.setRuns(l.getRuns());
				vInfo.setVoter(bestEManager.getVoter().toString());
				vInfo.setFaultRatio(bestEManager.getInjectionsRatio());
				vInfo.setModels(bestEManager.getModels());
				vInfo.setBestScore(bestScore);
				vInfo.setMetricsString(bestEManager.getMetricsString());
				vInfo.setSeriesString(iManager.getSelectedSeries(iManager.getScoresFolder(), buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel()));
				vInfo.printFile(new File(getDetectorOutputFolder() + File.separatorChar + "validationInfo.info"));
			}
			return new DetectorOutput(iManager, algTypes, Double.isFinite(bestScore) ? bestScore : 0.0,
					getBestSetup(evaluations, metList, voterList), iManager.loadAlgorithmModels(buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1)),
					bestEManager != null ? bestEManager.getVotingEvaluations() : null, l,
					bestEManager != null ? bestEManager.getDetailedEvaluations() : null,
					iManager.getSelectedSeries(iManager.getScoresFolder(), buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel()), 
					iManager.extractSelectedFeatures(iManager.getScoresFolder(), buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel(), loaderPref.getFilename()),		
					getWritableTag(),
					bestEManager.getInjectionsRatio());
					
		} else {
			AppLogger.logError(getClass(), "NoVotersFound", "Unable to gather train results. You may want to try a different training set which includes i) normal data, and ii) some anomalies that RELOAD can use to tune algorithms' parameters.");
			return null;
		}
	}
	
/*	private String getMetricScores(Map<ScoresVoter, List<Map<Metric, Double>>> evaluations, Metric[] metList, ScoresVoter voter){
		List<ScoresVoter> list = new LinkedList<ScoresVoter>();
		list.add(voter);
		return getMetricScores(evaluations, metList, list);
	}
	
	private String getMetricScores(Map<ScoresVoter, List<Map<Metric, Double>>> evaluations, Metric[] metList, List<ScoresVoter> voterList){
		double score;
		double bestScore = Double.NEGATIVE_INFINITY;
		ScoresVoter bVoter = null;
		String out = "";
		for(ScoresVoter voter : voterList){
			for(Metric met : metList){
				score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(voter), met));
				if(met.equals(metric)){
					if(score > bestScore) {
						bestScore = score;
						bVoter = voter;
					}
				}
			}
		}
		for(Metric met : metList){
			score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(bVoter), met));
			out = out + score + ",";
		}
		return out.substring(0, out.length()-1);
	} */
	
	private double getBestScore(Map<ScoresVoter, List<Map<Metric, Double>>> evaluations, Metric[] metList, List<ScoresVoter> voterList) {
		double score;
		double bestScore = Double.NaN;
		for(ScoresVoter voter : voterList){
			for(Metric met : metList){
				score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(voter), met));
				if(met.equals(metric)){
					if(metric.compareResults(score, bestScore) > 0) {
						bestScore = score;
					}
				}
			}
		}
		return bestScore;
	}
	
	private ScoresVoter getBestSetup(Map<ScoresVoter, List<Map<Metric, Double>>> evaluations, Metric[] metList, ScoresVoter voter){
		List<ScoresVoter> list = new LinkedList<ScoresVoter>();
		list.add(voter);
		return getBestSetup(evaluations, metList, list);
	}
	
	private ScoresVoter getBestSetup(Map<ScoresVoter, List<Map<Metric, Double>>> evaluations, Metric[] metList, List<ScoresVoter> voterList) {
		double score;
		double bestScore = Double.NaN;
		ScoresVoter bSetup = null;
		for(ScoresVoter voter : voterList){
			for(Metric met : metList){
				score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(voter), met));
				if(met.equals(metric)){
					if(metric.compareResults(score, bestScore) > 0) {
						bestScore = score;
						bSetup = voter;
					}
				}				
			}
		}
		return bSetup;
	}

	public void flush() {
		iManager = null;
	}

	public DetectorOutput evaluate(DetectorOutput optOut) {
		String scoresFileString = buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1);
		if(iManager.countAvailableModels(scoresFileString) > 0){
			if(optOut == null || optOut.getVoter() == null){
				return evaluateAll();
			} else {
				return optimizedEvaluation(optOut.getVoter());
			}
		} else {
			AppLogger.logError(getClass(), "NoVotersFound", "Unable to gather voters as result of train phase.");
			return null;
		}
	}
	
	private DetectorOutput optimizedEvaluation(ScoresVoter bestVoter){
		Metric[] metList = iManager.loadValidationMetrics();
		boolean printOutput = iManager.getOutputVisibility();
		List<Loader> lList = buildLoader("validation");
		List<MonitoredData> expList;
		DetectorOutput dOut = null;
		try {
			if(bestVoter != null) {
				if(lList.size() > 1)
					AppLogger.logError(getClass(), "TooManyLoaders", "Too many validation loaders. Evaluating just the first.");
				Loader l = lList.iterator().next();
				expList = l.fetch();
				if(expList != null && expList.size() > 0){
					dOut = singleEvaluation(l, metList, generateKnowledge(expList), bestVoter, printOutput);
					dOut.setBestRuns(l.getRuns());	
					//dOut.printDetailedKnowledgeScores(iManager.getOutputFolder());
					AppLogger.logInfo(getClass(), "Final Evaluated score is " + new DecimalFormat("#.##").format(dOut.getBestScore()) + ", runs (" + dOut.getBestRuns() + ")");
				} else AppLogger.logError(getClass(), "NoSuchDataError", "Unable to fetch validation data");
			} else AppLogger.logError(getClass(), "WrongEvaluationSetup", "Unable to apply '" + bestVoter + "' results voter");
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate detector");
		}
		return dOut;
	}
	
	public DetectorOutput singleEvaluation(Loader l, Metric[] metList, Map<KnowledgeType, List<Knowledge>> map, ScoresVoter voter, boolean printOutput){
		ValidationInfo vInfo = new ValidationInfo();
		Map<ScoresVoter, Integer> nVoters = new HashMap<ScoresVoter, Integer>();
		Map<ScoresVoter, List<Map<Metric, Double>>> evaluations = new HashMap<ScoresVoter, List<Map<Metric,Double>>>();
		vInfo.setRuns(l.getRuns());
		vInfo.setVoter(voter.toString());
		EvaluatorManager eManager = new ValidationManager(voter, getDetectorOutputFolder(), iManager.getScoresFile(buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1)), map, metList, printOutput);
		vInfo.setFaultRatio(eManager.getInjectionsRatio());
		vInfo.setModels(eManager.getModels());
		if(eManager.detectAnomalies()) {
			evaluations.put(voter, eManager.getMetricsValues());
		} else evaluations.put(voter, new LinkedList<Map<Metric,Double>>());
		nVoters.put(voter, eManager.getCheckersNumber());
		double score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(voter), metric));
		vInfo.setBestScore(score);
		vInfo.setMetricsString(eManager.getMetricsString());
		vInfo.setSeriesString(iManager.getSelectedSeries(iManager.getScoresFolder(), buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel()));
		if(printOutput){
			vInfo.printFile(new File(getDetectorOutputFolder() + File.separatorChar + "validationInfo.info"));
		}
		return new DetectorOutput(
				iManager, algTypes,
				Double.isFinite(score) ? score : 0.0,
				getBestSetup(evaluations, metList, voter), 
				iManager.loadAlgorithmModels(buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1)),
				eManager.getVotingEvaluations(), 
				l, 
				eManager.getDetailedEvaluations(), 
				iManager.getSelectedSeries(iManager.getScoresFolder(), buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel()), 
				iManager.extractSelectedFeatures(iManager.getScoresFolder(), buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel(), loaderPref.getFilename()),	
				getWritableTag(), 
				eManager.getInjectionsRatio());
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
		double bestScore = Double.NaN;
		double score;
		int index = 0;
		try {
			if(lList.size() > 1){
				for(Loader l : lList){
					expList = l.fetch();
					AppLogger.logInfo(getClass(), "[" + (++index) + "/" + lList.size() + "] Evaluating " + expList.size() + " runs (" + l.getRuns() + ")");
					score = singleOptimization(l, metList, generateKnowledge(expList), printOutput, false).getBestScore();
					if(metric.compareResults(score, bestScore) > 0){
						bestRuns = l.getRuns();
						bestExpList = expList;
						bestScore = score;
						bestLoader = l;
					}
					AppLogger.logInfo(getClass(), "Score is " + new DecimalFormat("#.##").format(score) + ", best is " + new DecimalFormat("#.##").format(bestScore));
				}
				dOut = singleOptimization(bestLoader, metList, generateKnowledge(bestExpList), printOutput, true);
				dOut.setBestRuns(bestRuns);
			} else {
				Loader l = lList.iterator().next();
				bestExpList = l.fetch();
				dOut = singleOptimization(l, metList, generateKnowledge(bestExpList), printOutput, true);
				dOut.setBestRuns(l.getRuns());
			}				
			AppLogger.logInfo(getClass(), "Final Validation score is " + new DecimalFormat("#.##").format(dOut.getBestScore()) + ", runs (" + dOut.getBestRuns() + ")");
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate detector");
		}
		return dOut;
	}

	public boolean hasMetaLearning() {
		return iManager.hasMetaLearning();
	}
	
	public String getMetaLearningCSV(){
		return "meta" + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1).replace(" ", "").replace(",", "_") + "_trainscores.csv";
	}

	public String getModelsPath() {
		return buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1);
	}
	
	public void report(){
		File drFile = new File(DetectorMain.DEFAULT_REPORT_FILE);
		FeatureSelectionInfo fsInfo;
		TrainInfo tInfo;
		ValidationInfo vInfo;
		BufferedWriter writer;
		try {
			fsInfo = iManager.loadFeatureSelectionInfo(iManager.getScoresFolder() + buildOutFilePrequel() + File.separatorChar + "featureSelectionInfo.info");
			tInfo = iManager.loadTrainInfo(iManager.getScoresFolder() + buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1) + "_trainInfo.info");
			vInfo = iManager.loadValidationInfo(getDetectorOutputFolder() + File.separatorChar + "validationInfo.info");
			if(!drFile.exists()){
				writer = new BufferedWriter(new FileWriter(drFile, false));
				writer.write("* Report for RELOAD activity on " + new Date(System.currentTimeMillis()) + "\n");
				writer.write(FeatureSelectionInfo.getFileHeader() + ",");
				writer.write(TrainInfo.getFileHeader() + ",");
				writer.write("best_dataseries,dataset,runs,algorithm,window_size,window_policy,setup,metric_score");
				for(Metric met : iManager.loadValidationMetrics()){
					writer.write("," + met.getMetricName());
				}
				writer.write("\n");
			} else {
				writer = new BufferedWriter(new FileWriter(drFile, true));
			}
			writer.write(fsInfo.toFileString() + ",");
			writer.write(tInfo.toFileString() + ",");
			writer.write(
					vInfo.getSeriesString() + "," +
					getWritableTag() + "," + 
					vInfo.getVoter() + "," + 
					vInfo.getBestScore() + "," + 
					vInfo.getMetricsString() + "\n");
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(DetectorMain.class, ex, "Unable to report");
		}
	}
		
}
