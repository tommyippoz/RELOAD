package ippoz.multilayer.detector.performance;

import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.support.AppUtility;

import java.util.HashMap;
import java.util.LinkedList;

public class ExperimentTiming {
	
	private int nObs;
	private double votingTime;
	
	private HashMap<AlgorithmType, LinkedList<Double>> algTimings;
	
	public ExperimentTiming(int nObs){
		this.nObs = nObs;
		algTimings = new HashMap<AlgorithmType, LinkedList<Double>>();
	}
	
	public void setVotingTime(double d) {
		this.votingTime = Double.valueOf(d);
	}
	
	public double getVotingTime(){
		return votingTime;
	}
	
	public Double getAvg(AlgorithmType algType) {
		return AppUtility.calcAvg(algTimings.get(algType));
	}

	public void addExpTiming(AlgorithmType algType, double d){
		if(algTimings.get(algType) == null)
			algTimings.put(algType, new LinkedList<Double>());
		algTimings.get(algType).add(d);
	}
	
	public int votersFor(AlgorithmType algType){
		if(algTimings.containsKey(algType))
			return algTimings.get(algType).size()/nObs;
		else return 0;
	}

	@Override
	public String toString() {
		String out = "ExperimentTiming [nObs=" + nObs + " | ";
		for(AlgorithmType at : algTimings.keySet()){
			out = out + at.toString() + ":" + algTimings.get(at).size() + " ";
		}
		out = out + "]";
		return out;
	}
	
}
