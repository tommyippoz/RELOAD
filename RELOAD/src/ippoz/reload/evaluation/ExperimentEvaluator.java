/**
 * 
 */
package ippoz.reload.evaluation;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.loader.LoaderBatch;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.graphics.HistogramChartDrawer;
import ippoz.reload.metric.Metric;

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
 * The Class ExperimentVoter.
 *
 * @author Tommy
 */
public class ExperimentEvaluator extends Thread {
	
	/** The Constant IMG_WIDTH for printing. */
	private static final int IMG_WIDTH = 1000;
	
	/** The Constant IMG_HEIGHT for printing. */
	private static final int IMG_HEIGHT = 1000;
	
	/** The Constant ANOMALY_SCORE_LABEL. */
	public static final String ANOMALY_SCORE_LABEL = "Anomaly Score";
	
	/** The Constant FAILURE_LABEL. */
	public static final String FAILURE_LABEL = "Failure";
	
	/** The experiment name. */
	private LoaderBatch expBatch;
	
	/** The algorithm list. */
	private AlgorithmModel evalModel;
	
	/** The complete results of the voting. */
	private Map<Date, AlgorithmResult> modelResults;
	
	private List<AlgorithmResult> failureScores;
	
	private Knowledge evalKnowledge;
	
	/**
	 * Instantiates a new experiment voter.
	 *
	 * @param expData the experiment data
	 * @param algVoters the algorithm list
	 * @param pManager 
	 * @throws CloneNotSupportedException 
	 */
	public ExperimentEvaluator(AlgorithmModel evalModel, Knowledge evalKnowledge) throws CloneNotSupportedException {
		super();
		if(evalModel != null)
		this.evalModel = evalModel.clone();
		this.evalKnowledge = evalKnowledge;
		if(evalKnowledge != null)
			expBatch = evalKnowledge.getID();
	}
	
	public LoaderBatch getExperimentID(){
		return expBatch;
	}
	
	/**
	 * Deep clone of the voters' list.
	 *
	 * @param algVoters the algorithms
	 * @return the deep-cloned list
	 *//*
	private List<AlgorithmModel> deepClone(List<AlgorithmModel> algVoters) {
		List<AlgorithmModel> list = new ArrayList<AlgorithmModel>(algVoters.size());
		try {
			for(AlgorithmModel aVoter : algVoters){
				list.add(aVoter.clone());
			}
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone Experiment");
		}
		return list;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		modelResults = new TreeMap<>();
		failureScores = new LinkedList<AlgorithmResult>();
		if(evalModel != null) {
			for(int i=0;i<evalKnowledge.size();i++){
				AlgorithmResult modelResult = evalModel.voteKnowledgeSnapshot(evalKnowledge, i);
				modelResults.put(evalKnowledge.getTimestamp(i), modelResult);
				if(evalKnowledge.getInjection(i) != null){
					failureScores.add(modelResult);
				}
				if(evalKnowledge.getKnowledgeType() == KnowledgeType.SLIDING){
					((SlidingKnowledge)evalKnowledge).slide(i, modelResult.getScore());
				}
			}
			if(evalKnowledge.getKnowledgeType() == KnowledgeType.SLIDING){
				((SlidingKnowledge)evalKnowledge).reset();
			}
		}
	}
	
	/**
	 * Prints the anomaly voting.
	 *
	 * @param outFormat the output format
	 * @param outFolderName the output folder
	 * @param validationMetrics the metrics used for validation and printed in the file
	 * @param anomalyTreshold the anomaly threshold
	 * @param algConvergence the algorithm convergence time (for printing)
	 * @param printOutput 
	 * @return 
	 */
	public Map<Metric, Double> printVoting(String outFormat, String outFolderName, Metric[] validationMetrics, double algConvergence, boolean printOutput) {
		/*if(printOutput){
			for(AlgorithmModel aVoter : algList){
				aVoter.printResults(outFormat, outFolderName, expName);
			}
		}*/
		return printExperimentVoting(outFolderName, validationMetrics, algConvergence, printOutput);
	}

