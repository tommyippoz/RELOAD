/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.loader.LoaderBatch;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.evaluation.ExperimentEvaluator;
import ippoz.reload.metric.Metric;
import ippoz.reload.voter.ScoresVoter;
import ippoz.reload.voter.VotingResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	
	/** The output folder. */
	private String outputFolder;
	
	/** The scores file. */
	private String scoresFile;
	
	/** The validation metrics. */
	private Metric[] validationMetrics;
	
	private List<Map<Metric, Double>> metricValues;
	
	private AlgorithmModel evalModel;
	
	private List<Map<Date, AlgorithmResult>> detailedEvaluations;
	
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
	public EvaluatorManager(String outputFolder, String scoresFile, Map<KnowledgeType, List<Knowledge>> map, Metric[] validationMetrics, boolean printOutput) {
		super(map);
		this.scoresFile = scoresFile;
		this.validationMetrics = validationMetrics;
		this.printOutput = printOutput;
		this.outputFolder = outputFolder;
		evalModel = buildEvaluationModel();
		if(!new File(outputFolder).exists())
			new File(outputFolder).mkdirs();
		AppLogger.logInfo(getClass(), "Evaluating " + map.get(map.keySet().iterator().next()).size() + " experiments");
	}
	
	private AlgorithmModel buildEvaluationModel(){
		List<AlgorithmModel> algVoters = AlgorithmModel.fromFile(scoresFile);
		if(algVoters != null && algVoters.size() > 0)
			return algVoters.get(0);
		else return null;
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
			//summarizeCSV();
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
		List<ExperimentEvaluator> voterList = new ArrayList<ExperimentEvaluator>(experimentsSize());
		Map<KnowledgeType, Knowledge> redKMap;
		metricValues = new ArrayList<Map<Metric,Double>>(voterList.size());
		detailedEvaluations = new ArrayList<>(voterList.size());
		if(evalModel != null){
			try {
				for(int expN = 0; expN < experimentsSize(); expN++){ 
					voterList.add(new ExperimentEvaluator(evalModel, getKnowledge(DetectionAlgorithm.getKnowledgeType(evalModel.getAlgorithmType())).get(expN)));
				}
			} catch (CloneNotSupportedException e) {
				AppLogger.logException(getClass(), e, "Error while loading Experiment Evaluators");
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
	
	public Map<LoaderBatch, List<AlgorithmResult>> getVotingEvaluations() {
		Map<LoaderBatch, List<AlgorithmResult>> outMap = new TreeMap<>();
		for(Thread t : getThreadList()){
			ExperimentEvaluator ev = (ExperimentEvaluator)t;
			outMap.put(ev.getExperimentID(), ev.getExperimentResults());
		}
		return outMap;
	}
	
	public List<Map<Metric, Double>> getMetricsValues() {
		return metricValues;
	}
	
	public Map<LoaderBatch, List<AlgorithmResult>> getDetailedEvaluations() {
		Map<LoaderBatch, List<AlgorithmResult>> outMap = new TreeMap<>();
		if(detailedEvaluations != null && detailedEvaluations.size() > 0){
			for(int i=0;i<getThreadList().size();i++){
				ExperimentEvaluator ev = (ExperimentEvaluator)getThreadList().get(i);
				outMap.put(ev.getExperimentID(), new LinkedList<>());
				if(i < detailedEvaluations.size()){
					Map<Date, AlgorithmResult> map = detailedEvaluations.get(i);
					if(map != null){
						for(Date mapEntry : map.keySet()){
							outMap.get(ev.getExperimentID()).add(map.get(mapEntry));
						}
					} else {
						System.out.println(ev.getExperimentID());
					}
				} else {
					System.out.println(ev.getExperimentID());
				}
			}
		}
		return outMap;
	}
	
	public Map<LoaderBatch, List<InjectedElement>> getFailures(){
		Map<LoaderBatch, List<InjectedElement>> outMap = new TreeMap<>();
		for(int i=0;i<getThreadList().size();i++){
			ExperimentEvaluator ev = (ExperimentEvaluator)getThreadList().get(i);
			outMap.put(ev.getExperimentID(), ev.getFailuresList());
		}
		return outMap;
	}
	
	public Integer getCheckersNumber() {
		return AlgorithmModel.fromFile(scoresFile).size();
	}	
	
	public void printDetailedKnowledgeScores(){
		BufferedWriter writer;
		String header1 = "";
		String header2 = "";
		AlgorithmResult map;
		try {
			Map<LoaderBatch, List<AlgorithmResult>> detailedExperimentsScores = getDetailedEvaluations();
			if(detailedExperimentsScores != null && detailedExperimentsScores.size() > 0){
				writer = new BufferedWriter(new FileWriter(new File(outputFolder + File.separatorChar + "scores.csv")));
				header1 = "exp,index,datapoint_index,fault/attack,reload_eval,reload_score,reload_confidence,";
				header2 = ",,,,,,,";
				
				Iterator<LoaderBatch> it = detailedExperimentsScores.keySet().iterator();
				LoaderBatch tag = it.next();
				while(it.hasNext() && (detailedExperimentsScores.get(tag) == null || detailedExperimentsScores.get(tag).size() == 0)){
					tag = it.next();
				}
				
				map = detailedExperimentsScores.get(tag).get(0);
				header1 = header1 + "," + evalModel.getAlgorithmType() + ",,,,," + evalModel.getDataSeries().toString().replace("#PLAIN#", "(P)").replace("#DIFFERENCE#", "(D)").replace("NO_LAYER", "") + ",";
				header2 = header2 + ",score,decision_function,eval,confidence,,";
				if(evalModel.getDataSeries().size() == 1){
					header2 = header2 + evalModel.getDataSeries().getName().replace("#PLAIN#", "(P)").replace("#DIFFERENCE#", "(D)").replace("NO_LAYER", "");
				} else {
					for(int i=0;i<evalModel.getDataSeries().size();i++){
						header1 = header1 + ",";
						header2 = header2 + ((MultipleDataSeries)evalModel.getDataSeries()).getSeries(i).getSanitizedName() + ",";
					}
				}
				header2 = header2 + ",";					
				
				writer.write("* This file reports on the scores each anomaly checker (couple of algorithm and indicator/feature) gives for each data point considered in the evaluation set. \n"
						+ "Data points are identified by name of the experiment and index inside the experiment, we report the true label of the data point (the one in the dataset) and the prediction made by RELOAD. \n"
						+ "In addition, for each anomaly checker we report a triple <score, decision function, evaluation> where the evaluation is calculated by applying such decision function to the score.\n");
				writer.write(header1 + "\n" + header2 + "\n");
				
				Map<LoaderBatch, List<InjectedElement>> injections = getFailures();
				for(LoaderBatch expName : detailedExperimentsScores.keySet()){
					if(detailedExperimentsScores.get(expName) != null && detailedExperimentsScores.get(expName).size() > 0){
						//timedRef = detailedKnowledgeScores.get(expName).get(0).getDate();
						Knowledge knowledge = Knowledge.findKnowledge(getKnowledge(), expName);
						for(int i=0;i<detailedExperimentsScores.get(expName).size();i++){
							writer.write(expName.getTag() + "," + 
									i + "," + (expName.getFrom() + i) + "," +
									(injections.get(expName).get(i) != null ? injections.get(expName).get(i).getDescription() : "") + "," +
									(detailedExperimentsScores.get(expName).get(i).getBooleanScore() ? "YES" : "NO") + "," +
									detailedExperimentsScores.get(expName).get(i).getScore() + "," + 
									detailedExperimentsScores.get(expName).get(i).getConfidence() + ",");
							if(i < detailedExperimentsScores.get(expName).size()){
								map = detailedExperimentsScores.get(expName).get(i);
								writer.write("," + map.getScore() + "," + 
										(map.getDecisionFunction() != null ? map.getDecisionFunction().toCompactStringComplete() : "CUSTOM")  + "," + 
										map.getScoreEvaluation() + "," + map.getConfidence() + ",,");
								if(knowledge != null){
									Snapshot snap = knowledge.buildSnapshotFor(i, evalModel.getDataSeries());
									if(evalModel.getDataSeries().size() == 1){
										writer.write(((DataSeriesSnapshot)snap).getSnapValue().getFirst() + ",");
									} else {
										for(int j=0;j<evalModel.getDataSeries().size();j++){
											writer.write(((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)evalModel.getDataSeries()).getSeries(j)).getSnapValue().getFirst() + ",");
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
	/*
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
	
	protected enum EvaluatorType {VALIDATION, OPTIMIZATION}*/

	public List<AlgorithmModel> getModels() {
		return AlgorithmModel.fromFile(scoresFile);
	}
	
	public String getMetricsString(){
		String metString = "";
		if(metricValues != null){
			for(Metric met : validationMetrics){
				double score = Double.parseDouble(Metric.getAverageMetricValue(metricValues, met));
				metString = metString + met.getMetricShortName() + ":" + score + ",";
			}
		}
		return metString;
	}

}
