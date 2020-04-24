/**
 * 
 */
package ippoz.reload.info;

import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;

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
public class TrainInfo {
	
	private static final String TRAIN_SERIES = "TRAIN DataSeries";

	private List<DataSeries> seriesList;
	
	private static final String TRAIN_KFOLD = "TRAIN KFold";
	
	private Integer kFold;
	
	private static final String TRAIN_RUNS = "TRAIN Runs";
	
	private String runs;
	
	private static final String TRAIN_NDATAPOINTS = "TRAIN Number of Data Points";
	
	private Integer nDataPoints;
	
	private static final String TRAIN_ALGORITHMS = "TRAIN Algorithms";
	
	private List<LearnerType> algTypes;
	
	private static final String TRAIN_FAULT_RATIO = "TRAIN FaultAttack Ratio";
	
	private Double faultRatio;
	
	private static final String TRAIN_TIME = "TRAIN Time(ms)";
	
	private Long trainTimeMs;
	
	public TrainInfo(){
		seriesList = null;
		kFold = null;
		runs = null;
		nDataPoints = null;
		algTypes = null;
		faultRatio = null;
		trainTimeMs = null;
	}
	
	public TrainInfo(File file){
		HashMap<String, String> preferences;
		try {
			preferences = AppUtility.loadPreferences(file, null);
			if(preferences != null && !preferences.isEmpty()){
				if(preferences.containsKey(TRAIN_ALGORITHMS) && preferences.get(TRAIN_ALGORITHMS) != null && preferences.get(TRAIN_ALGORITHMS).trim().length() > 0){
					algTypes = new LinkedList<LearnerType>();
					if(preferences.get(TRAIN_ALGORITHMS).contains(",")){
						String[] splitted = preferences.get(TRAIN_ALGORITHMS).trim().replace("[", "").replace("]", "").split(",");
						for(String algString : splitted){
							try {
								algTypes.add(LearnerType.fromString(algString.trim()));
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to decode algorithm '" + algString + "'");
							}
						}
					} else {
						try {
							algTypes.add(LearnerType.fromString(preferences.get(TRAIN_ALGORITHMS).trim().replace("[", "").replace("]", "")));
						} catch(Exception ex){
							AppLogger.logException(getClass(), ex, "Unable to decode algorithm '" + preferences.get(TRAIN_ALGORITHMS) + "'");
						}
					}
				}
				if(preferences.containsKey(TRAIN_KFOLD) && preferences.get(TRAIN_KFOLD) != null && AppUtility.isInteger(preferences.get(TRAIN_KFOLD).trim())){
					kFold = Integer.parseInt(preferences.get(TRAIN_KFOLD).trim());
				}
				if(preferences.containsKey(TRAIN_RUNS) && preferences.get(TRAIN_RUNS) != null){
					runs = preferences.get(TRAIN_RUNS).trim();
				}
				if(preferences.containsKey(TRAIN_NDATAPOINTS) && preferences.get(TRAIN_NDATAPOINTS) != null && AppUtility.isInteger(preferences.get(TRAIN_NDATAPOINTS).trim())){
					nDataPoints = Integer.parseInt(preferences.get(TRAIN_NDATAPOINTS).trim());
				}
				if(preferences.containsKey(TRAIN_SERIES) && preferences.get(TRAIN_SERIES) != null && preferences.get(TRAIN_SERIES).trim().length() > 0){
					seriesList = new LinkedList<DataSeries>();
					if(preferences.get(TRAIN_SERIES).contains(",")){
						String[] splitted = preferences.get(TRAIN_SERIES).trim().split(",");
						for(String algString : splitted){
							try {
								seriesList.add(DataSeries.fromString(algString.trim(), false));
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to decode dataseries '" + algString + "'");
							}
						}
					} else {
						try {
							seriesList.add(DataSeries.fromString(preferences.get(TRAIN_SERIES).trim(), false));
						} catch(Exception ex){
							AppLogger.logException(getClass(), ex, "Unable to decode dataseries '" + preferences.get(TRAIN_SERIES) + "'");
						}
					}
				}
				if(preferences.containsKey(TRAIN_FAULT_RATIO) && preferences.get(TRAIN_FAULT_RATIO) != null && AppUtility.isNumber(preferences.get(TRAIN_FAULT_RATIO).trim())){
					faultRatio = Double.parseDouble(preferences.get(TRAIN_FAULT_RATIO).trim());
				}
				if(preferences.containsKey(TRAIN_TIME) && preferences.get(TRAIN_TIME) != null && AppUtility.isInteger(preferences.get(TRAIN_TIME).trim())){
					trainTimeMs = Long.parseLong(preferences.get(TRAIN_TIME).trim());
				}
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Error while loading train info");
		}
	}
	
	public void setSeries(List<DataSeries> seriesList) {
		this.seriesList = seriesList;
	}
	
	public void setKFold(int kFold) {
		this.kFold = kFold;
	}

	public void setAlgorithms(List<LearnerType> algTypes) {
		this.algTypes = algTypes;
	}

	public void setDataPoints(int nRuns) {
		this.nDataPoints = nRuns;
	}
	
	public void setFaultRatio(double injectionsRatio) {
		this.faultRatio = injectionsRatio;
	}
	
	public void setTrainingTime(long trainTimeMs) {
		this.trainTimeMs = trainTimeMs;
	}

	public List<DataSeries> getSeriesList() {
		return seriesList;
	}

	public Integer getkFold() {
		return kFold;
	}

	public Integer getNRuns() {
		return nDataPoints;
	}
	
	public Double getFaultRatio() {
		return faultRatio;
	}

	public List<LearnerType> getAlgTypes() {
		return algTypes;
	}
	
	public Long getTrainTime(){
		return trainTimeMs;
	}

	public void printFile(File file) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("* INFO file generated at " + new Date() + " that reports on training details\n"); 
			writer.write("\n* Algorithms used in the experiment\n" + TRAIN_ALGORITHMS + " = " + (algTypes != null ? Arrays.toString(algTypes.toArray()) : "") + "\n");
			writer.write("\n* K-Fold value used\n" + TRAIN_KFOLD + " = " + (kFold != null ? kFold : "") + "\n");
			writer.write("\n* Runs used for training\n" + TRAIN_RUNS + " = " + (runs != null ? runs : "") + "\n");
			writer.write("\n* Number of Data Points used for training\n" + TRAIN_NDATAPOINTS + " = " + (nDataPoints != null ? nDataPoints : "") + "\n");
			writer.write("\n* Data Series used with algorithms\n" + TRAIN_SERIES + " = " + (seriesList != null ? Arrays.toString(seriesList.toArray()) : "") + "\n");
			writer.write("\n* % of Faults/attacks in training set\n" + TRAIN_FAULT_RATIO + " = " + (faultRatio != null ? faultRatio : "") + "\n");
			writer.write("\n* Training time in ms\n" + TRAIN_TIME + " = " + (trainTimeMs != null ? trainTimeMs : "") + "\n");
			writer.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to write train INFO");
		} 		
	}
	
	public String toFileString(){
		return (algTypes != null ? Arrays.toString(algTypes.toArray()) : "").replace(",", ";").replace("[", "").replace("]", "") + ","
				+ (kFold != null ? kFold : "") + ","
				+ (runs != null ? runs : "") + ","
				+ (nDataPoints != null ? nDataPoints : "") + ","
				+ (seriesList != null ? Arrays.toString(seriesList.toArray()) : "").replace(",", ";").replace("[", "").replace("]", "") + ","
				+ (faultRatio != null ? faultRatio : "") + ","
				+ (trainTimeMs != null ? trainTimeMs : "");
	}
	
	public static String getFileHeader(){
		return TRAIN_ALGORITHMS + "," + TRAIN_KFOLD + "," + TRAIN_RUNS + "," +
				TRAIN_NDATAPOINTS + "," + TRAIN_SERIES + "," +
				TRAIN_FAULT_RATIO + "," + TRAIN_TIME;
	}

	public void setRuns(String runs) {
		this.runs = runs;
	}

}