	/**
	 * Prints the experiment voting.
	 *
	 * @param outFolderName the output folder
	 * @param validationMetrics the metrics used for validation and printed in the file
	 * @param  the anomaly threshold
	 * @param algConvergence the algorithm convergence time (for printing)
	 * @param printOutput 
	 */
	private Map<Metric, Double> printExperimentVoting(String outFolderName, Metric[] validationMetrics, double algConvergence, boolean printOutput) {
		/*if(printOutput){
			printGraphics(outFolderName, algConvergence);
			printText(outFolderName);
		}*/
		return printMetrics(outFolderName, validationMetrics, printOutput);
	}
	
	public synchronized Map<Metric, Double> calculateMetricScores(Metric[] validationMetrics) {
		Map<Metric, Double> metResults = new HashMap<Metric, Double>();
		for(Metric met : validationMetrics){
			metResults.put(met, met.evaluateAnomalyResults(new ArrayList<AlgorithmResult>(modelResults.values())));
		}
		return metResults;
	}
	
	
	
	/**
	 * Prints the metrics.
	 *
	 * @param outFolderName the output folder
	 * @param validationMetrics the metrics used for validation and printed in the file
	 * @return 
	 */
	private synchronized Map<Metric, Double> printMetrics(String outFolderName, Metric[] validationMetrics, boolean printOutput) {
		PrintWriter pw;
		Map<Metric, Double> metResults = new HashMap<Metric, Double>();
		try {
			for(Metric met : validationMetrics){
				metResults.put(met, met.evaluateAnomalyResults(new ArrayList<AlgorithmResult>(modelResults.values())));
			}
			if(printOutput){
				pw = new PrintWriter(new FileOutputStream(new File(outFolderName + "/voter/results.csv"), true));
				pw.append(expBatch + "," + evalKnowledge.size() + ",");
				for(Metric met : validationMetrics){
					pw.append(String.valueOf(metResults.get(met)) + ",");
				}
				pw.append("\n");
				pw.close();
			}
		} catch (FileNotFoundException ex) {
			AppLogger.logException(getClass(), ex, "Unable to find results file");
		} 
		return metResults;
	}

	/**
	 * Prints the graphics.
	 *
	 * @param outFolderName the output folder
	 * @param anomalyTreshold the anomaly threshold
	 */
	private void printGraphics(String outFolderName, double anomalyTreshold){
		HistogramChartDrawer hist;
		Map<String, List<? extends AlgorithmResult>> voterMap = new HashMap<String, List<? extends AlgorithmResult>>();
		voterMap.put(ANOMALY_SCORE_LABEL, new ArrayList<AlgorithmResult>(modelResults.values()));
		voterMap.put(FAILURE_LABEL, failureScores);
		hist = new HistogramChartDrawer("Anomaly Score", "Seconds", "Score", resultToMap(voterMap), anomalyTreshold, 10);
		hist.saveToFile(outFolderName + "/voter/graphic/" + expBatch + ".png", IMG_WIDTH, IMG_HEIGHT);
	}
	
	private Map<String, Map<Double, Double>> resultToMap(Map<String, List<? extends AlgorithmResult>> voterMap) {
		Map<String, Map<Double, Double>> map = new HashMap<String, Map<Double,Double>>();
		/*for(String mapTag : voterMap.keySet()){
			map.put(mapTag, new TreeMap<Double,Double>());
			for(AlgorithmResult vr : voterMap.get(mapTag)){
				map.get(mapTag).put(vr.getDateOffset(voterMap.get(mapTag).get(0).getDate()), vr.getValue());
			}
		}*/
		return map;
	}

