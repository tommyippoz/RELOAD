/**
 * 
 */
package ippoz.madness.detector.voter;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.algorithm.result.AlgorithmResult;
import ippoz.madness.detector.graphics.HistogramChartDrawer;
import ippoz.madness.detector.metric.Metric;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.TimedValue;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Class ExperimentVoter.
 *
 * @author Tommy
 */
public class ExperimentVoter extends Thread {
	
	/** The Constant IMG_WIDTH for printing. */
	private static final int IMG_WIDTH = 1000;
	
	/** The Constant IMG_HEIGHT for printing. */
	private static final int IMG_HEIGHT = 1000;
	
	/** The Constant ANOMALY_SCORE_LABEL. */
	public static final String ANOMALY_SCORE_LABEL = "Anomaly Score";
	
	/** The Constant FAILURE_LABEL. */
	public static final String FAILURE_LABEL = "Failure";
	
	/** The experiment name. */
	private String expName;
	
	/** The algorithm list. */
	private List<AlgorithmVoter> algList;
	
	/** The complete results of the voting. */
	private Map<Date, Map<AlgorithmVoter, AlgorithmResult>> partialVoting;
	
	/** The contracted results of the voting. */
	private List<TimedValue> voting;
	
	private List<TimedValue> failures;
	
	private Map<KnowledgeType, Knowledge> kMap;
	
	/**
	 * Instantiates a new experiment voter.
	 *
	 * @param expData the experiment data
	 * @param algVoters the algorithm list
	 * @param pManager 
	 */
	public ExperimentVoter(List<AlgorithmVoter> algVoters, Map<KnowledgeType, Knowledge> knowMap) {
		super();
		this.algList = deepClone(algVoters);
		kMap = new HashMap<KnowledgeType, Knowledge>();
		for(KnowledgeType kType : knowMap.keySet()){
			kMap.put(kType, knowMap.get(kType).cloneKnowledge());
		}
		expName = kMap.get(kMap.keySet().iterator().next()).getTag();
	}
	
	public String getExperimentName(){
		return expName;
	}
	
