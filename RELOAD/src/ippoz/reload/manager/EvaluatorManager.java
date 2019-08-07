/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.TimedResult;
import ippoz.reload.commons.support.TimedValue;
import ippoz.reload.metric.Metric;
import ippoz.reload.voter.AlgorithmVoter;
import ippoz.reload.voter.ExperimentVoter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Class EvaluatorManager.
 * The manager of the evaluation-scoring phase. Called after the training or when train scores are already available.
 *
 * @author Tommy
 */
public class EvaluatorManager extends DataManager {
	
	/** The output folder. */
	private String outputFolder;
	
	/** The output format. */
	private String outputFormat;
	
	/** The scores file. */
	private String scoresFile;
	
	/** The validation metrics. */
	private Metric[] validationMetrics;
	
	private List<Map<Metric, Double>> expMetricEvaluations;
	
	private List<Map<Date, Map<AlgorithmVoter, AlgorithmResult>>> detailedEvaluations;
	
	/** The anomaly threshold. Votings over that threshold raise alarms. */
	private double anomalyTreshold;
	
	/** The algorithm convergence time. */
	private double algConvergence;
	
	/** The detector score threshold. Used to filter the available anomaly checkers by score. */
	private double detectorScoreTreshold;
	
	private boolean printOutput;
	
	/**
	 * Instantiates a new evaluator manager.
	 *
	 * @param prefManager the preference manager
	 * @param pManager the timings manager
	 * @param map the experiment list
	 * @param validationMetrics the validation metrics
	 * @param anTresholdString the an threshold string
	 * @param algConvergence the algorithm convergence
	 * @param detectorScoreTreshold the detector score threshold
	 */
	public EvaluatorManager(String oFolder, String outputFormat, String scoresFile, Map<KnowledgeType, List<Knowledge>> map, Metric[] validationMetrics, String anTresholdString, double algConvergence, String voterTreshold, boolean printOutput) {
		super(map);
		this.scoresFile = scoresFile;
		this.outputFormat = outputFormat;
		this.validationMetrics = validationMetrics;
		this.algConvergence = algConvergence;
		this.printOutput = printOutput;
		detectorScoreTreshold = getVoterTreshold(voterTreshold);
		anomalyTreshold = getAnomalyVoterTreshold(anTresholdString, loadTrainScores().size());
		outputFolder = oFolder + voterTreshold + "_" + anTresholdString;
		AppLogger.logInfo(getClass(), "Evaluating " + map.get(map.keySet().iterator().next()).size() + " experiments with [" + voterTreshold + " | " + anTresholdString + "]");
	}
	
	public double getAnomalyThreshold(){
		return anomalyTreshold;
	}
	
	private double getVoterTreshold(String voterTreshold) {
		if(voterTreshold != null){
			if(AppUtility.isNumber(voterTreshold))
				return Double.parseDouble(voterTreshold);
			else if(voterTreshold.contains("BEST")){
				return Double.parseDouble(voterTreshold.substring(voterTreshold.indexOf("T")+1).trim());
			} else if(voterTreshold.contains("FILTERED")){
				return -1.0*Double.parseDouble(voterTreshold.substring(voterTreshold.indexOf("D")+1).trim());
			}
		}
		return Double.NaN;
	}

	/**
	 * Detects anomalies.
	 * This is the core of the evaluation, which ends in the anomaly evaluation of each snapshot of each experiment.
	 * @return 
	 */
	public boolean detectAnomalies(){
		long start = System.currentTimeMillis();
		try {
			start();
			join();
			if(getThreadList().size() > 0) {
				AppLogger.logInfo(getClass(), "Detection executed in " + (System.currentTimeMillis() - start) + " ms");
			} else AppLogger.logInfo(getClass(), "Detection not executed");
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to complete evaluation phase");
		}
		return getThreadList().size() > 0;
	}
	
