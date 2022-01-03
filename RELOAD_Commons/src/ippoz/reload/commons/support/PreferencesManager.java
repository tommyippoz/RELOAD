/**
 * 
 */
package ippoz.reload.commons.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class PreferencesManager.
 *
 * @author Tommy
 */
public class PreferencesManager {
	
	/** The map of the preferences. */
	protected HashMap<String, String> preferences;
	
	private File file;
	
	/**
	 * Instantiates a new preferences manager.
	 *
	 * @param prefFile the preferences source file
	 */
	public PreferencesManager(File prefFile){
		try {
			if(prefFile != null && prefFile.exists()){
				preferences = AppUtility.loadPreferences(prefFile, null);
				file = prefFile;
			} else preferences = null;
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
	
	public void refresh() throws IOException{
		preferences = AppUtility.loadPreferences(file, null);
	}

	public boolean hasPreference(String loaderPrefFile) {
		return preferences != null && preferences.containsKey(loaderPrefFile);
	}

	public boolean isValidFile() {
		return preferences != null;
	}

	public String getFilename() {
		return file.getName();
	}
	
	public File getFile() {
		return file;
	}

	public void updatePreference(String tag, String newValue, boolean updateFile) {
		if(hasPreference(tag)){
			preferences.put(tag, newValue);
			if(updateFile)
				updatePreferencesFile(tag, newValue);
		}
	}
	
	public void updatePreference(String tag, String newValue, boolean createNew, boolean updateFile) {
		if(createNew || hasPreference(tag)){
			preferences.put(tag, newValue);
			if(updateFile)
				updatePreferencesFile(tag, newValue);
		}
	}

	private void updatePreferencesFile(String tag, String newValue) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		String readed;
		boolean found = false;
		List<String> fileLines = new LinkedList<String>();
		try {
			if(file.exists() && newValue != null){
				reader = new BufferedReader(new FileReader(file));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && (readed.trim().startsWith(tag + " ") || readed.trim().startsWith(tag + "="))){
							fileLines.add(tag + " = " + newValue.trim());
							found = true;
						} else fileLines.add(readed);
					}
				}
				reader.close();
				if(!found)
					fileLines.add(tag + " = " + newValue.trim());
				writer = new BufferedWriter(new FileWriter(file));
				for(String st : fileLines){
					writer.write(st + "\n");
				}
				writer.close();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		} 		
	}

	public void updateToFile() {
		for(String tag : preferences.keySet()){
			updatePreferencesFile(tag, preferences.get(tag));
		}
	}

	public String getCompactFilename() {
		String extendedName = getFilename();
		int index = extendedName.indexOf('.');
		return getFilename().substring(0, index);
	}

	public void renameLoader(String newName) {
		String newPath = file.getPath();
		newPath = newPath.replace(file.getName(), newName);
		if(!newPath.endsWith(".loader"))
			newPath = newPath + ".loader";
		File newFile = new File(newPath);
		AppUtility.copyFile(file, newFile);
		file.delete();
		file = newFile;
	}

}