	/**
	 * Deep clone of the voters' list.
	 *
	 * @param algVoters the algorithms
	 * @return the deep-cloned list
	 */
	private List<AlgorithmVoter> deepClone(List<AlgorithmVoter> algVoters) {
		List<AlgorithmVoter> list = new ArrayList<AlgorithmVoter>(algVoters.size());
		try {
			for(AlgorithmVoter aVoter : algVoters){
				list.add(aVoter.clone());
			}
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone Experiment");
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		double votingResult;
		Map<AlgorithmVoter, AlgorithmResult> snapVoting;
		Knowledge currentKnowledge = kMap.get(kMap.keySet().iterator().next());
		partialVoting = new TreeMap<Date, Map<AlgorithmVoter, AlgorithmResult>>();
		voting = new ArrayList<TimedValue>(currentKnowledge.size());
		failures = new LinkedList<TimedValue>();
		if(algList.size() > 0) {
			for(int i=0;i<currentKnowledge.size();i++){
				snapVoting = new HashMap<AlgorithmVoter, AlgorithmResult>();
				for(AlgorithmVoter aVoter : algList){
					snapVoting.put(aVoter, aVoter.voteKnowledgeSnapshot(kMap.get(DetectionAlgorithm.getKnowledgeType(aVoter.getAlgorithmType())), i));
				}
				partialVoting.put(currentKnowledge.getTimestamp(i), snapVoting);
				votingResult = voteResults(snapVoting);
				voting.add(new TimedValue(currentKnowledge.getTimestamp(i), votingResult));
				if(currentKnowledge.getInjection(i) != null)
					failures.add(new TimedValue(currentKnowledge.getTimestamp(i), 1.0));
				if(kMap.containsKey(KnowledgeType.SLIDING)){
					((SlidingKnowledge)kMap.get(KnowledgeType.SLIDING)).slide(i, votingResult);
				}
			}
			if(kMap.containsKey(KnowledgeType.SLIDING)){
				((SlidingKnowledge)kMap.get(KnowledgeType.SLIDING)).reset();
			}
		}
	}
	
	/**
	 * Votes results obtaining a contracted indication about anomaly (double score)
	 *
	 * @param snapVoting the complete algorithm scoring results
	 * @return contracted anomaly score
	 */
	private double voteResults(Map<AlgorithmVoter, AlgorithmResult> snapVoting){
		double snapScore = 0.0;
		boolean undetectable = true;
		for(AlgorithmVoter aVoter : algList){
			double algScore = DetectionAlgorithm.convertResultIntoDouble(snapVoting.get(aVoter).getScoreEvaluation());
			if(algScore >= 0.0){
				undetectable = false;
				if(aVoter.getReputationScore() > 0)
					snapScore = snapScore + 1.0*aVoter.getReputationScore()*algScore;
				else snapScore = snapScore + 1.0*algScore;
			}
		}
		if(undetectable)
			return -1.0;
		else return snapScore;
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
	public Map<Metric, Double> printVoting(String outFormat, String outFolderName, Metric[] validationMetrics, double anomalyTreshold, double algConvergence, boolean printOutput) {
		if(printOutput){
			for(AlgorithmVoter aVoter : algList){
				aVoter.printResults(outFormat, outFolderName, expName);
			}
		}
		return printExperimentVoting(outFolderName, validationMetrics, anomalyTreshold, algConvergence, printOutput);
	}

	/**
	 * Prints the experiment voting.
	 *
	 * @param outFolderName the output folder
	 * @param validationMetrics the metrics used for validation and printed in the file
	 * @param anomalyTreshold the anomaly threshold
	 * @param algConvergence the algorithm convergence time (for printing)
	 * @param printOutput 
	 */
	private Map<Metric, Double> printExperimentVoting(String outFolderName, Metric[] validationMetrics, double anomalyTreshold, double algConvergence, boolean printOutput) {
		if(printOutput){
			printGraphics(outFolderName, anomalyTreshold, algConvergence);
			printText(outFolderName);
		}
		return printMetrics(outFolderName, validationMetrics, anomalyTreshold, printOutput);
	}
	
	/**
	 * Prints the metrics.
	 *
	 * @param outFolderName the output folder
	 * @param validationMetrics the metrics used for validation and printed in the file
	 * @param anomalyTreshold the anomaly threshold
	 * @return 
	 */
	private synchronized Map<Metric, Double> printMetrics(String outFolderName, Metric[] validationMetrics, double anomalyTreshold, boolean printOutput) {
		PrintWriter pw;
		Map<Metric, Double> metResults = new HashMap<Metric, Double>();
		try {
			for(Metric met : validationMetrics){
				//if(!met.getMetricName().equals("AUC"))
					metResults.put(met, met.evaluateAnomalyResults(kMap.get(kMap.keySet().iterator().next()), voting, anomalyTreshold));
				//else metResults.put(met, getAUC());
			}
			if(printOutput){
				pw = new PrintWriter(new FileOutputStream(new File(outFolderName + "/voter/results.csv"), true));
				pw.append(expName + "," + kMap.get(kMap.keySet().iterator().next()).size() + ",");
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
	 * @param algConvergence the algorithm convergence time (for printing)
	 */
	private void printGraphics(String outFolderName, double anomalyTreshold, double algConvergence){
		HistogramChartDrawer hist;
		Map<String, List<TimedValue>> voterMap = new HashMap<String, List<TimedValue>>();
		voterMap.put(ANOMALY_SCORE_LABEL, voting);
		voterMap.put(FAILURE_LABEL, failures);
		hist = new HistogramChartDrawer("Anomaly Score", "Seconds", "Score", resultToMap(voterMap), anomalyTreshold, algConvergence);
		hist.saveToFile(outFolderName + "/voter/graphic/" + expName + ".png", IMG_WIDTH, IMG_HEIGHT);
	}
	
	private Map<String, Map<Double, Double>> resultToMap(Map<String, List<TimedValue>> voterMap) {
		Map<String, Map<Double, Double>> map = new HashMap<String, Map<Double,Double>>();
		for(String mapTag : voterMap.keySet()){
			map.put(mapTag, new TreeMap<Double,Double>());
			for(TimedValue vr : voterMap.get(mapTag)){
				map.get(mapTag).put(vr.getDateOffset(voterMap.get(mapTag).get(0).getDate()), vr.getValue());
			}
		}
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
	 */
	private void printText(String outFolderName){
		BufferedWriter writer = null;
		Map<LayerType, Map<AlgorithmType, Integer>> countMap;
		String partial;
		int count;
		try {
			countMap = buildMap();
			writer = new BufferedWriter(new FileWriter(new File(outFolderName + "/voter/" + expName + ".csv")));
			writer.write("timestamp,anomaly_alerts,");
			for(LayerType currentLayer : countMap.keySet()){
				for(AlgorithmType algTag : countMap.get(currentLayer).keySet()){
					writer.write(currentLayer.toString() + "@" + algTag + ",");
				}
			}
			writer.write("details\n");
			for(Date timestamp : partialVoting.keySet()){
				countMap = buildMap();
				partial = "";
				count = 0;
				for(AlgorithmVoter aVoter : algList){
					double algScore = DetectionAlgorithm.convertResultIntoDouble(partialVoting.get(timestamp).get(aVoter).getScoreEvaluation());
					if(algScore > 0.0){
						countMap.get(aVoter.getLayerType()).replace(aVoter.getAlgorithmType(), countMap.get(aVoter.getLayerType()).get(aVoter.getAlgorithmType()) + 1);			
						partial = partial + aVoter.toString() + "|";
						count++;
					}
				}
				writer.write(AppUtility.getSecondsBetween(timestamp, kMap.get(kMap.keySet().iterator().next()).getTimestamp(0)) + ",");
				writer.write(count + ",");
				for(LayerType currentLayer : countMap.keySet()){
					for(AlgorithmType algTag : countMap.get(currentLayer).keySet()){
						writer.write(countMap.get(currentLayer).get(algTag) + ",");
					}
				}
				writer.write(partial + "\n");
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to save voting text output");
		} 
	}

	/**
	 * Builds the basic map used in printText function.
	 *
	 * @return the basic map
	 */
	private Map<LayerType, Map<AlgorithmType, Integer>> buildMap() {
		Map<LayerType, Map<AlgorithmType, Integer>> map = new HashMap<LayerType, Map<AlgorithmType, Integer>>();
		for(AlgorithmVoter aVoter : algList){
			if(!map.keySet().contains(aVoter.getLayerType()))
				map.put(aVoter.getLayerType(), new HashMap<AlgorithmType, Integer>());
			if(!map.get(aVoter.getLayerType()).containsKey(aVoter.getAlgorithmType()))
				map.get(aVoter.getLayerType()).put(aVoter.getAlgorithmType(), 0);
		}
		return map;
	}

	public Map<Date, Map<AlgorithmVoter, AlgorithmResult>> getSingleAlgorithmScores() {
		return partialVoting;
	}
	
	public List<TimedValue> getExperimentVoting(){
		return voting;
	}

	public int getFailuresNumber() {
		return failures != null ? failures.size() : 0;
	}
	
	public List<InjectedElement> getFailuresList() {
		List<InjectedElement> list = new LinkedList<InjectedElement>();
		Knowledge currentKnowledge = kMap.get(kMap.keySet().iterator().next());
		for(int i=0;i<currentKnowledge.size();i++){
			list.add(currentKnowledge.getInjection(i));
		}
		return list;
	}

}
