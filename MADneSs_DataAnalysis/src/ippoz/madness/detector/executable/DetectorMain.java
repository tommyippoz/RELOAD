/**
 * 
 */
package ippoz.madness.detector.executable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.PreferencesManager;
import ippoz.madness.detector.manager.DetectionManager;

/**
 * The Class DetectorMain.
 * This is the main class of the project that calls the main manager (DetectionManager). 
 * After reading the preferences, starts the training and then the evaluation phase.
 *
 * @author Tommy
 */
public class DetectorMain { 
	
	private static final String DEFAULT_REPORT_FILE = "madnessreport.csv";
	
	private static final String DEFAULT_PREF_FILE = "madness.preferences";
	
	private static final String DEFAULT_MULTIPLE_PREF_FILE = "multiplemadness.preferences";
	
	/**
	 * The main method.
	 *
	 * @param args the default console arguments: currently not used
	 */
	public static void main(String[] args) {
		List<String> prefList = null;
		PreferencesManager prefManager;
		DetectionManager dManager;
		try {
			if(isMultipleRun()){
				prefList = loadMultiplePreferences();
				if(prefList.size() == 0){
					AppLogger.logError(DetectorMain.class, "PreferencesError", "Unable to properly load '" + DEFAULT_MULTIPLE_PREF_FILE + "' preferences. Single default '" + DEFAULT_PREF_FILE + "' will be used.");
					prefList.add(DEFAULT_PREF_FILE);
				}
			} else {
				prefList = new LinkedList<String>();
				prefList.add(DEFAULT_PREF_FILE);
			}
			for(String prefName : prefList){
				prefManager = new PreferencesManager(prefName);
				AppLogger.logInfo(DetectorMain.class, "[" + (prefList.indexOf(prefName)+1) + "/" + prefList.size() + "] Preference '" + prefName + "' Loaded");
				dManager = new DetectionManager(prefManager);
				if(dManager.checkAssumptions()){
					if(dManager.needFiltering()){
						AppLogger.logInfo(DetectorMain.class, "Starting Filtering Process");
						dManager.filterIndicators();
					}
					if(dManager.needTest()) {
						AppLogger.logInfo(DetectorMain.class, "Starting Train Process");
						dManager.train();
					} 
					AppLogger.logInfo(DetectorMain.class, "Starting Evaluation Process");
					report(prefName, dManager.evaluate());
				}
				dManager.flush();
				AppLogger.logInfo(DetectorMain.class, "Done.");
			}
		} catch(Exception ex) {
			AppLogger.logException(DetectorMain.class, ex, "");
		}
	}
	
	private static void report(String prefName, String[] result){
		File drFile = new File(DEFAULT_REPORT_FILE);
		BufferedWriter writer;
		try {
			if(!drFile.exists()){
				writer = new BufferedWriter(new FileWriter(drFile, false));
				writer.write("* Report for MADneSs activity on " + new Date(System.currentTimeMillis()) + "\n");
				writer.write("pref_filename,setup,metric_score\n");
			} else {
				writer = new BufferedWriter(new FileWriter(drFile, true));
			}
			writer.write(prefName + "," + result[1] + "," + result[0] + "\n");
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(DetectorMain.class, ex, "Unable to report");
		}
	}
	
	private static List<String> loadMultiplePreferences() {
		File mpFile = new File(DEFAULT_MULTIPLE_PREF_FILE);
		List<String> toReturn = new LinkedList<String>();
		BufferedReader reader;
		String readed;
		try {
			if(!mpFile.exists())
				return toReturn;
			reader = new BufferedReader(new FileReader(mpFile));
			while(reader.ready()){
				readed = reader.readLine();
				if(readed != null){
					readed = readed.trim();
					if(readed.length() > 0 && !readed.trim().startsWith("*") && readed.trim().endsWith(".preferences")){
						toReturn.add(readed.trim());
					}
				}
			}
			AppLogger.logInfo(DetectorMain.class, "Preferences loaded: " + toReturn.size());
			reader.close();
		} catch(Exception ex){
			AppLogger.logException(DetectorMain.class, ex, "Unable to read multiple preferences");
		}
		return toReturn;
	}

	private static boolean isMultipleRun(){
		return new File(DEFAULT_MULTIPLE_PREF_FILE).exists();
	}
	
}
