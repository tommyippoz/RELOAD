/**
 * 
 */
package ippoz.multilayer.detector.performance;

import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.support.AppUtility;
import ippoz.multilayer.detector.trainer.AlgorithmTrainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class TrainingTiming {
	
	private static final String[] trainingAttr = {TrainingStat.AVERAGE, TrainingStat.MEDIAN, TrainingStat.STD, TrainingStat.CONFIGURATIONS, TrainingStat.AVERAGE_TOT, TrainingStat.MEDIAN_TOT, TrainingStat.STD_TOT, TrainingStat.EXPERIMENTS, TrainingStat.AVERAGE_EXP, TrainingStat.MEDIAN_EXP, TrainingStat.STD_EXP, TrainingStat.AVERAGE_EXP_TOT, TrainingStat.MEDIAN_EXP_TOT, TrainingStat.STD_EXP_TOT};
	private static final String[] scoresAttr = {TrainingStat.RATING_POSITION, TrainingStat.RATING_SCORE, TrainingStat.PRESENCE_FIRST_10, TrainingStat.PRESENCE_FIRST_30, TrainingStat.PRESENCE_FIRST_50, TrainingStat.PRESENCE_FIRST_100};
	
	private LinkedList<TrainingResult> resList;
	private HashMap<AlgorithmType, TrainingStat> statMap;
	private HashMap<AlgorithmType, LinkedList<TrainingDetail>> algTrainingTimes;
	
	public TrainingTiming(){
		algTrainingTimes = new HashMap<AlgorithmType, LinkedList<TrainingDetail>>();
	}

	public synchronized void addTrainingTime(AlgorithmType algType, long time, int confNumber) {
		if(algTrainingTimes.get(algType) == null)
			algTrainingTimes.put(algType, new LinkedList<TrainingDetail>());
		algTrainingTimes.get(algType).add(new TrainingDetail(confNumber, time));
	}
	
	public void addAlgorithmScores(LinkedList<? extends Thread> list) {
		AlgorithmTrainer trainer;
		resList = new LinkedList<TrainingResult>();
		for(Thread tThread : list){
			trainer = (AlgorithmTrainer)tThread;
			resList.add(new TrainingResult(trainer.getAlgType(), trainer.getMetricScore()));
		}
		Collections.sort(resList);
		computeStats(((AlgorithmTrainer)list.getFirst()).getExpNumber());
	}
	
	public String getHeader() {
		String first = "algorithm,training";
		String second = "algorithm,";
		for(String tag : trainingAttr){
			first = first + ",";
			second = second + tag + ",";
		}
		first = first + "scores";
		for(String tag : scoresAttr){
			first = first + ",";
			second = second + tag + ",";
		}
		return first + "\n" + second;
	}

	public String toFileRow(AlgorithmType algType) {
		String row = algType + ",";
		for(String tag : trainingAttr){
			row = row + statMap.get(algType).getStat(tag) + ",";
		}
		for(String tag : scoresAttr){
			row = row + statMap.get(algType).getStat(tag) + ",";
		}
		return row;
	}
	
	private void computeStats(int nExp){
		statMap = new HashMap<AlgorithmType, TrainingStat>();
		calculateTrainingStats(nExp);
		calculateScoresStats();
	}
	
	private void calculateScoresStats() {
		int i = 1;
		HashMap<AlgorithmType, Integer> algCount = new HashMap<AlgorithmType, Integer>(); 
		HashMap<AlgorithmType, Double> ratingMap = new HashMap<AlgorithmType, Double>();
		HashMap<AlgorithmType, Double> scoreMap = new HashMap<AlgorithmType, Double>();
		for(AlgorithmType algType : algTrainingTimes.keySet()){
			algCount.put(algType, 0);
			ratingMap.put(algType, 0.0);
			scoreMap.put(algType, 0.0);
		}
		Collections.sort(resList);
		for(TrainingResult td : resList){
			algCount.put(td.getAlgType(), algCount.get(td.getAlgType())+1);
			ratingMap.put(td.getAlgType(), ratingMap.get(td.getAlgType()) + (resList.size() - i + 1));
			scoreMap.put(td.getAlgType(), scoreMap.get(td.getAlgType()) + (resList.size() - i + 1)*td.getScore());
			if(i == 10){
				for(AlgorithmType algType : algTrainingTimes.keySet()){
					statMap.get(algType).addStat(TrainingStat.PRESENCE_FIRST_10, 1.0*algCount.get(algType)/10.0);
				}
			} else if(i == 30){
				for(AlgorithmType algType : algTrainingTimes.keySet()){
					statMap.get(algType).addStat(TrainingStat.PRESENCE_FIRST_30, 1.0*algCount.get(algType)/30.0);
				}
			} else if(i == 50){
				for(AlgorithmType algType : algTrainingTimes.keySet()){
					statMap.get(algType).addStat(TrainingStat.PRESENCE_FIRST_50, 1.0*algCount.get(algType)/50.0);
				}
			} else if(i == 100){
				for(AlgorithmType algType : algTrainingTimes.keySet()){
					statMap.get(algType).addStat(TrainingStat.PRESENCE_FIRST_100, 1.0*algCount.get(algType)/100.0);
				}
			}
			i++;
		}
		for(AlgorithmType algType : algTrainingTimes.keySet()){
			statMap.get(algType).addStat(TrainingStat.RATING_POSITION, ratingMap.get(algType));
			statMap.get(algType).addStat(TrainingStat.RATING_SCORE, scoreMap.get(algType));
		}
	}

	private void calculateTrainingStats(int nExp) {
		int n;
		Double[] timeSingleExp;
		Double[] timeTotExp;
		Double[] timeSingle;
		Double[] timeTot;
		for(AlgorithmType algType : algTrainingTimes.keySet()){
			n = algTrainingTimes.get(algType).size();
			timeSingleExp = new Double[n];
			timeTotExp = new Double[n];
			timeSingle = new Double[n];
			timeTot = new Double[n];
			for(int i=0;i<n;i++){
				timeSingleExp[i] = algTrainingTimes.get(algType).get(i).getSingleExpTime(nExp);
				timeTotExp[i] = algTrainingTimes.get(algType).get(i).getExpTime(nExp);
				timeSingle[i] = algTrainingTimes.get(algType).get(i).getSingleTime();
				timeTot[i] = algTrainingTimes.get(algType).get(i).getTime();
			}
			statMap.put(algType, new TrainingStat());
			statMap.get(algType).addStat(TrainingStat.AVERAGE, AppUtility.calcAvg(timeSingle));
			statMap.get(algType).addStat(TrainingStat.STD, AppUtility.calcStd(timeSingle, Double.valueOf(statMap.get(algType).getStat(TrainingStat.AVERAGE))));
			statMap.get(algType).addStat(TrainingStat.MEDIAN, AppUtility.calcMedian(timeSingle));
			statMap.get(algType).addStat(TrainingStat.CONFIGURATIONS, (double)algTrainingTimes.get(algType).getFirst().getnConfigurations());
			statMap.get(algType).addStat(TrainingStat.AVERAGE_TOT, AppUtility.calcAvg(timeTot));
			statMap.get(algType).addStat(TrainingStat.STD_TOT, AppUtility.calcStd(timeTot, Double.valueOf(statMap.get(algType).getStat(TrainingStat.AVERAGE_TOT))));
			statMap.get(algType).addStat(TrainingStat.MEDIAN_TOT, AppUtility.calcMedian(timeTot));
			statMap.get(algType).addStat(TrainingStat.EXPERIMENTS, (double)nExp);
			statMap.get(algType).addStat(TrainingStat.AVERAGE_EXP, AppUtility.calcAvg(timeSingleExp));
			statMap.get(algType).addStat(TrainingStat.STD_EXP, AppUtility.calcStd(timeSingleExp, Double.valueOf(statMap.get(algType).getStat(TrainingStat.AVERAGE_EXP))));
			statMap.get(algType).addStat(TrainingStat.MEDIAN_EXP, AppUtility.calcMedian(timeSingleExp));
			statMap.get(algType).addStat(TrainingStat.AVERAGE_EXP_TOT, AppUtility.calcAvg(timeTotExp));
			statMap.get(algType).addStat(TrainingStat.STD_EXP_TOT, AppUtility.calcStd(timeTotExp, Double.valueOf(statMap.get(algType).getStat(TrainingStat.AVERAGE_EXP_TOT))));
			statMap.get(algType).addStat(TrainingStat.MEDIAN_EXP_TOT, AppUtility.calcMedian(timeTotExp));
		}
	}

	private class TrainingResult implements Comparable<TrainingResult> {
		
		private AlgorithmType algType;
		private double score;
		
		public TrainingResult(AlgorithmType algType, double score) {
			super();
			this.algType = algType;
			this.score = score;
		}

		public AlgorithmType getAlgType() {
			return algType;
		}

		public double getScore() {
			return score;
		}

		@Override
		public int compareTo(TrainingResult arg0) {
			return -1*Double.compare(getScore(), arg0.getScore());
		}
		
	}
	
	private class TrainingStat {
		
		public static final String AVERAGE = "avg"; 
		public static final String MEDIAN = "med"; 
		public static final String STD = "std"; 
		public static final String CONFIGURATIONS = "configurations"; 
		public static final String AVERAGE_TOT = "avg_tot"; 
		public static final String MEDIAN_TOT = "med_tot"; 
		public static final String STD_TOT = "std_tot"; 
		public static final String EXPERIMENTS = "train_experiments"; 
		public static final String AVERAGE_EXP = "avg_exp"; 
		public static final String MEDIAN_EXP = "med_exp"; 
		public static final String STD_EXP = "std_exp"; 
		public static final String AVERAGE_EXP_TOT = "avg_exp_tot"; 
		public static final String MEDIAN_EXP_TOT = "med_exp_tot"; 
		public static final String STD_EXP_TOT = "std_exp_tot"; 
		public static final String PRESENCE_FIRST_10 = "pr10"; 
		public static final String PRESENCE_FIRST_30 = "pr30";
		public static final String PRESENCE_FIRST_50 = "pr50"; 
		public static final String PRESENCE_FIRST_100 = "pr100";
		public static final String RATING_POSITION = "rate_pos"; 
		public static final String RATING_SCORE = "rate_score"; 
		
		private HashMap<String, Double> stats;
		
		public TrainingStat(){
			stats = new HashMap<String, Double>();
		}
		
		public String getStat(String tag) {
			return String.valueOf(stats.get(tag));
		}

		public void addStat(String statName, Double statValue){
			stats.put(statName, statValue);
		}
		
	}
	
	private class TrainingDetail {
		
		private int nConfigurations;
		private long time;
		
		public TrainingDetail(int nConfigurations, long time) {
			this.nConfigurations = nConfigurations;
			this.time = time;
		}
		
		public Double getSingleExpTime(int nExp) {
			return getSingleTime()/nExp;
		}

		public Double getExpTime(int nExp) {
			return getTime()/nExp;
		}

		public Double getSingleTime() {
			if(nConfigurations > 1)
				return getTime()/nConfigurations;
			else return getTime();
		}

		public int getnConfigurations() {
			return nConfigurations;
		}
		
		public double getTime() {
			return (double)time;
		}
			
	}

}