	/**
	 * Gets the anomaly voter threshold.
	 *
	 * @param anTresholdString the anomaly threshold string read from preferences
	 * @param checkers the number of selected checkers
	 * @return the anomaly voter threshold
	 */
	private double getAnomalyVoterTreshold(String anTresholdString, int checkers){
		switch(anTresholdString){
			case "ALL":
				return checkers;
			case "HALF":
				return Math.ceil(checkers/2.0);
			case "THIRD":
				return Math.ceil(checkers/3.0);
			case "QUARTER":
				return Math.ceil(checkers/4.0);
			default:
				return Double.parseDouble(anTresholdString);
		}
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#initRun()
	 */
	@Override
	protected void initRun() {
		List<AlgorithmVoter> algVoters = loadTrainScores();
		List<ExperimentVoter> voterList = new ArrayList<ExperimentVoter>(experimentsSize());
		Map<KnowledgeType, Knowledge> redKMap;
		expMetricEvaluations = new ArrayList<Map<Metric,Double>>(voterList.size());
		detailedEvaluations = new ArrayList<Map<Date, Map<AlgorithmVoter, AlgorithmResult>>>(voterList.size());
		if(printOutput){
			setupResultsFile();
		}
		if(algVoters != null && algVoters.size() > 0){
			for(int expN = 0; expN < experimentsSize(); expN++){ 
				redKMap = new HashMap<KnowledgeType, Knowledge>();
				for(KnowledgeType kType : getKnowledgeTypes()){
					redKMap.put(kType, getKnowledge(kType).get(expN));
				}
				voterList.add(new ExperimentVoter(algVoters, redKMap));
			}
		}
		setThreadList(voterList);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#threadStart(java.lang.Thread, int)
	 */
	@Override
	protected void threadStart(Thread t, int tIndex) {
		//AppLogger.logInfo(getClass(), "Evaluating experiment " + tIndex + "/" + threadNumber());
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#threadComplete(java.lang.Thread, int)
	 */
	@Override
	protected void threadComplete(Thread t, int tIndex) {
		ExperimentVoter ev = (ExperimentVoter)t;
		if(ev.getFailuresNumber() > 0){
			expMetricEvaluations.add(ev.printVoting(outputFormat, outputFolder, validationMetrics, anomalyTreshold, algConvergence, printOutput));
			detailedEvaluations.add(ev.getSingleAlgorithmScores());
		} else {
			expMetricEvaluations.add(null);
			detailedEvaluations.add(null);
		}
	}
	
	/**
	 * Loads train scores.
	 * This is the outcome of some previous training phases.
	 *
	 * @return the list of AlgorithmVoters resulting from the read scores
	 */
	private LinkedList<AlgorithmVoter> loadTrainScores() {
		File asFile = new File(scoresFile);
		BufferedReader reader;
		AlgorithmConfiguration conf;
		String[] splitted;
		LinkedList<AlgorithmVoter> voterList = new LinkedList<AlgorithmVoter>();
		String readed;
		String seriesString;
		try {
			if(asFile.exists()){
				reader = new BufferedReader(new FileReader(asFile));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && readed.indexOf("§") != -1){
							splitted = readed.split("§");
							if(splitted.length > 3 && checkAnomalyTreshold(Double.valueOf(splitted[3]), voterList.size())){
								conf = AlgorithmConfiguration.buildConfiguration(AlgorithmType.valueOf(splitted[1]), (splitted.length > 5 ? splitted[5] : null));
								switch(AlgorithmType.valueOf(splitted[1])){
									case RCC:
									case PEA:
										seriesString = null;
										break;
									default:
										seriesString = splitted[0];
										break;
								}
								if(conf != null){
									conf.addItem(AlgorithmConfiguration.WEIGHT, splitted[2]);
									conf.addItem(AlgorithmConfiguration.AVG_SCORE, splitted[3]);
									conf.addItem(AlgorithmConfiguration.STD_SCORE, splitted[4]);
								}
								addVoter(new AlgorithmVoter(DetectionAlgorithm.buildAlgorithm(conf.getAlgorithmType(), DataSeries.fromString(seriesString, conf.getAlgorithmType() != AlgorithmType.INV), conf), Double.parseDouble(splitted[3]), Double.parseDouble(splitted[2])), voterList);
							}
						}
					}
				}
				reader.close();
			} else AppLogger.logError(getClass(), "FileNotFound", "Unable to find '" + scoresFile + "'");
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read scores");
		}
		return voterList;
	}
	
