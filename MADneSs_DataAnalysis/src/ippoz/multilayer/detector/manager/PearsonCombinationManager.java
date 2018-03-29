/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.dataseries.IndicatorDataSeries;
import ippoz.multilayer.detector.commons.knowledge.Knowledge;
import ippoz.multilayer.detector.commons.knowledge.snapshot.SnapshotValue;
import ippoz.multilayer.detector.commons.service.StatPair;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.AppUtility;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.reputation.Reputation;
import ippoz.multilayer.detector.trainer.AlgorithmTrainer;
import ippoz.multilayer.detector.trainer.ConfigurationSelectorTrainer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * @author Tommy
 *
 */
public class PearsonCombinationManager {
	
	private File indexesFile;
	
	private List<DataSeries> seriesList;
	
	private List<Knowledge> kList;
	
	private Map<DataSeries, Map<String, List<Double>>> seriesExpData;
	
	private List<PearsonResult> pResults;
	
	public PearsonCombinationManager(File indexesFile, List<DataSeries> seriesList, List<Knowledge> kList){
		this.indexesFile = indexesFile;
		this.seriesList = seriesList;
		this.kList = kList;
		initExpData();
	}
	
	private void initExpData(){
		seriesExpData = new HashMap<DataSeries, Map<String, List<Double>>>();
		for(DataSeries ds : seriesList){
			if(ds instanceof IndicatorDataSeries) { 
				Map<String, List<Double>> map = new HashMap<String, List<Double>>();
				for(Knowledge kItem : kList){
					List<SnapshotValue> dsValue = kItem.getDataSeriesValues(ds);
					map.put(kItem.getTag(), new ArrayList<Double>(dsValue.size()));
					for(int i=0;i<dsValue.size();i++){
						map.get(kItem.getTag()).add(dsValue.get(i).getFirst());
					}
				}
				seriesExpData.put(ds, map);
			}
		}
	}
	
	public void loadPearsonResults(File pearsonFile) {
		pResults = new LinkedList<PearsonResult>();
		BufferedReader reader;
		String readed;
		try {
			if(pearsonFile.exists()){
				reader = new BufferedReader(new FileReader(pearsonFile));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*") && readed.contains(";")){
							pResults.add(new PearsonResult(DataSeries.fromString(readed.split(",")[0], true), DataSeries.fromString(readed.split(",")[1], true), Double.parseDouble(readed.split(",")[2]), Double.parseDouble(readed.split(",")[3])));
						}
					}
				}
				reader.close();
			} 
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read indicator couples");
		}
	}
	
	public void calculatePearsonIndexes(){
		PearsonResult pr;
		List<Double> pExp;
		pResults = new LinkedList<PearsonResult>();
		AppLogger.logInfo(getClass(), "Calculating Indicator Correlations");
		for(DataSeries ds1 : seriesExpData.keySet()){
			for(DataSeries ds2 : seriesExpData.keySet()){
				if(!ds1.equals(ds2)){
					pExp = new ArrayList<Double>(kList.size());
					for(Knowledge kItem : kList){
						pExp.add(new PearsonsCorrelation().correlation(AppUtility.toPrimitiveArray(seriesExpData.get(ds1).get(kItem.getTag())), AppUtility.toPrimitiveArray(seriesExpData.get(ds2).get(kItem.getTag()))));
					}
					pr = new PearsonResult(ds1, ds2, pExp);
					if(pr.isValid(pResults))
						pResults.add(pr);
				}
			}
		}
		printPearsonResults();
		AppLogger.logInfo(getClass(), "Found " + pResults.size() + " valid correlations");
	}
	
	private void printPearsonResults() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(indexesFile));
			writer.write("data_series,data_series,avg,std\n");
			for(PearsonResult pr : pResults){
				writer.write(pr.toFileRow() + "\n");
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to save pearson index output");
		} 
	}
	
	public List<AlgorithmTrainer> getTrainers(Metric metric, Reputation reputation, Map<AlgorithmType, List<AlgorithmConfiguration>> confList) {
		List<AlgorithmTrainer> trainerList = new ArrayList<AlgorithmTrainer>(pResults.size());
		for(PearsonResult pr : pResults){
			trainerList.add(new ConfigurationSelectorTrainer(AlgorithmType.PEA, null, metric, reputation, kList, adaptConf(confList, pr).get(AlgorithmType.PEA)));
		}
		return trainerList;
	}
	
	private Map<AlgorithmType, List<AlgorithmConfiguration>> adaptConf(Map<AlgorithmType, List<AlgorithmConfiguration>> confList, PearsonResult pr) {
		for(AlgorithmConfiguration ac : confList.get(AlgorithmType.PEA)){
			ac.addItem(AlgorithmConfiguration.DETAIL, pr.getDs1().toString() + ";" + pr.getDs2().toString() + ";" + String.valueOf(pr.getAvg()) + ";" + String.valueOf(pr.getStd()));
		}
		return confList;
	}

	public void flush(){
		seriesExpData.clear();
		seriesExpData = null;
	}
	
	private class PearsonResult {
		
		private DataSeries ds1;
		private DataSeries ds2;
		private StatPair prStats;
		
		public PearsonResult(DataSeries ds1, DataSeries ds2, List<Double> pCalc) {
			this.ds1 = ds1;
			this.ds2 = ds2;
			prStats = new StatPair(pCalc);
		}

		public PearsonResult(DataSeries ds1, DataSeries ds2, double avg, double std) {
			this.ds1 = ds1;
			this.ds2 = ds2;
			prStats = new StatPair(avg, std);
		}

		public DataSeries getDs1() {
			return ds1;
		}

		public DataSeries getDs2() {
			return ds2;
		}

		public double getAvg() {
			return prStats.getAvg();
		}
		
		public double getStd() {
			return prStats.getStd();
		}

		public boolean isValid(List<PearsonResult> pResults){
			for(PearsonResult pR : pResults){
				if((pR.getDs1().equals(ds1) || pR.getDs2().equals(ds1)) && (pR.getDs1().equals(ds2) || pR.getDs2().equals(ds2)))
					return false;
			}
			return Math.abs(prStats.getAvg()) > 0.9 && Math.abs(prStats.getAvg()) < 1; 
		}
		
		public String toFileRow(){
			return ds1.toString() + "," + ds2.toString() + "," + prStats.getAvg() + "," + prStats.getStd();
		}	
		
	}
	
}
