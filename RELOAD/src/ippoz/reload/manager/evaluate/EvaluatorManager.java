/**
 * 
 */
package ippoz.reload.manager.evaluate;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.evaluation.ExperimentEvaluator;
import ippoz.reload.manager.DataManager;
import ippoz.reload.manager.InputManager;
import ippoz.reload.metric.Metric;
import ippoz.reload.voter.ScoresVoter;
import ippoz.reload.voter.VotingResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The Class EvaluatorManager.
 * The manager of the evaluation-scoring phase. Called after the training or when train scores are already available.
 *
 * @author Tommy
 */
public class EvaluatorManager extends DataManager {
	
	private EvaluatorType eType;
	
	/** The output folder. */
	private String outputFolder;
	
	/** The scores file. */
	private String scoresFile;
	
	private ScoresVoter voter;
	
	/** The validation metrics. */
	private Metric[] validationMetrics;
	
	private List<Map<Metric, Double>> metricValues;
	
	private List<Map<Date, Map<AlgorithmModel, AlgorithmResult>>> detailedEvaluations;
	
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
	public EvaluatorManager(EvaluatorType eType, ScoresVoter voter, String outputFolder, String scoresFile, Map<KnowledgeType, List<Knowledge>> map, Metric[] validationMetrics, boolean printOutput) {
		super(map);
		this.eType = eType;
		this.scoresFile = scoresFile;
		this.validationMetrics = validationMetrics;
		this.printOutput = printOutput;
		this.voter = voter;
		this.outputFolder = outputFolder;
		if(!new File(outputFolder).exists())
			new File(outputFolder).mkdirs();
		AppLogger.logInfo(getClass(), "Evaluating " + map.get(map.keySet().iterator().next()).size() + " experiments with [" + voter.toString() + "]");
	}
	