	/*private List<TimedValue> convertFailures(List<Map<AlgorithmVoter, Snapshot>> expSnapMap) {
		List<TimedValue> failList = new LinkedList<TimedValue>();
		for(Map<AlgorithmVoter, Snapshot> map : expSnapMap){
			if(map.get(algList.get(0)).getInjectedElement() != null){
				failList.add(new TimedValue(map.get(algList.get(0)).getTimestamp(), 1.0));
				for(int i=1;i<=map.get(algList.get(0)).getInjectedElement().getDuration();i++){
					failList.add(new TimedValue(new Date(map.get(algList.get(0)).getTimestamp().getTime() + i*1000), -1.0));
				}
			}
		}
		return failList;
	}*/
	
	/**
	 * Prints the textual summarization of the voting.
	 *
	 * @param outFolderName the output folder
	 *//*
	private void printText(String outFolderName){
		BufferedWriter writer = null;
		Map<LayerType, Map<LearnerType, Integer>> countMap;
		String partial;
		int count;
		try {
			countMap = buildMap();
			writer = new BufferedWriter(new FileWriter(new File(outFolderName + "/voter/" + expBatch + ".csv")));
			writer.write("timestamp,anomaly_alerts,");
			for(LayerType currentLayer : countMap.keySet()){
				for(LearnerType algTag : countMap.get(currentLayer).keySet()){
					writer.write(currentLayer.toString() + "@" + algTag + ",");
				}
			}
			writer.write("details\n");
			for(Date timestamp : modelResults.keySet()){
				countMap = buildMap();
				partial = "";
				count = 0;
				for(AlgorithmModel aVoter : evalModel){
					double algScore = DetectionAlgorithm.convertResultIntoDouble(modelResults.get(timestamp).get(aVoter).getScoreEvaluation());
					if(algScore > 0.0){
						countMap.get(aVoter.getLayerType()).replace(aVoter.getAlgorithmType(), countMap.get(aVoter.getLayerType()).get(aVoter.getAlgorithmType()) + 1);			
						partial = partial + aVoter.toString() + "|";
						count++;
					}
				}
				writer.write(AppUtility.getSecondsBetween(timestamp, evalKnowledge.get(evalKnowledge.keySet().iterator().next()).getTimestamp(0)) + ",");
				writer.write(count + ",");
				for(LayerType currentLayer : countMap.keySet()){
					for(LearnerType algTag : countMap.get(currentLayer).keySet()){
						writer.write(countMap.get(currentLayer).get(algTag) + ",");
					}
				}
				writer.write(partial + "\n");
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to save voting text output");
		} 
	}*/

	/**
	 * Builds the basic map used in printText function.
	 *
	 * @return the basic map
	 *//*
	private Map<LayerType, Map<LearnerType, Integer>> buildMap() {
		Map<LayerType, Map<LearnerType, Integer>> map = new HashMap<LayerType, Map<LearnerType, Integer>>();
		for(AlgorithmModel aVoter : evalModel){
			if(!map.keySet().contains(aVoter.getLayerType()))
				map.put(aVoter.getLayerType(), new HashMap<LearnerType, Integer>());
			if(!map.get(aVoter.getLayerType()).containsKey(aVoter.getAlgorithmType()))
				map.get(aVoter.getLayerType()).put(aVoter.getAlgorithmType(), 0);
		}
		return map;
	}*/

	public Map<Date, AlgorithmResult> getSingleAlgorithmScores() {
		return modelResults;
	}

	public int getFailuresNumber() {
		return failureScores != null ? failureScores.size() : 0;
	}
	
	public List<InjectedElement> getFailuresList() {
		List<InjectedElement> list = new LinkedList<InjectedElement>();
		for(int i=0;i<evalKnowledge.size();i++){
			list.add(evalKnowledge.getInjection(i));
		}
		return list;
	}

	public List<AlgorithmResult> getExperimentResults() {
		return new ArrayList<AlgorithmResult>(modelResults.values());
	}

}
