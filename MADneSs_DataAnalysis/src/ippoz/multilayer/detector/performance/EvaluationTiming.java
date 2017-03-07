/**
 * 
 */
package ippoz.multilayer.detector.performance;

import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.support.AppUtility;

import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class EvaluationTiming {
	
	private String voterTreshold;
	private String anomalyTreshold;
	private double nVoters;
	private double nTreshold;
	private int selectedCheckers;
	private LinkedList<ExperimentTiming> etList;
	
	public EvaluationTiming(String anomalyStrTreshold, String voterStrTreshold, double anomalyTreshold, double voterTreshold, int selectedCheckers) {
		this.voterTreshold = voterStrTreshold;
		this.anomalyTreshold = anomalyStrTreshold;
		this.nVoters = voterTreshold;
		this.nTreshold = anomalyTreshold;
		this.selectedCheckers = selectedCheckers;
		etList = new LinkedList<ExperimentTiming>();
	}

	public String toFileRow() {
		String fileRow = voterTreshold + "," + nVoters + "," + selectedCheckers + "," + anomalyTreshold + "," + nTreshold + ",";
		fileRow = fileRow + printVotingTimes();
		for(AlgorithmType at : AlgorithmType.values()){
			fileRow = fileRow + algStat(at);
		}
		return fileRow;
	}
	
	public String printVotingTimes(){
		double avg;
		LinkedList<Double> vTimes = new LinkedList<Double>();
		for(ExperimentTiming et : etList){
			vTimes.add(et.getVotingTime());
		}
		avg = AppUtility.calcAvg(vTimes);
		return avg + "," + AppUtility.calcStd(vTimes, avg) + ",";
		
	}
	
	public String algStat(AlgorithmType algType){
		int aVoters = etList.getFirst().votersFor(algType);
		Double[] expAverages = buildAverages(algType);
		double avgTime = AppUtility.calcAvg(expAverages);
		double stdTime = AppUtility.calcStd(expAverages, avgTime);
		return aVoters + "," + avgTime + "," + stdTime + ","; 
	}

	private Double[] buildAverages(AlgorithmType algType) {
		Double[] avgs = new Double[etList.size()];
		for(int i=0;i<etList.size();i++){
			avgs[i] = etList.get(i).getAvg(algType);
		}
		return avgs;
	}

	@Override
	public String toString() {
		return "EvaluationTiming [" + etList.size() + "]";
	}

	public synchronized void addExperimentTiming(ExperimentTiming expTiming) {
		etList.add(expTiming);
	}
	
}
