/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.knowledge.data.MonitoredData;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.AppUtility;
import ippoz.multilayer.detector.commons.support.PreferencesManager;
import ippoz.multilayer.detector.loader.CSVPreLoader;
import ippoz.multilayer.detector.loader.Loader;
import ippoz.multilayer.detector.loader.MySQLLoader;
import ippoz.multilayer.detector.metric.FalsePositiveRate_Metric;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.reputation.Reputation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
	
	private List<DataSeries> selectedDataSeries;
	
	/**
	 * Instantiates a new detection manager.
	 *
	 * @param prefManager the main preference manager
	 */
	public DetectionManager(PreferencesManager prefManager){
		iManager = new InputManager(prefManager);
		metric = iManager.getMetricName();
		reputation = iManager.getReputation(metric);
		dataTypes = iManager.getDataTypes();
		algTypes = iManager.getAlgTypes();
	}
	
	private List<Loader> buildLoader(String loaderTag){
		if(iManager.getLoader() != null){
			return buildLoaderList(loaderTag, iManager.getRunIDs(loaderTag), iManager.getLoaderPreferences(), iManager.getAnomalyWindow());
		} else return new LinkedList<>();
	}
	
	private List<Loader> buildLoaderList(String loaderTag, String runsString, PreferencesManager loaderPrefManager, int anomalyWindow){
		List<Loader> lList = new LinkedList<>();
		Loader newLoader;
		LinkedList<Integer> runs;
		int nRuns;
		if(runsString != null && runsString.length() > 0){
			if(runsString.startsWith("@") && runsString.contains("(") && runsString.contains(")")){
				nRuns = Integer.parseInt(runsString.substring(runsString.indexOf('@')+1, runsString.indexOf('(')));
				runs = readRunIds(runsString.substring(runsString.indexOf('(')+1, runsString.indexOf(')')));
				for(int i=0;i<runs.size();i=i+nRuns){
					newLoader = buildSingleLoader(new LinkedList<Integer>(runs.subList(i, i+nRuns > runs.size() ? runs.size() : i+nRuns)), loaderPrefManager, loaderTag, anomalyWindow);
					if(newLoader != null)
						lList.add(newLoader);
				}
			} else {
				newLoader = buildSingleLoader(readRunIds(runsString), loaderPrefManager, loaderTag, anomalyWindow);
				if(newLoader != null)
					lList.add(newLoader);
			}
		} else AppLogger.logError(getClass(), "LoaderError", "Unable to find run preference");
		return lList;
	}
	
	private Loader buildSingleLoader(LinkedList<Integer> list, PreferencesManager loaderPrefManager, String loaderTag, int anomalyWindow){
		String loaderType = iManager.getLoader();
		if(loaderType != null && loaderType.equalsIgnoreCase("MYSQL"))
			return new MySQLLoader(list, loaderPrefManager, loaderTag, iManager.getConsideredLayers(), null);
		else if(loaderType != null && loaderType.equalsIgnoreCase("CSVALL"))
			return new CSVPreLoader(list, loaderPrefManager, loaderTag, anomalyWindow);
		else {
			AppLogger.logError(getClass(), "LoaderError", "Unable to parse loader '" + loaderType + "'");
			return null;
		} 
	}
	
	/**
	 * Check premises for the execution, such as MySQL server status.
	 *
	 * @return true, if premises are satisfied
	 */
	public boolean checkAssumptions(){
		if(iManager.getLoader().equalsIgnoreCase("MYSQL") && !AppUtility.isServerUp(3306)){
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
	public boolean needTest(){
		return iManager.getTrainingFlag();
	}
	
	public boolean needFiltering() {
		return iManager.getFilteringFlag();
	}

	/**
	 * Starts the train process.
	 */
	public void filterIndicators(){
		FilterManager fManager;
		try {
			if(needFiltering()) {
				fManager = new FilterManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), buildLoader("filter").get(0).fetch(), iManager.loadConfigurations(algTypes), new FalsePositiveRate_Metric(true), reputation, dataTypes, algTypes, iManager.getFilteringTreshold());
				selectedDataSeries = fManager.filter();
				fManager.flush();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to filter indicators");
		}
	}	
	
	/**
	 * Starts the train process.
	 */
	public void train(){
		TrainerManager tManager;
		try {
			if(needTest()) {
				if(selectedDataSeries == null && !new File(iManager.getScoresFolder() + "filtered.csv").exists())
					tManager = new TrainerManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), iManager.getOutputFolder(), buildLoader("train").iterator().next().fetch(), iManager.loadConfigurations(algTypes), metric, reputation, dataTypes, algTypes);
				else {
					if(selectedDataSeries == null){
						tManager = new TrainerManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), iManager.getOutputFolder(), buildLoader("train").iterator().next().fetch(), iManager.loadConfigurations(algTypes), metric, reputation, dataTypes, algTypes, loadSelectedDataSeriesString());
					} else tManager = new TrainerManager(iManager.getSetupFolder(), iManager.getDataSeriesDomain(), iManager.getScoresFolder(), iManager.getOutputFolder(), buildLoader("train").iterator().next().fetch(), iManager.loadConfigurations(algTypes), metric, reputation, algTypes, selectedDataSeries); 
				}
				tManager.train();
				tManager.flush();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to train detector");
		}
	}

	private String[] loadSelectedDataSeriesString() {
		LinkedList<String> sSeries = new LinkedList<String>();
		String readed;
		BufferedReader reader = null;
		File dsF = new File(iManager.getScoresFolder() + "filtered.csv");
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
	 */
	public void evaluate(){
		Metric[] metList = iManager.loadValidationMetrics();
		boolean printOutput = iManager.getOutputVisibility();
		List<Loader> lList = buildLoader("validation");
		List<MonitoredData> bestExpList = null;
		List<MonitoredData> expList;
		String bestRuns = null;
		double bestScore = 0;
		double score;
		int index = 0;
		try {
			if(lList.size() > 1){
				for(Loader l : lList){
					expList = l.fetch();
					AppLogger.logInfo(getClass(), "[" + (++index) + "/" + lList.size() + "] Evaluating " + expList.size() + " runs (" + l.getRuns() + ")");
					score = singleEvaluation(metList, expList, printOutput, false);
					if(score > bestScore){
						bestRuns = l.getRuns();
						bestExpList = expList;
						bestScore = score;
					}
					AppLogger.logInfo(getClass(), "Score is " + new DecimalFormat("#.##").format(score) + ", best is " + new DecimalFormat("#.##").format(bestScore));
				}
				singleEvaluation(metList, bestExpList, printOutput, true);
			} else {
				bestRuns = "all";
				bestExpList = lList.iterator().next().fetch();
				bestScore = singleEvaluation(metList, bestExpList, printOutput, true);
			}	
			AppLogger.logInfo(getClass(), "Final score is " + new DecimalFormat("#.##").format(bestScore) + ", runs (" + bestRuns + ")");
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate detector");
		}
	}
	
	private double singleEvaluation(Metric[] metList, List<MonitoredData> expList, boolean printOutput, boolean summaryFlag){
		EvaluatorManager eManager;
		double bestScore;
		String[] anomalyTresholds = iManager.parseAnomalyTresholds();
		String[] voterTresholds = iManager.parseVoterTresholds();
		Map<String, Integer> nVoters = new HashMap<String, Integer>();
		Map<String, Map<String, List<Map<Metric, Double>>>> evaluations = new HashMap<String, Map<String, List<Map<Metric,Double>>>>();
		for(String voterTreshold : voterTresholds){
			evaluations.put(voterTreshold.trim(), new HashMap<String, List<Map<Metric,Double>>>());
			for(String anomalyTreshold : anomalyTresholds){
				eManager = new EvaluatorManager(iManager.getOutputFolder(), iManager.getOutputFormat(), iManager.getScoresFile(), expList, expList.get(0).getIndicators(), metList, anomalyTreshold.trim(), iManager.getConvergenceTime(), voterTreshold.trim(), printOutput);
				if(eManager.detectAnomalies()) {
					evaluations.get(voterTreshold.trim()).put(anomalyTreshold.trim(), eManager.getMetricsEvaluations());
					//eManager.printTimings(iManager.getOutputFolder() + "evaluationTimings.csv");
				}
				nVoters.put(voterTreshold.trim(), eManager.getCheckersNumber());
				eManager.flush();
			}
		}
		bestScore = getBestScore(evaluations, metList, anomalyTresholds);
		if(summaryFlag) {
			summarizeEvaluations(evaluations, metList, iManager.parseAnomalyTresholds(), nVoters, bestScore);
		}
		return Double.isFinite(bestScore) ? bestScore : 0.0;
	}
	
	private double getBestScore(Map<String, Map<String, List<Map<Metric, Double>>>> evaluations, Metric[] metList, String[] anomalyTresholds) {
		double score;
		double bestScore = 0;
		for(String voterTreshold : evaluations.keySet()){
			for(String anomalyTreshold : anomalyTresholds){
				for(Metric met : metList){
					score = Double.parseDouble(getAverageMetricValue(evaluations.get(voterTreshold).get(anomalyTreshold.trim()), met));
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

	private void summarizeEvaluations(Map<String, Map<String, List<Map<Metric, Double>>>> evaluations, Metric[] metList, String[] anomalyTresholds, Map<String, Integer> nVoters, double bestScore) {
		BufferedWriter writer;
		BufferedWriter compactWriter;
		double score;
		try {
			compactWriter = new BufferedWriter(new FileWriter(new File(iManager.getOutputFolder() + "tableSummary.csv")));
			writer = new BufferedWriter(new FileWriter(new File(iManager.getOutputFolder() + "summary.csv")));
			compactWriter.write("selection_strategy,checkers,");
			for(String anomalyTreshold : anomalyTresholds){
				compactWriter.write(anomalyTreshold + ",");
			}
			compactWriter.write("\n");
			writer.write("voter,anomaly,checkers,");
			for(Metric met : metList){
				writer.write(met.getMetricName() + ",");
			}
			writer.write("\n");
			for(String voterTreshold : evaluations.keySet()){
				compactWriter.write(voterTreshold + "," + nVoters.get(voterTreshold.trim()) + ",");
				for(String anomalyTreshold : anomalyTresholds){
					writer.write(voterTreshold + "," + anomalyTreshold.trim() + "," + nVoters.get(voterTreshold.trim()) + ",");
					for(Metric met : metList){
						score = Double.parseDouble(getAverageMetricValue(evaluations.get(voterTreshold).get(anomalyTreshold.trim()), met));
						if(met.equals(metric)){
							compactWriter.write(score + ",");
						}
						writer.write(score + ",");
					}
					writer.write("\n");
				}
				compactWriter.write("\n");
			}
			compactWriter.close();
			writer.close();
			AppLogger.logInfo(getClass(), "Best score obtained is '" + bestScore + "'");
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write summary files");
		}
	}

	private String getAverageMetricValue(List<Map<Metric, Double>> list, Metric met) {
		List<Double> dataList = new ArrayList<Double>();
		if(list != null){
			for(Map<Metric, Double> map : list){
				dataList.add(map.get(met));
			}
			return String.valueOf(AppUtility.calcAvg(dataList));
		} else return String.valueOf(Double.NaN);
	}
	
	/**
	 * Returns run IDs parsing a specific tag.
	 *
	 * @param runTag the run tag
	 * @return the list of IDs
	 */
	private LinkedList<Integer> readRunIds(String idPref){
		String from, to;
		LinkedList<Integer> idList = new LinkedList<Integer>();
		if(idPref != null && idPref.length() > 0){
			for(String id : idPref.split(",")){
				if(id.contains("-")){
					from = id.split("-")[0].trim();
					to = id.split("-")[1].trim();
					for(int i=Integer.parseInt(from);i<=Integer.parseInt(to);i++){
						idList.add(i);
					}
				} else idList.add(Integer.parseInt(id.trim()));
			}
		}
		return idList;
	}
		
}
