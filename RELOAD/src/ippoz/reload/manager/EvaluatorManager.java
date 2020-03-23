/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.evaluation.ExperimentEvaluator;
import ippoz.reload.metric.Metric;
import ippoz.reload.voter.ScoresVoter;
import ippoz.reload.voter.VotingResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
	
	private ScoresVoter voter;
	
	/** The validation metrics. */
	private Metric[] validationMetrics;
	
	private List<Map<Metric, Double>> expMetricEvaluations;
	
	private List<Map<Date, Map<AlgorithmModel, AlgorithmResult>>> detailedEvaluations;
	
	/** The algorithm convergence time. */
	private double algConvergence;
	
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
	public EvaluatorManager(ScoresVoter voter, String oFolder, String outputFormat, String scoresFile, Map<KnowledgeType, List<Knowledge>> map, Metric[] validationMetrics, double algConvergence, boolean printOutput) {
		super(map);
		this.scoresFile = scoresFile;
		this.outputFormat = outputFormat;
		this.validationMetrics = validationMetrics;
		this.algConvergence = algConvergence;
		this.printOutput = printOutput;
		this.voter = voter;
		outputFolder = oFolder + voter.toString().replace(" ", "_");
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
		expMetricEvaluations = new ArrayList<Map<Metric,Double>>(voterList.size());
		detailedEvaluations = new ArrayList<Map<Date, Map<AlgorithmModel, AlgorithmResult>>>(voterList.size());
		if(printOutput){
			setupResultsFile();
		}
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
		expMetricEvaluations.add(ev.printVoting(outputFormat, outputFolder, validationMetrics, algConvergence, printOutput));
		detailedEvaluations.add(ev.getSingleAlgorithmScores());
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
	
	public Map<String, List<VotingResult>> getVotingEvaluations() {
		Map<String, List<VotingResult>> outMap = new TreeMap<String, List<VotingResult>>();
		for(Thread t : getThreadList()){
			ExperimentEvaluator ev = (ExperimentEvaluator)t;
			outMap.put(ev.getExperimentName(), ev.getExperimentVoting());
		}
		return outMap;
	}
	
	public List<Map<Metric, Double>> getMetricsEvaluations() {
		return expMetricEvaluations;
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

}
