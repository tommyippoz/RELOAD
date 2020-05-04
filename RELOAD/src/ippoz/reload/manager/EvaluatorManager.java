/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.MetaLearner;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	/** The scores file. */
	private String scoresFile;
	
	/** The validation metrics. */
	private Metric[] validationMetrics;
	
	private Map<String, Double> metricValues;
	
	private AlgorithmModel evalModel;
	
	private List<List<AlgorithmResult>> detailedEvaluations;
	
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
		this.scoresFile = scoresFile + File.separatorChar + "scores.csv";
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
	
	public Map<String, Double> getMetricsValues() {
		if(metricValues == null)
			computeMetricValues();
		return metricValues;
	}
	
	private void computeMetricValues() {
		metricValues = new HashMap<>();
		List<AlgorithmResult> allResults = new ArrayList<>();
		for(List<AlgorithmResult> list : detailedEvaluations){
			allResults.addAll(list);
		}
		for(Metric met : validationMetrics){
			metricValues.put(met.getMetricName(), met.evaluateAnomalyResults(allResults));
		}
	}

	public Map<LoaderBatch, List<AlgorithmResult>> getDetailedEvaluations() {
		Map<LoaderBatch, List<AlgorithmResult>> outMap = new TreeMap<>();
		if(detailedEvaluations != null && detailedEvaluations.size() > 0){
			for(int i=0;i<getThreadList().size();i++){
				ExperimentEvaluator ev = (ExperimentEvaluator)getThreadList().get(i);
				outMap.put(ev.getExperimentID(), new LinkedList<>());
				if(i < detailedEvaluations.size()){
					List<AlgorithmResult> map = detailedEvaluations.get(i);
					outMap.get(ev.getExperimentID()).addAll(map);
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
		try {
			Map<LoaderBatch, List<AlgorithmResult>> detailedExperimentsScores = getDetailedEvaluations();
			if(detailedExperimentsScores != null && detailedExperimentsScores.size() > 0){
				writer = new BufferedWriter(new FileWriter(new File(outputFolder + File.separatorChar + "scores.csv")));
				header1 = "dataset_information,,,,,reload_eval,,,,,dataset_features";
				header2 = "batch_index,item_index,dataset_index,label,,boolean_score,confidence,score,decision_function,,";
				if(evalModel.getDataSeries().size() == 1){
					header2 = header2 + evalModel.getDataSeries().getName().replace("#PLAIN#", "(P)").replace("#DIFFERENCE#", "(D)").replace("NO_LAYER", "");
				} else {
					for(int i=0;i<evalModel.getDataSeries().size();i++){
						header1 = header1 + ",";
						header2 = header2 + ((MultipleDataSeries)evalModel.getDataSeries()).getSeries(i).getSanitizedName() + ",";
					}
				}				
				if(evalModel.getAlgorithmType() instanceof MetaLearner){
					header1 = header1 + ",meta_features,";
					header2 = header2 + ",";
					for(BaseLearner bl : ((MetaLearner)evalModel.getAlgorithmType()).getBaseLearners()){
						header2 = header2 + bl.toCompactString() + ",";
					}
				}
				
				writer.write("* This file reports on the scores each anomaly checker (couple of algorithm and indicator/feature) gives for each data point considered in the evaluation set. \n"
						+ "Data points are identified by name of the experiment and index inside the experiment, we report the true label of the data point (the one in the dataset) and the prediction made by RELOAD. \n"
						+ "In addition, for each anomaly checker we report a triple <score, decision function, evaluation> where the evaluation is calculated by applying such decision function to the score.\n");
				writer.write(header1 + "\n" + header2 + "\n");
				
				for(LoaderBatch expName : detailedExperimentsScores.keySet()){
					if(detailedExperimentsScores.get(expName) != null && detailedExperimentsScores.get(expName).size() > 0){
						//timedRef = detailedKnowledgeScores.get(expName).get(0).getDate();
						Knowledge knowledge = Knowledge.findKnowledge(getKnowledge(), expName);
						for(int i=0;i<detailedExperimentsScores.get(expName).size();i++){
							AlgorithmResult res = detailedExperimentsScores.get(expName).get(i);
							writer.write(expName.getTag() + "," + i + "," + (expName.getFrom() + i) + "," + (res.getInjection() != null ? res.getInjection().getDescription() : "") + ",,");
							writer.write(res.getScoreEvaluation() + "," + res.getConfidence() + "," +
									res.getScore() + "," + (res.getDecisionFunction() != null ? res.getDecisionFunction().toCompactStringComplete() : "CUSTOM") + ",,");
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
							if(evalModel.getAlgorithmType() instanceof MetaLearner){
								double[] ob = (double[]) detailedExperimentsScores.get(expName).get(i).getAdditionalScore();
								writer.write("," + Arrays.toString(ob).replace("[", "").replace("]", ""));
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
				double score = metricValues.get(met.getMetricName());
				metString = metString + met.getMetricShortName() + ":" + score + ",";
			}
		}
		return metString;
	}

}
