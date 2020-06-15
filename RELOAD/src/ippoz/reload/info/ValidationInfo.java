/**
 * 
 */
package ippoz.reload.info;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.evaluation.AlgorithmModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class ValidationInfo {
	
	private static final String VALIDATION_RUNS = "VALIDATION Runs";
	
	private String runs;
	
	private static final String VALIDATION_NDATAPOINTS = "VALIDATION Number of Data Points";
	
	private Integer nDataPoints;
	
	private static final String VALIDATION_MODELS = "VALIDATION Models";
	
	private List<AlgorithmModel> models;
	
	private static final String VALIDATION_FAULT_RATIO = "VALIDATION FaultAttack Ratio";
	
	private Double faultRatio;
	
	private static final String VALIDATION_TIME = "VALIDATION Time(ms)";
	
	private Long valTimeMs;
	
	private static final String VALIDATION_BESTSCORE = "VALIDATION Best Score";
	
	private double bestScore;
	
	private static final String VALIDATION_BESTVOTER = "VALIDATION Best Voter";
	
	private String voterString;
	
	private static final String VALIDATION_SERIESSTRING = "VALIDATION SelectedSeries";
	
	private String seriesString;
	
	private static final String VALIDATION_METRICS = "VALIDATION Metrics";
	
	private String metricsString;
	
	public ValidationInfo(){
		models = null;
		runs = null;
		nDataPoints = null;
		faultRatio = null;
		valTimeMs = null;
	}
	
	public ValidationInfo(File file){
		HashMap<String, String> preferences;
		try {
			preferences = AppUtility.loadPreferences(file, null);
			if(preferences != null && !preferences.isEmpty()){
				if(preferences.containsKey(VALIDATION_MODELS) && preferences.get(VALIDATION_MODELS) != null && preferences.get(VALIDATION_MODELS).trim().length() > 0){
					models = new LinkedList<AlgorithmModel>();
					if(preferences.get(VALIDATION_MODELS).contains(",")){
						String[] splitted = preferences.get(VALIDATION_MODELS).trim().replace("[", "").replace("]", "").split(",");
						for(String algString : splitted){
							try {
								models.add(AlgorithmModel.fromString(algString.trim()));
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to decode algorithm '" + algString + "'");
							}
						}
					} else {
						try {
							models.add(AlgorithmModel.fromString(preferences.get(VALIDATION_MODELS).trim().replace("[", "").replace("]", "")));
						} catch(Exception ex){
							AppLogger.logException(getClass(), ex, "Unable to decode algorithm '" + preferences.get(VALIDATION_MODELS) + "'");
						}
					}
				}
				if(preferences.containsKey(VALIDATION_RUNS) && preferences.get(VALIDATION_RUNS) != null){
					runs = preferences.get(VALIDATION_RUNS).trim();
				}
				if(preferences.containsKey(VALIDATION_NDATAPOINTS) && preferences.get(VALIDATION_NDATAPOINTS) != null && AppUtility.isInteger(preferences.get(VALIDATION_NDATAPOINTS).trim())){
					nDataPoints = Integer.parseInt(preferences.get(VALIDATION_NDATAPOINTS).trim());
				}
				if(preferences.containsKey(VALIDATION_FAULT_RATIO) && preferences.get(VALIDATION_FAULT_RATIO) != null && AppUtility.isNumber(preferences.get(VALIDATION_FAULT_RATIO).trim())){
					faultRatio = Double.parseDouble(preferences.get(VALIDATION_FAULT_RATIO).trim());
				}
				if(preferences.containsKey(VALIDATION_TIME) && preferences.get(VALIDATION_TIME) != null && AppUtility.isInteger(preferences.get(VALIDATION_TIME).trim())){
					valTimeMs = Long.parseLong(preferences.get(VALIDATION_TIME).trim());
				}
				if(preferences.containsKey(VALIDATION_BESTSCORE) && preferences.get(VALIDATION_BESTSCORE) != null){
					bestScore = Double.parseDouble(preferences.get(VALIDATION_BESTSCORE).trim());
				}
				if(preferences.containsKey(VALIDATION_BESTVOTER) && preferences.get(VALIDATION_BESTVOTER) != null){
					voterString = preferences.get(VALIDATION_BESTVOTER).trim();
				}
				if(preferences.containsKey(VALIDATION_SERIESSTRING) && preferences.get(VALIDATION_SERIESSTRING) != null){
					seriesString = preferences.get(VALIDATION_SERIESSTRING).trim();
				}
				if(preferences.containsKey(VALIDATION_METRICS) && preferences.get(VALIDATION_METRICS) != null){
					metricsString = preferences.get(VALIDATION_METRICS).trim();
				}
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Error while loading train info");
		}
	}
	
	public double getBestScore() {
		return bestScore;
	}

	public void setBestScore(double bestScore) {
		this.bestScore = bestScore;
	}

	public String getVoter() {
		return voterString;
	}

	public void setVoter(String voterString) {
		this.voterString = voterString;
	}

	public String getSeriesString() {
		return seriesString;
	}

	public void setSeriesString(List<DataSeries> list) {
		seriesString = "";
		if(list!= null && list.size() > 0){
			for(DataSeries ds : list){
				seriesString = seriesString + ds.getCompactName() + "; ";
			}
		}
	}

	public void setModels(List<AlgorithmModel> models) {
		this.models = models;
	}

	public void setDataPoints(int nRuns) {
		this.nDataPoints = nRuns;
	}
	
	public void setFaultRatio(double injectionsRatio) {
		this.faultRatio = injectionsRatio;
	}
	
	public void setValidationTime(long valTimeMs) {
		this.valTimeMs = valTimeMs;
	}

	public Integer getNRuns() {
		return nDataPoints;
	}
	
	public Double getFaultRatio() {
		return faultRatio;
	}

	public List<AlgorithmModel> getAlgModels() {
		return models;
	}
	
	public Long getValidationTime(){
		return valTimeMs;
	}

	public String getMetricsString() {
		return metricsString;
	}
	
	public String getMetricsValues(){
		String gridRows = "";
		if(metricsString != null && metricsString.trim().length() > 0){
			for(String splitItem : metricsString.trim().split(",")){
				if(splitItem.contains(":")){
					gridRows = gridRows + splitItem.split(":")[1].trim() + ",";
				}
			}
			return gridRows.substring(0, gridRows.length() - 1);
		}
		return "";
	}

	public void setMetricsString(String metricsString) {
		this.metricsString = metricsString;
	}

	public void printFile(File file) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("* INFO file generated at " + new Date() + " that reports on validation details\n"); 
			writer.write("\n* Models used in the experiment\n" + VALIDATION_MODELS + " = " + (models != null ? Arrays.toString(models.toArray()) : "") + "\n");
			writer.write("\n* Runs used for validation\n" + VALIDATION_RUNS + " = " + (runs != null ? runs : "") + "\n");
			writer.write("\n* Number of Data Points used for training\n" + VALIDATION_NDATAPOINTS + " = " + (nDataPoints != null ? nDataPoints : "") + "\n");
			writer.write("\n* % of Faults/attacks in validation set\n" + VALIDATION_FAULT_RATIO + " = " + (faultRatio != null ? faultRatio : "") + "\n");
			writer.write("\n* Validation time in ms\n" + VALIDATION_TIME + " = " + (valTimeMs != null ? valTimeMs : "") + "\n");
			writer.write("\n* Best 'target metric' score obtained\n" + VALIDATION_BESTSCORE + " = " + bestScore + "\n");
			writer.write("\n* Optimal voter used during validation\n" + VALIDATION_BESTVOTER + " = " + (voterString != null ? voterString : "") + "\n");
			writer.write("\n* Series used for validation\n" + VALIDATION_SERIESSTRING + " = " + (seriesString != null ? seriesString : "") + "\n");
			writer.write("\n* Metric values for validation\n" + VALIDATION_METRICS + " = " + (metricsString != null ? metricsString : "") + "\n");
			
			writer.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to write validation INFO");
		} 		
	}
	
	public String toFileString(){
		return (models != null ? Arrays.toString(models.toArray()) : "").replace(",", ";").replace("[", "").replace("]", "") + ","
				+ (runs != null ? runs : "") + ","
				+ (nDataPoints != null ? nDataPoints : "") + ","
				+ (faultRatio != null ? faultRatio : "") + ","
				+ (valTimeMs != null ? valTimeMs : "");
	}
	
	public static String getFileHeader(){
		return VALIDATION_MODELS + "," + VALIDATION_RUNS + "," +
				VALIDATION_NDATAPOINTS + "," + VALIDATION_FAULT_RATIO + "," + VALIDATION_TIME;
	}

	public void setRuns(String runs) {
		this.runs = runs;
	}

	public String getBatchesString() {
		return runs;
	}

}

