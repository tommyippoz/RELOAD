/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.IndicatorDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.SnapshotValue;
import ippoz.reload.commons.service.StatPair;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;
import ippoz.reload.trainer.AlgorithmTrainer;
import ippoz.reload.trainer.ConfigurationSelectorTrainer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	
	private int kfold;
	
	private Map<DataSeries, Map<String, List<Double>>> seriesExpData;
	
	private List<PearsonResult> pResults;
	
	public PearsonCombinationManager(File indexesFile, List<DataSeries> seriesList, List<Knowledge> kList, int kfold){
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
		List<DataSeries> nList;
		BufferedReader reader;
		String readed;
		try {
			pResults = new LinkedList<PearsonResult>();
			if(pearsonFile.exists()){
				reader = new BufferedReader(new FileReader(pearsonFile));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*") && readed.contains(";")){
							if(readed.split(",").length > 0){
								if(readed.split(",")[0].contains("@")){
									nList = new ArrayList<DataSeries>(readed.split(",")[0].split("@").length);
									for(String sName : readed.split(",")[0].split("@")){
										nList.add(DataSeries.fromString(sName.trim(), true));
									}
								} else {
									nList = new ArrayList<DataSeries>(1);
									nList.add(DataSeries.fromString(readed.split(",")[0].trim(), true));
								}
								pResults.add(new PearsonResult(nList, Double.parseDouble(readed.split(",")[1]), Double.parseDouble(readed.split(",")[2])));
							}
						}
					}
				}
				reader.close();
			} 
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read indicator couples");
		}
	}
	
	public void calculatePearsonIndexes(double pearsonSimple, double pearsonComplex){
		Integer correlationSize = 2;
		PearsonResult pr;
		List<Double> pExp;
		List<DataSeries> dsList;
		Map<Integer, List<PearsonResult>> pMap;
		try {
			AppLogger.logInfo(getClass(), "Calculating Indicator Correlations");
			pMap = new HashMap<Integer, List<PearsonResult>>();
			pMap.put(2, new LinkedList<PearsonResult>());
			for(DataSeries ds1 : seriesExpData.keySet()){
				for(DataSeries ds2 : seriesExpData.keySet()){
					if(!ds1.equals(ds2)){
						pExp = new ArrayList<Double>(kList.size());
						for(Knowledge kItem : kList){
							pExp.add(new PearsonsCorrelation().correlation(AppUtility.toPrimitiveArray(seriesExpData.get(ds1).get(kItem.getTag())), AppUtility.toPrimitiveArray(seriesExpData.get(ds2).get(kItem.getTag()))));
						}
						dsList = new ArrayList<DataSeries>(2);
						dsList.add(ds1);
						dsList.add(ds2);
						pr = new PearsonResult(dsList, pExp);
						if(pr.isValid(pMap.get(2), pearsonSimple))
							pMap.get(2).add(pr);
					}
				}
			}
			AppLogger.logInfo(getClass(), "Found " + pMap.get(2).size() + " valid 2-uples correlations");
			while(pMap.get(correlationSize) != null && pMap.get(correlationSize).size() > 1){
				
				correlationSize++;
				pMap.put(correlationSize, new LinkedList<PearsonResult>());
				
				for(int i=0;i<pMap.get(correlationSize-1).size();i++){
					PearsonResult res = pMap.get(correlationSize-1).get(i);
					for(int j=i+1;j<pMap.get(correlationSize-1).size();j++){
						PearsonResult otherRes = pMap.get(correlationSize-1).get(j);
						if(!res.equals(otherRes) && match(res, otherRes)){
							dsList = merge(res, otherRes);
							if(dsList.size() >= correlationSize){
								if(!pMap.containsKey(dsList.size()))
									pMap.put(dsList.size(), new LinkedList<PearsonResult>());
								pr = new PearsonResult(dsList, Math.abs(res.getAvg()) <= Math.abs(otherRes.getAvg()) ? res.getAvg() : otherRes.getAvg(), res.getStd() + otherRes.getStd());
								if(pr.isValid(pMap.get(dsList.size()), pearsonComplex))
									pMap.get(dsList.size()).add(pr);
							}
						}
					}
				}
				
				AppLogger.logInfo(getClass(), "Found " + pMap.get(correlationSize).size() + " valid " + correlationSize + "-uples correlations");
				
			}
			
			pResults = new LinkedList<PearsonResult>();
			List<Integer> keys = new ArrayList<Integer>(pMap.keySet());
			Collections.sort(keys);
			for(Integer cIndex : keys){
				if(pMap.get(cIndex) != null && pMap.get(cIndex).size() > 0) {
					pResults.addAll(pMap.get(cIndex));
				}
			}
			AppLogger.logInfo(getClass(), "Found " + pResults.size() + " valid correlations");
			
			printPearsonResults();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Error while calculating pearson correlations");
		}
	}
	
	private void printPearsonResults() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(indexesFile));
			writer.write("data_series,avg,std\n");
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
			trainerList.add(new ConfigurationSelectorTrainer(AlgorithmType.PEA, null, metric, reputation, kList, adaptConf(confList, pr).get(AlgorithmType.PEA), kfold));
		}
		return trainerList;
	}
	
	private Map<AlgorithmType, List<AlgorithmConfiguration>> adaptConf(Map<AlgorithmType, List<AlgorithmConfiguration>> confList, PearsonResult pr) {
		for(AlgorithmConfiguration ac : confList.get(AlgorithmType.PEA)){
			ac.addItem(AlgorithmConfiguration.DETAIL, pr.toFileRow());
		}
		return confList;
	}

	public void flush(){
		seriesExpData.clear();
		seriesExpData = null;
	}
	
	private class PearsonResult {
		
		private List<DataSeries> dsList;
		private StatPair prStats;
		
		public PearsonResult(List<DataSeries> dsList, List<Double> pCalc) {
			this.dsList = dsList;
			prStats = new StatPair(pCalc);
		}

		public PearsonResult(List<DataSeries> dsList, double avg, double std) {
			this.dsList = dsList;
			prStats = new StatPair(avg, std);
		}
		
		public double getAvg(){
			return prStats.getAvg();
		}

		public double getStd(){
			return prStats.getStd();
		}
		
		private List<DataSeries> getDataSeries() {
			return dsList;
		}
		
		public boolean isValid(List<PearsonResult> pResults, double minAvg){
			boolean foundFlag;
			for(PearsonResult pR : pResults){
				foundFlag = true;
				for(DataSeries ds : dsList){
					if(!pR.containsSeries(ds)){
						foundFlag = false;
						break;
					}
				}
				if(foundFlag)
					return false;
			}
			return Math.abs(prStats.getAvg()) >= minAvg && Math.abs(prStats.getAvg()) < 1; 
		}
		
		private boolean containsSeries(DataSeries otherDs){
			for(DataSeries ds : dsList){
				if(ds.equals(otherDs))
					return true;
			}
			return false;
		}
		
		public String toFileRow(){
			String fileRow = "";
			for(DataSeries ds : dsList) {
				fileRow = fileRow + ds.toString() + "@";
			}
			return fileRow.substring(0, fileRow.length()-1) + "," + prStats.getAvg() + "," + prStats.getStd();
		}
		
	}
	
	public static List<DataSeries> merge(PearsonResult pr1, PearsonResult pr2){
		List<DataSeries> mergedList = new LinkedList<DataSeries>();
		mergedList.addAll(pr1.getDataSeries());
		for(DataSeries ds : pr2.getDataSeries()){
			if(!pr1.containsSeries(ds))
				mergedList.add(ds);
		}
		return mergedList;
	}
	
	public static boolean match(PearsonResult pr1, PearsonResult pr2){
		for(DataSeries ds : pr2.getDataSeries()){
			if(pr1.containsSeries(ds))
				return true;
		}
		return false;
	}
	
}
