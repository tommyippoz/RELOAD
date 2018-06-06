/**
 * 
 */
package ippoz.madness.detector.executable;

import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.knowledge.sliding.SlidingPolicy;
import ippoz.madness.detector.commons.knowledge.sliding.SlidingPolicyType;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.commons.support.PreferencesManager;
import ippoz.madness.detector.manager.DetectionManager;
import ippoz.madness.detector.manager.InputManager;
import ippoz.madness.detector.metric.Metric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
	
	/**
	 * The main method.
	 *
	 * @param args the default console arguments: currently not used
	 */
	public static void main(String[] args) {
		List<DetectionManager> dmList;
		InputManager iManager;
		try {
			if(new File(DEFAULT_PREF_FILE).exists()){
				dmList = new LinkedList<DetectionManager>();
				iManager = new InputManager(new PreferencesManager(DEFAULT_PREF_FILE));
				for(PreferencesManager loaderPref : readLoaders(iManager)){
					for(List<AlgorithmType> aList : readAlgorithmCombinations(iManager)){
						if(hasSliding(aList)){
							for(Integer windowSize : readWindowSizes(iManager)){
								for(SlidingPolicy sPolicy : readSlidingPolicies(iManager)){
									dmList.add(new DetectionManager(iManager, aList, loaderPref, windowSize, sPolicy));
								}
							}
						} else {
							dmList.add(new DetectionManager(iManager, aList, loaderPref));
						}
					}
				}
				AppLogger.logInfo(DetectorMain.class, dmList.size() + " MADneSs instances found.");
				for(int i=0;i<dmList.size();i++){
					AppLogger.logInfo(DetectorMain.class, "Running MADneSs [" + (i+1) + "/" + dmList.size() + "]: '" + dmList.get(i).getTag() + "'");
					runMADneSs(dmList.get(i));
				}
			} else {
				AppLogger.logError(DetectorMain.class, "PreferencesError", "Unable to properly load '" + DEFAULT_PREF_FILE + "' preferences.");
			}
		} catch(Exception ex) {
			AppLogger.logException(DetectorMain.class, ex, "");
		}
	}
	
	private static List<SlidingPolicy> readSlidingPolicies(InputManager iManager) {
		List<SlidingPolicy> wList = new LinkedList<SlidingPolicy>();
		String wPref = iManager.getSlidingPolicies();
		if(wPref != null && wPref.trim().length() > 0){
			for(String s : wPref.trim().split(",")){
				try {
					wList.add(SlidingPolicy.getPolicy(SlidingPolicyType.valueOf(s.trim())));
				} catch(Exception ex){
					AppLogger.logError(DetectorMain.class, "ParsingError", "Policy '" + s + "' unrecognized");
				}
			}
		}
		return wList;
	}

	private static List<Integer> readWindowSizes(InputManager iManager) {
		List<Integer> wList = new LinkedList<Integer>();
		String wPref = iManager.getSlidingWindowSizes();
		if(wPref != null && wPref.trim().length() > 0){
			for(String s : wPref.trim().split(",")){
				s = s.trim();
				if(AppUtility.isInteger(s)){
					wList.add(Integer.parseInt(s));
				}
			}
		}
		return wList;
	}

	private static List<PreferencesManager> readLoaders(InputManager iManager) {
		List<PreferencesManager> lList = new LinkedList<PreferencesManager>();
		String lPref = iManager.getLoaders();
		PreferencesManager pManager;
		if(lPref != null && lPref.trim().length() > 0){
			for(String s : lPref.trim().split(",")){
				s = s.trim();
				if(s.endsWith(".loader")){
					pManager = iManager.getLoaderPreferences(s);
					if(pManager != null)
						lList.add(pManager);
				} else if(new File(iManager.getLoaderFolder() + s).exists() && new File(iManager.getLoaderFolder() + s).isDirectory()){
					s = iManager.getLoaderFolder() + s; 
					if(!s.endsWith("" + File.separatorChar))
						s = s + File.separatorChar;
					for(File f : new File(s).listFiles()){
						if(f.getName().endsWith(".loader")){
							lList.add(new PreferencesManager(s + f.getName()));
						}
					}
				}
			}
		}
		return lList;
	}
	
	private static List<List<AlgorithmType>> readAlgorithmCombinations(InputManager iManager) {
		File algTypeFile = new File(iManager.getSetupFolder() + "algorithmPreferences.preferences");
		List<List<AlgorithmType>> alList = new LinkedList<List<AlgorithmType>>();
		BufferedReader reader;
		String readed;
		try {
			if(algTypeFile.exists()){
				reader = new BufferedReader(new FileReader(algTypeFile));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*")){
							List<AlgorithmType> aList = new LinkedList<AlgorithmType>();
							for(String s : readed.trim().split(",")){
								try {
									aList.add(AlgorithmType.valueOf(s.trim()));
								} catch(Exception ex){
									AppLogger.logError(DetectorMain.class, "ParsingError", "Algorithm '" + s + "' unrecognized");
								}
							}
							if(aList.size() > 0)
								alList.add(aList);
						}
					}
				}
				reader.close();
			} else {
				AppLogger.logError(DetectorMain.class, "MissingPreferenceError", "File " + 
						algTypeFile.getPath() + " not found. Will be generated. Using default value of 'ELKI_KMEANS'");
				List<AlgorithmType> aList = new LinkedList<AlgorithmType>();
				aList.add(AlgorithmType.ELKI_KMEANS);
				alList.add(aList);
				iManager.generateDefaultAlgorithmPreferences();
			}
		} catch(Exception ex){
			AppLogger.logException(DetectorMain.class, ex, "Unable to read data types");
		}
		return alList;
	}

	private static boolean hasSliding(List<AlgorithmType> aList) {
		for(AlgorithmType at : aList){
			if(at.toString().toUpperCase().contains("SLIDING"))
				return true;
		}
		return false;
	}

	private static void runMADneSs(DetectionManager dManager){
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
			report(dManager.getWritableTag(), dManager.evaluate(), dManager.getMetrics());
		}
		dManager.flush();
		dManager = null;
		AppLogger.logInfo(DetectorMain.class, "Done.");
	}

	private static void report(String madnessInfo, String[] result, Metric[] metrics){
		File drFile = new File(DEFAULT_REPORT_FILE);
		BufferedWriter writer;
		try {
			if(!drFile.exists()){
				writer = new BufferedWriter(new FileWriter(drFile, false));
				writer.write("* Report for MADneSs activity on " + new Date(System.currentTimeMillis()) + "\n");
				writer.write("dataset,runs,algorithm,window_size,window_policy,setup,metric_score");
				for(Metric met : metrics){
					writer.write("," + met.getMetricName());
				}
				writer.write("\n");
			} else {
				writer = new BufferedWriter(new FileWriter(drFile, true));
			}
			writer.write(madnessInfo + "," + result[1] + "," + result[0] + "," + result[2] + "\n");
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(DetectorMain.class, ex, "Unable to report");
		}
	}
	
}