	public double[] getAnomalyThresholds(){
		return voter.getThresholds();
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
			if(printOutput)
				printDetailedKnowledgeScores();
			summarizeCSV();
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to complete evaluation phase");
		}
		return getThreadList().size() > 0;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#initRun()
	 */
	@Override
	protected void initRun() {
		List<AlgorithmModel> algVoters = InputManager.loadAlgorithmModelsFor(scoresFile, voter);
		List<ExperimentEvaluator> voterList = new ArrayList<ExperimentEvaluator>(experimentsSize());
		Map<KnowledgeType, Knowledge> redKMap;
		metricValues = new ArrayList<Map<Metric,Double>>(voterList.size());
		detailedEvaluations = new ArrayList<Map<Date, Map<AlgorithmModel, AlgorithmResult>>>(voterList.size());
		/*if(printOutput){
			setupResultsFile();
		}*/
		if(algVoters != null && algVoters.size() > 0){
			for(int expN = 0; expN < experimentsSize(); expN++){ 
				redKMap = new HashMap<KnowledgeType, Knowledge>();
				for(KnowledgeType kType : getKnowledgeTypes()){
					redKMap.put(kType, getKnowledge(kType).get(expN));
				}
				voterList.add(new ExperimentEvaluator(algVoters, redKMap, voter));
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
		ExperimentEvaluator ev = (ExperimentEvaluator)t;
		metricValues.add(ev.calculateMetricScores(validationMetrics));
		detailedEvaluations.add(ev.getSingleAlgorithmScores());
	}
	
	

	/**
	 * Setup results file.
	 */
	/*private void setupResultsFile() {
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
	}*/
	
	public Map<String, List<VotingResult>> getVotingEvaluations() {
		Map<String, List<VotingResult>> outMap = new TreeMap<String, List<VotingResult>>();
		for(Thread t : getThreadList()){
			ExperimentEvaluator ev = (ExperimentEvaluator)t;
			outMap.put(ev.getExperimentName(), ev.getExperimentVoting());
		}
		return outMap;
	}
	
	public List<Map<Metric, Double>> getMetricsValues() {
		return metricValues;
	}
	
	public Map<String, List<Map<AlgorithmModel, AlgorithmResult>>> getDetailedEvaluations() {
		Map<String, List<Map<AlgorithmModel, AlgorithmResult>>> outMap = new TreeMap<String, List<Map<AlgorithmModel, AlgorithmResult>>>();
		if(detailedEvaluations != null && detailedEvaluations.size() > 0){
			for(int i=0;i<getThreadList().size();i++){
				ExperimentEvaluator ev = (ExperimentEvaluator)getThreadList().get(i);
				outMap.put(ev.getExperimentName(), new LinkedList<Map<AlgorithmModel, AlgorithmResult>>());
				//System.out.println(ev.getExperimentName());
				if(i < detailedEvaluations.size()){
					Map<Date,Map<AlgorithmModel,AlgorithmResult>> map = detailedEvaluations.get(i);
					if(map != null){
						for(Date mapEntry : map.keySet()){
							outMap.get(ev.getExperimentName()).add(map.get(mapEntry));
						}
					} else {
						System.out.println(ev.getExperimentName());
					}
				} else {
					System.out.println(ev.getExperimentName());
				}
			}
		}
		return outMap;
	}
	
	public Map<String, List<InjectedElement>> getFailures(){
		Map<String, List<InjectedElement>> outMap = new TreeMap<String, List<InjectedElement>>();
		for(int i=0;i<getThreadList().size();i++){
			ExperimentEvaluator ev = (ExperimentEvaluator)getThreadList().get(i);
			outMap.put(ev.getExperimentName(), ev.getFailuresList());
		}
		return outMap;
	}
	
	public Integer getCheckersNumber() {
		return InputManager.loadAlgorithmModelsFor(scoresFile, voter).size();
	}	
	
	public void printDetailedKnowledgeScores(){
		BufferedWriter writer;
		String header1 = "";
		String header2 = "";
		Map<AlgorithmModel, AlgorithmResult> map;
		Set<AlgorithmModel> voterList;
		try {
			Map<String, List<Map<AlgorithmModel, AlgorithmResult>>> detailedExperimentsScores = getDetailedEvaluations();
			Map<String, List<VotingResult>> votingScores = getVotingEvaluations();
			if(votingScores != null && votingScores.size() > 0 &&
					detailedExperimentsScores != null && detailedExperimentsScores.size() > 0){
				writer = new BufferedWriter(new FileWriter(new File(outputFolder + File.separatorChar + "algorithmscores_" + voter.toString().replace(" ", "_") + ".csv")));
				header1 = "exp,index,fault/attack,reload_eval,reload_score,reload_confidence,";
				header2 = ",,,,,,";
				
				Iterator<String> it = detailedExperimentsScores.keySet().iterator();
				String tag = it.next();
				while(it.hasNext() && (detailedExperimentsScores.get(tag) == null || detailedExperimentsScores.get(tag).size() == 0)){
					tag = it.next();
				}
				
				map = detailedExperimentsScores.get(tag).get(0);
				voterList = map.keySet();
				for(AlgorithmModel av : voterList){
					header1 = header1 + "," + av.getAlgorithmType() + ",,,,," + av.getDataSeries().toString().replace("#PLAIN#", "(P)").replace("#DIFFERENCE#", "(D)").replace("NO_LAYER", "") + ",";
					header2 = header2 + ",score,decision_function,eval,confidence,,";
					if(av.getDataSeries().size() == 1){
						header2 = header2 + av.getDataSeries().getName().replace("#PLAIN#", "(P)").replace("#DIFFERENCE#", "(D)").replace("NO_LAYER", "");
					} else {
						for(int i=0;i<av.getDataSeries().size();i++){
							header1 = header1 + ",";
							header2 = header2 + ((MultipleDataSeries)av.getDataSeries()).getSeries(i).getSanitizedName() + ",";
						}
					}
					header2 = header2 + ",";					
				}
				
				writer.write("* This file reports on the scores each anomaly checker (couple of algorithm and indicator/feature) gives for each data point considered in the evaluation set. \n"
						+ "Data points are identified by name of the experiment and index inside the experiment, we report the true label of the data point (the one in the dataset) and the prediction made by RELOAD. \n"
						+ "In addition, for each anomaly checker we report a triple <score, decision function, evaluation> where the evaluation is calculated by applying such decision function to the score.\n");
				writer.write(header1 + "\n" + header2 + "\n");
				
				Map<String, List<InjectedElement>> injections = getFailures();
				for(String expName : votingScores.keySet()){
					if(detailedExperimentsScores.get(expName) != null && detailedExperimentsScores.get(expName).size() > 0){
						//timedRef = detailedKnowledgeScores.get(expName).get(0).getDate();
						Knowledge knowledge = Knowledge.findKnowledge(getKnowledge(), expName);
						for(int i=0;i<votingScores.get(expName).size();i++){
							writer.write(expName + "," + 
									i + "," + 
									(injections.get(expName).get(i) != null ? injections.get(expName).get(i).getDescription() : "") + "," +
									(votingScores.get(expName).get(i).getBooleanScore() ? "YES" : "NO") + "," +
									votingScores.get(expName).get(i).getVotingResult() + "," + 
									votingScores.get(expName).get(i).getConfidence() + ",");
							if(i < detailedExperimentsScores.get(expName).size()){
								map = detailedExperimentsScores.get(expName).get(i);
								for(AlgorithmModel av : voterList){
									for(AlgorithmModel mapVoter : map.keySet()){
										if(mapVoter.compareTo(av) == 0){
											av = mapVoter;
											break;
										}
									}
									writer.write("," + map.get(av).getScore() + "," + 
											(map.get(av).getDecisionFunction() != null ? map.get(av).getDecisionFunction().toCompactStringComplete() : "CUSTOM")  + "," + 
											map.get(av).getScoreEvaluation() + "," + map.get(av).getConfidence() + ",,");
									if(knowledge != null){
										Snapshot snap = knowledge.buildSnapshotFor(i, av.getDataSeries());
										if(av.getDataSeries().size() == 1){
											writer.write(((DataSeriesSnapshot)snap).getSnapValue().getFirst() + ",");
										} else {
											for(int j=0;j<av.getDataSeries().size();j++){
												writer.write(((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)av.getDataSeries()).getSeries(j)).getSnapValue().getFirst() + ",");
											}
										}
									}
								}
							}
							writer.write("\n");
						}
					}
				}
				writer.close();
			}
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write summary files");
		}
	}
	
	private void summarizeCSV() {
		BufferedWriter writer;
		File summaryFile = new File(outputFolder + File.separatorChar + eType.toString().toLowerCase() + "Summary.csv");		
		double score;
		try {
			if(metricValues != null){
				if(!summaryFile.exists()){
					writer = new BufferedWriter(new FileWriter(summaryFile));
					writer.write("voter,anomaly_checkers,");
					for(Metric met : validationMetrics){
						writer.write(met.getMetricName() + ",");
					}
					writer.write("\n");
				} else {
					writer = new BufferedWriter(new FileWriter(summaryFile, true));
				}
				writer.write(voter.toString() + "," + getCheckersNumber() + ",");
				for(Metric met : validationMetrics){
					score = Double.parseDouble(Metric.getAverageMetricValue(metricValues, met));
					writer.write(score + ",");
				}
				writer.write("\n");
				writer.close();
			}
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write summary file");
		}
	}
	
	protected enum EvaluatorType {VALIDATION, OPTIMIZATION}

	public List<AlgorithmModel> getModels() {
		return InputManager.loadAlgorithmModelsFor(scoresFile, voter);
	}
	
	public String getMetricsString(){
		String metString = "";
		if(metricValues != null){
			for(Metric met : validationMetrics){
				double score = Double.parseDouble(Metric.getAverageMetricValue(metricValues, met));
				metString = metString + score + ",";
			}
		}
		return metString;
	}

	public ScoresVoter getVoter() {
		return voter;
	}

}
