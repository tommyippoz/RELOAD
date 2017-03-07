/**
 * 
 */
package ippoz.multilayer.detector.commons.support;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * The Class PreferencesManager.
 *
 * @author Tommy
 */
public class PreferencesManager {
	
	/** The map of the preferences. */
	private HashMap<String, String> preferences;
	
	/**
	 * Instantiates a new preferences manager.
	 *
	 * @param prefFile the preferences source file
	 */
	public PreferencesManager(File prefFile){
		try {
			preferences = AppUtility.loadPreferences(prefFile, null);
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to load main preferences file");
		}
	}
	
	/**
	 * Instantiates a new preferences manager.
	 *
	 * @param filename the source file name
	 */
	public PreferencesManager(String filename) {
		this(new File(filename));
	}

	/**
	 * Gets the preference.
	 *
	 * @param tag the preference tag
	 * @return the preference value
	 */
	public String getPreference(String tag){
		return preferences.get(tag);
	}

}
