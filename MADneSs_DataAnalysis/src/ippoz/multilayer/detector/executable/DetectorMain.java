/**
 * 
 */
package ippoz.multilayer.detector.executable;

import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.PreferencesManager;
import ippoz.multilayer.detector.manager.DetectionManager;

/**
 * The Class DetectorMain.
 * This is the main class of the project that calls the main manager (DetectionManager). 
 * After reading the preferences, starts the training and then the evaluation phase.
 *
 * @author Tommy
 */
public class DetectorMain { 

	/**
	 * The main method.
	 *
	 * @param args the default console arguments: currently not used
	 */
	public static void main(String[] args) {
		PreferencesManager prefManager;
		DetectionManager dManager;
		try {
			prefManager = new PreferencesManager("detector.preferences");
			AppLogger.logInfo(DetectorMain.class, "Preferences Loaded");
			dManager = new DetectionManager(prefManager);
			if(dManager.checkPremises()){
				if(dManager.needTest()) {
					AppLogger.logInfo(DetectorMain.class, "Starting Train Process");
					dManager.train();
				} 
				AppLogger.logInfo(DetectorMain.class, "Starting Evaluation Process");
				dManager.evaluate();
			}
			AppLogger.logInfo(DetectorMain.class, "Done.");
		} catch(Exception ex) {
			AppLogger.logException(DetectorMain.class, ex, "");
		}
	}
	
}
