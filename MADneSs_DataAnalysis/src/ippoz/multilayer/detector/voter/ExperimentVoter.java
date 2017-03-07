/**
 * 
 */
package ippoz.multilayer.detector.voter;

import ippoz.multilayer.commons.layers.LayerType;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.AppUtility;
import ippoz.multilayer.detector.graphics.HistogramChartDrawer;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.performance.EvaluationTiming;
import ippoz.multilayer.detector.performance.ExperimentTiming;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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
	private LinkedList<AlgorithmVoter> algList;
	
	/** The complete results of the voting. */
	private TreeMap<Date, HashMap<AlgorithmVoter, Double>> partialVoting;
	
	/** The contracted results of the voting. */
	private TreeMap<Date, Double> voting;
	
	/** The list of the snapshots for each voter */
	private LinkedList<HashMap<AlgorithmVoter, Snapshot>> expSnapMap;
	
	private EvaluationTiming eTiming;
	
	/**
	 * Instantiates a new experiment voter.
	 *
	 * @param expData the experiment data
	 * @param algList the algorithm list
	 * @param pManager 
	 */
	public ExperimentVoter(ExperimentData expData, LinkedList<AlgorithmVoter> algList, EvaluationTiming eTiming) {
		super();
		this.expName = expData.getName();
		this.algList = deepClone(algList);
		this.eTiming = eTiming;
		expSnapMap = loadExpAlgSnapshots(expData);
	}
	
	private LinkedList<HashMap<AlgorithmVoter, Snapshot>> loadExpAlgSnapshots(ExperimentData expData) {
		HashMap<AlgorithmVoter, Snapshot> newMap;
		LinkedList<HashMap<AlgorithmVoter, Snapshot>> expAlgMap = new LinkedList<HashMap<AlgorithmVoter, Snapshot>>();
		for(int i=0;i<expData.getSnapshotNumber();i++){
			newMap = new HashMap<AlgorithmVoter, Snapshot>();
			for(AlgorithmVoter aVoter : algList){
				newMap.put(aVoter, expData.buildSnapshotFor(aVoter.getAlgorithmType(), i, aVoter.getDataSeries(), aVoter.getAlgorithmConfiguration()));
			}
			expAlgMap.add(newMap);
		}
		return expAlgMap;
	}
	
	/**
	 * Deep clone of the voters' list.
	 *
	 * @param algorithms the algorithms
	 * @return the deep-cloned list
	 */
	private LinkedList<AlgorithmVoter> deepClone(LinkedList<AlgorithmVoter> algorithms) {
		LinkedList<AlgorithmVoter> list = new LinkedList<AlgorithmVoter>();
		try {
			for(AlgorithmVoter aVoter : algorithms){
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
		double baseTime;
		Snapshot snapshot = null;
		HashMap<AlgorithmVoter, Double> snapVoting;
		ExperimentTiming expTiming = new ExperimentTiming(expSnapMap.size());
		partialVoting = new TreeMap<Date, HashMap<AlgorithmVoter, Double>>();
		voting = new TreeMap<Date, Double>();
		if(algList.size() > 0) {
			for(int i=0;i<expSnapMap.size();i++){
				snapVoting = new HashMap<AlgorithmVoter, Double>();
				for(AlgorithmVoter aVoter : algList){
					baseTime = AppUtility.readMillis();
					snapshot = expSnapMap.get(i).get(aVoter);
					snapVoting.put(aVoter, aVoter.voteSnapshot(snapshot));
					expTiming.addExpTiming(aVoter.getAlgorithmType(), AppUtility.readMillis() - baseTime);
				}
				baseTime = AppUtility.readMillis();
				partialVoting.put(snapshot.getTimestamp(), snapVoting);
				voting.put(snapshot.getTimestamp(), voteResults(snapVoting));
				expTiming.setVotingTime(AppUtility.readMillis() - baseTime);
			}
		}
		eTiming.addExperimentTiming(expTiming);
	}
	
	/**
	 * Votes results obtaining a contracted indication about anomaly (double score)
	 *
	 * @param algResults the complete algorithm scoring results
	 * @return contracted anomaly score
	 */
	private double voteResults(HashMap<AlgorithmVoter, Double> algResults){
		double snapScore = 0.0;
		for(AlgorithmVoter aVoter : algList){
			snapScore = snapScore + 1.0*aVoter.getReputationScore()*algResults.get(aVoter);
		}
		return snapScore;
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
	public HashMap<Metric, Double> printVoting(String outFormat, String outFolderName, Metric[] validationMetrics, double anomalyTreshold, double algConvergence, boolean printOutput) {
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
	private HashMap<Metric, Double> printExperimentVoting(String outFolderName, Metric[] validationMetrics, double anomalyTreshold, double algConvergence, boolean printOutput) {
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
	private synchronized HashMap<Metric, Double> printMetrics(String outFolderName, Metric[] validationMetrics, double anomalyTreshold, boolean printOutput) {
		PrintWriter pw;
		HashMap<Metric, Double> metResults = new HashMap<Metric, Double>();
		try {
			for(Metric met : validationMetrics){
				metResults.put(met, met.evaluateAnomalyResults(getSimpleSnapshotList(), voting, anomalyTreshold));
			}
			if(printOutput){
				pw = new PrintWriter(new FileOutputStream(new File(outFolderName + "/voter/results.csv"), true));
				pw.append(expName + "," + expSnapMap.size() + ",");
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

	private LinkedList<Snapshot> getSimpleSnapshotList() {
		LinkedList<Snapshot> simpleList = new LinkedList<Snapshot>();
		for(HashMap<AlgorithmVoter, Snapshot> map : expSnapMap){
			simpleList.add(map.get(algList.getFirst()));
		}
		return simpleList;
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
		HashMap<String, TreeMap<Double, Double>> voterMap = new HashMap<String, TreeMap<Double, Double>>();
		voterMap.put(ANOMALY_SCORE_LABEL, AppUtility.convertMapSnapshots(voting));
		voterMap.put(FAILURE_LABEL, convertFailures(expSnapMap));
		hist = new HistogramChartDrawer("Anomaly Score", "Seconds", "Score", voterMap, anomalyTreshold, algConvergence);
		hist.saveToFile(outFolderName + "/voter/graphic/" + expName + ".png", IMG_WIDTH, IMG_HEIGHT);
	}
	
	private TreeMap<Double, Double> convertFailures(LinkedList<HashMap<AlgorithmVoter, Snapshot>> expSnapMap) {
		TreeMap<Date, Double> treeMap = new TreeMap<Date, Double>();
		for(HashMap<AlgorithmVoter, Snapshot> map : expSnapMap){
			if(map.get(algList.getFirst()).getInjectedElement() != null){
				treeMap.put(map.get(algList.getFirst()).getTimestamp(), 1.0);
				for(int i=1;i<map.get(algList.getFirst()).getInjectedElement().getDuration();i++){
					treeMap.put(new Date(map.get(algList.getFirst()).getTimestamp().getTime() + i*1000), -1.0);
				}
			}
		}
		return AppUtility.convertMapTimestamps(expSnapMap.getFirst().get(algList.getFirst()).getTimestamp(), treeMap);
	}
	
	/**
	 * Prints the textual summarization of the voting.
	 *
	 * @param outFolderName the output folder
	 */
	private void printText(String outFolderName){
		BufferedWriter writer = null;
		HashMap<LayerType, HashMap<AlgorithmType, Integer>> countMap;
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
					if(partialVoting.get(timestamp).get(aVoter) > 0.0){
						countMap.get(aVoter.getLayerType()).replace(aVoter.getAlgorithmType(), countMap.get(aVoter.getLayerType()).get(aVoter.getAlgorithmType()) + 1);			
						partial = partial + aVoter.toString() + "|";
						count++;
					}
				}
				writer.write(AppUtility.getSecondsBetween(timestamp, expSnapMap.getFirst().get(algList.getFirst()).getTimestamp()) + ",");
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
	private HashMap<LayerType, HashMap<AlgorithmType, Integer>> buildMap() {
		HashMap<LayerType, HashMap<AlgorithmType, Integer>> map = new HashMap<LayerType, HashMap<AlgorithmType, Integer>>();
		for(AlgorithmVoter aVoter : algList){
			if(!map.keySet().contains(aVoter.getLayerType()))
				map.put(aVoter.getLayerType(), new HashMap<AlgorithmType, Integer>());
			if(!map.get(aVoter.getLayerType()).containsKey(aVoter.getAlgorithmType()))
				map.get(aVoter.getLayerType()).put(aVoter.getAlgorithmType(), 0);
		}
		return map;
	}

}
