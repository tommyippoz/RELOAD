/**
 * 
 */
package ippoz.reload.commons.loader;

import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.PreferencesManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class ARFFLoader. Allows loading Knowledge from ARFF Files.
 *
 * @author Tommy
 */
public class ARFFLoader extends FileLoader {

	/**
	 * Instantiates a new ARFF loader.
	 *
	 * @param runs the runs
	 */
	public ARFFLoader(File file, String[] toSkip, String labelCol, String faultyTags, String avoidTags, int anomalyWindow, String experimentRows, String runsString) {
		super(file, toSkip, labelCol, faultyTags, avoidTags, anomalyWindow, experimentRows, runsString);
	}
	
	/**
	 * Instantiates a new ARFF loader.
	 *
	 * @param list the list
	 * @param prefManager the preferences manager
	 * @param tag the tag
	 * @param anomalyWindow the anomaly window
	 * @param datasetsFolder the datasets folder
	 */
	public ARFFLoader(PreferencesManager prefManager, String tag, int anomalyWindow, String datasetsFolder, String runsString) {
		this(extractFile(prefManager, datasetsFolder, tag), 
				splitString(prefManager.getPreference(SKIP_COLUMNS), ","),  
				prefManager.getPreference(LABEL_COLUMN), 
				extractFaultyTags(prefManager, tag), 
				extractAvoidTags(prefManager, tag), 
				anomalyWindow,
				FileLoader.getBatchPreference(prefManager),
				runsString);
	}
	
	@Override
	public LoaderType getLoaderType() {
		return LoaderType.ARFF;
	}

	@Override
	public String getLoaderName() {
		return "ARFF - " + file.getName().split(".")[0];
	}

	@Override
	public Map<String, Boolean> loadHeader() {
		BufferedReader reader = null;
		String readLine = null;
		Map<String, Boolean> arffHeader = null;
		try {
			arffHeader = new LinkedHashMap<>();
			if(file != null && file.exists() && !file.isDirectory()){
				reader = new BufferedReader(new FileReader(file));
				
				// Looks for @relation
				boolean flag = true;
				while(reader.ready() && flag){
					readLine = reader.readLine();
					if(readLine != null && !isComment(readLine)){
						readLine = readLine.trim();
						if(readLine.startsWith("@relation"))
							flag = false;
					}
				}
			
				// Stores header until it finds @data or end of file
				flag = true;
				while(reader.ready() && flag){
					readLine = reader.readLine();
					if(readLine != null && !isComment(readLine)){
						readLine = readLine.trim();
						if(readLine.startsWith("@data"))
							flag = false;
						else if(readLine.startsWith("@attribute") && readLine.split(" ").length >= 3){
							arffHeader.put(readLine.split(" ")[1].trim(), true);
						}
						
					}
				}
				
				// Closes the flow
				reader.close();
			}
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		
		return arffHeader;
	}
	
	@Override
	public boolean isComment(String readedString){
		return readedString != null && readedString.length() > 0 && readedString.startsWith("%");
	}

	@Override
	protected BufferedReader skipHeader() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		boolean flag = true;
		while(reader.ready() && flag){
			String readLine = reader.readLine();
			if(readLine != null && !isComment(readLine)){
				readLine = readLine.trim();
				if(readLine.startsWith("@data"))
					flag = false;
			}
		}
		return reader;
	}

}
