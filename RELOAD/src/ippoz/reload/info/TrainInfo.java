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
import java.util.Date;
import java.util.HashMap;

/**
 * @author Tommy
 *
 */
public class TrainInfo {
	
	private static final String TRAIN_LOADER = "TRAIN Loader";
	
	private String loaderName;
	
	private static final String TRAIN_SERIES = "TRAIN DataSeries";

	private DataSeries dataSeries;
	
	private static final String TRAIN_KFOLD = "TRAIN KFold";
	
	private Integer kFold;
	
	private static final String TRAIN_RUNS = "TRAIN Runs";
	
	private String runs;
	
	private static final String TRAIN_NDATAPOINTS = "TRAIN Number of Data Points";
	
	private Integer nDataPoints;
	
	private static final String TRAIN_ALGORITHMS = "TRAIN Algorithms";
	
	private LearnerType algTypes;
	
	private static final String TRAIN_FAULT_RATIO = "TRAIN FaultAttack Ratio";
	
	private Double faultRatio;
	
	private static final String TRAIN_TIME = "TRAIN Time(ms)";
	
	private Long trainTimeMs;
	
	private static final String TRAIN_METRICS = "TRAIN Metrics";
	
	private String metricsString;
	
	public TrainInfo(){
		loaderName = null;
		dataSeries = null;
		kFold = null;
		runs = null;
		nDataPoints = null;
		algTypes = null;
		faultRatio = null;
		trainTimeMs = null;
		metricsString = null;
	}
	
	public TrainInfo(File file){
		HashMap<String, String> preferences;
		try {
			preferences = AppUtility.loadPreferences(file, null);
			if(preferences != null && !preferences.isEmpty()){
				if(preferences.containsKey(TRAIN_LOADER) && preferences.get(TRAIN_LOADER) != null){
					loaderName = preferences.get(TRAIN_LOADER).trim();
				}
				if(preferences.containsKey(TRAIN_ALGORITHMS) && preferences.get(TRAIN_ALGORITHMS) != null && preferences.get(TRAIN_ALGORITHMS).trim().length() > 0){
					String algString = preferences.get(TRAIN_ALGORITHMS).trim();
					try {
						algTypes = LearnerType.fromString(algString.trim());
					} catch(Exception ex){
						AppLogger.logException(getClass(), ex, "Unable to decode algorithm '" + algString + "'");
					}
				}
				if(preferences.containsKey(TRAIN_KFOLD) && preferences.get(TRAIN_KFOLD) != null && AppUtility.isInteger(preferences.get(TRAIN_KFOLD).trim())){
					kFold = Integer.parseInt(preferences.get(TRAIN_KFOLD).trim());
				}
				if(preferences.containsKey(TRAIN_RUNS) && preferences.get(TRAIN_RUNS) != null){
					runs = preferences.get(TRAIN_RUNS).trim().replace(",", ";");
				}
				if(preferences.containsKey(TRAIN_NDATAPOINTS) && preferences.get(TRAIN_NDATAPOINTS) != null && AppUtility.isInteger(preferences.get(TRAIN_NDATAPOINTS).trim())){
					nDataPoints = Integer.parseInt(preferences.get(TRAIN_NDATAPOINTS).trim());
				}
				if(preferences.containsKey(TRAIN_SERIES) && preferences.get(TRAIN_SERIES) != null && preferences.get(TRAIN_SERIES).trim().length() > 0){
					if(preferences.get(TRAIN_SERIES).contains(",")){
						String[] splitted = preferences.get(TRAIN_SERIES).trim().split(",");
						for(String algString : splitted){
							try {
								dataSeries = DataSeries.fromString(algString.trim());
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to decode dataseries '" + algString + "'");
							}
						}
					} else {
						try {
							dataSeries = DataSeries.fromString(preferences.get(TRAIN_SERIES).trim());
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
				if(preferences.containsKey(TRAIN_METRICS) && preferences.get(TRAIN_METRICS) != null){
					metricsString = preferences.get(TRAIN_METRICS).trim();
				}
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Error while loading train info");
		}
	}
	
	public String getRuns() {
		return runs;
	}

	public void setSeries(DataSeries dataSeries) {
		this.dataSeries = dataSeries;
	}
	
	public void setKFold(int kFold) {
		this.kFold = kFold;
	}

	public void setAlgorithm(LearnerType algTypes) {
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

	public String getLoaderName() {
		return loaderName;
	}

	public void setLoaderName(String loaderName) {
		this.loaderName = loaderName;
	}

	public DataSeries getSeriesList() {
		return dataSeries;
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

	public LearnerType getAlgTypes() {
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
			writer.write("\n* Loader Name\n" + TRAIN_LOADER + " = " + (loaderName != null ? loaderName : "") + "\n");
			writer.write("\n* Algorithms used in the experiment\n" + TRAIN_ALGORITHMS + " = " + (algTypes != null ? algTypes.toString() : "") + "\n");
			writer.write("\n* K-Fold value used\n" + TRAIN_KFOLD + " = " + (kFold != null ? kFold : "") + "\n");
			writer.write("\n* Runs used for training\n" + TRAIN_RUNS + " = " + (runs != null ? runs : "") + "\n");
			writer.write("\n* Number of Data Points used for training\n" + TRAIN_NDATAPOINTS + " = " + (nDataPoints != null ? nDataPoints : "") + "\n");
			writer.write("\n* Data Series used with algorithms\n" + TRAIN_SERIES + " = " + (dataSeries != null ? dataSeries.toString() : "") + "\n");
			writer.write("\n* % of Faults/attacks in training set\n" + TRAIN_FAULT_RATIO + " = " + (faultRatio != null ? faultRatio : "") + "\n");
			writer.write("\n* Training time in ms\n" + TRAIN_TIME + " = " + (trainTimeMs != null ? trainTimeMs : "") + "\n");
			writer.write("\n* Metric values for training\n" + TRAIN_METRICS + " = " + (metricsString != null ? metricsString : "") + "\n");
			writer.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to write train INFO");
		} 		
	}
	
	public String toFileString(){
		return (loaderName != null ? loaderName.replace(",", ";") : "") + ","
			    + (algTypes != null ? algTypes.toString() : "").replace(",", ";").replace("[", "").replace("]", "") + ","
				+ (kFold != null ? kFold : "") + ","
				+ (runs != null ? runs.replace(",", ";") : "") + ","
				+ (nDataPoints != null ? nDataPoints : "") + ","
				+ (dataSeries != null ? dataSeries.toString().replace(",", ";").replace("[", "").replace("]", "") : "") + ","
				+ (faultRatio != null ? faultRatio : "") + ","
				+ (trainTimeMs != null ? trainTimeMs : "") + ","
				+ (metricsString != null ? metricsString.replace(",", ";") : "");
	}
	
	public static String getFileHeader(){
		return TRAIN_LOADER + "," + TRAIN_ALGORITHMS + "," + TRAIN_KFOLD + "," + TRAIN_RUNS + "," +
				TRAIN_NDATAPOINTS + "," + TRAIN_SERIES + "," +
				TRAIN_FAULT_RATIO + "," + TRAIN_TIME + "," + TRAIN_METRICS;
	}

	public void setRuns(String runs) {
		this.runs = runs;
	}
	
	public String getMetricsString() {
		return metricsString;
	}

	public void setMetricsString(String metricsString) {
		this.metricsString = metricsString;
	}

}
