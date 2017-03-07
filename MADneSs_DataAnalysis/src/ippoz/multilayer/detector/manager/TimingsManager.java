/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.support.AppLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * The Class TimingsManager.
 * Used to store performance parameters regarding the setup and/or the execution of the anomaly detector.
 *
 * @author Tommy
 */

public class TimingsManager {

	/** The train runs. */
	public static final String TRAIN_RUNS = "train_runs";
	
	/** The validation runs. */
	public static final String VALIDATION_RUNS = "validation_runs";
	
	/** The anomaly checkers. */
	public static final String ANOMALY_CHECKERS = "anomaly_checkers";
	
	/** The selected anomaly checkers. */
	public static final String SELECTED_ANOMALY_CHECKERS = "selected_anomaly_checkers";
	
	/** The scoring metric. */
	public static final String SCORING_METRIC = "scoring_metric";
	
	/** The reputation metric. */
	public static final String REPUTATION_METRIC = "reputation_metric";
	
	/** The execution time. */
	public static final String EXECUTION_TIME = "execution_time";
	
	/** The train time. */
	public static final String TRAIN_TIME = "train_time";
	
	/** The train init time. */
	public static final String TRAIN_INIT_TIME = "train_init_time";
	
	/** The validation time. */
	public static final String VALIDATION_TIME = "validation_time";
	
	/** The avg train time. */
	public static final String AVG_TRAIN_TIME = "exp_train_time";
	
	/** The avg validation time. */
	public static final String AVG_VALIDATION_TIME = "exp_validation_time";
	
	/** The load train time. */
	public static final String LOAD_TRAIN_TIME = "load_train_time";
	
	/** The load validation time. */
	public static final String LOAD_VALIDATION_TIME = "load_validation_time";
	
	/** The avg load train time. */
	public static final String AVG_LOAD_TRAIN_TIME = "exp_load_train_time";
	
	/** The avg load validation time. */
	public static final String AVG_LOAD_VALIDATION_TIME = "exp_load_validation_time";
	
	/** The timing map. */
	private HashMap<String, Object> timingMap;
	
	/**
	 * Instantiates a new performance manager.
	 * @param anomalyTreshold 
	 * @param voterTreshold 
	 */
	public TimingsManager(){
		timingMap = new HashMap<String, Object>();
	}
	
	/**
	 * Adds a numeric timing parameter.
	 *
	 * @param tag parameter tag
	 * @param value parameter value
	 */
	public void addTiming(String tag, Double value){
		timingMap.put(tag, value);
	}
	
	/**
	 * Adds a string timing parameter.
	 *
	 * @param tag parameter tag
	 * @param value parameter value
	 */
	public void addTiming(String tag, String value){
		timingMap.put(tag, value);
	}
	
	/**
	 * Prints the timings.
	 *
	 * @param filename the filename
	 */
	public void printTimings(String outFolder){
		printGeneralTimings(new File(outFolder + "/generalTimings.preferences"));
	}
	
	private void printGeneralTimings(File gtFile){
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(gtFile));
			for(String key : timingMap.keySet()){
				writer.write(key + " = " + timingMap.get(key) + "\n");
			}
			writer.close();
		} catch (FileNotFoundException ex) {
			AppLogger.logException(getClass(), ex, "Unable to find results file");
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Write error");
		}
	}
	
	public void setupExpTimings(File etFile){
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(etFile));
			writer.write(getExpFileHeader() + "\n");
			writer.close();
		} catch (FileNotFoundException ex) {
			AppLogger.logException(getClass(), ex, "Unable to find results file");
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Write error");
		}
	}
	
	@SuppressWarnings("unused")
	private static String getExpFileHeader() {
		String bigH = "checkers,,,treshold,,voting,,";
		String smallH = "text,selected,all,text,value,avg,std,";
		for(AlgorithmType at : AlgorithmType.values()){
			bigH = bigH + at.toString() + ",,,";
		}
		for(AlgorithmType at : AlgorithmType.values()){
			smallH = smallH + "checkers,avg_time,std_time,";
		}
		return bigH + "\n" + smallH;
	}

}