	private void addVoter(AlgorithmVoter newVoter, LinkedList<AlgorithmVoter> voterList){
		boolean found = false;
		if(detectorScoreTreshold >= 0)
			voterList.add(newVoter);
		else {
			for(AlgorithmVoter aVoter : voterList){
				if(aVoter.usesSeries(newVoter.getDataSeries())){
					found = true;
					break;
				}
			}
			if(!found)
				voterList.add(newVoter);
		}
	}
	
	private boolean checkAnomalyTreshold(Double newMetricValue, int nVoters) {
		if(Math.abs(detectorScoreTreshold) >= 1)
			return nVoters < Math.abs(detectorScoreTreshold);
		else return newMetricValue >= detectorScoreTreshold;
	}

	/**
	 * Setup results file.
	 */
	private void setupResultsFile() {
		File resultsFile;
		PrintWriter pw;
		try {
			resultsFile = new File(outputFolder + "/results.csv");
			if(!new File(outputFolder).exists())
				new File(outputFolder).mkdirs();
			else if(resultsFile.exists())
				resultsFile.delete();
			pw = new PrintWriter(new FileOutputStream(resultsFile, true));
			pw.append("exp_name,exp_obs,");
			for(Metric met : validationMetrics){
				pw.append(met.getMetricName() + ",");
			}
			pw.close();
		} catch (FileNotFoundException ex) {
			AppLogger.logException(getClass(), ex, "Unable to find results file");
		} 		
	}
	
	public Map<String, List<TimedResult>> getTimedEvaluations() {
		Map<String, List<TimedResult>> outMap = new TreeMap<String, List<TimedResult>>();
		for(Thread t : getThreadList()){
			ExperimentVoter ev = (ExperimentVoter)t;
			outMap.put(ev.getExperimentName(), ev.getExperimentVoting());
		}
		return outMap;
	}
	
	public List<Map<Metric, Double>> getMetricsEvaluations() {
		return expMetricEvaluations;
	}
	
	public Map<String, List<Map<AlgorithmVoter, AlgorithmResult>>> getDetailedEvaluations() {
		Map<String, List<Map<AlgorithmVoter, AlgorithmResult>>> outMap = new TreeMap<String, List<Map<AlgorithmVoter, AlgorithmResult>>>();
		if(detailedEvaluations != null && detailedEvaluations.size() > 0){
			for(int i=0;i<getThreadList().size();i++){
				ExperimentVoter ev = (ExperimentVoter)getThreadList().get(i);
				outMap.put(ev.getExperimentName(), new LinkedList<Map<AlgorithmVoter, AlgorithmResult>>());
				if(i < detailedEvaluations.size()){
					Map<Date,Map<AlgorithmVoter,AlgorithmResult>> map = detailedEvaluations.get(i);
					if(map != null){
						for(Date mapEntry : map.keySet()){
							outMap.get(ev.getExperimentName()).add(map.get(mapEntry));
						}
					} 
				}
			}
		}
		return outMap;
	}
	
	public Map<String, List<InjectedElement>> getFailures(){
		Map<String, List<InjectedElement>> outMap = new TreeMap<String, List<InjectedElement>>();
		for(int i=0;i<getThreadList().size();i++){
			ExperimentVoter ev = (ExperimentVoter)getThreadList().get(i);
			outMap.put(ev.getExperimentName(), ev.getFailuresList());
		}
		return outMap;
	}
	
	public Integer getCheckersNumber() {
		return loadTrainScores().size();
	}	

}
