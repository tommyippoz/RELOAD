/**
 * 
 */
package ippoz.reload.commons.loader;

import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class CSVLoader. Embeds some of the functionalities to read CSV files.
 *
 * @author Tommy
 */
public class CSVLoader extends FileLoader {

	/**
	 * Instantiates a new CSV loader.
	 *
	 * @param runs the runs
	 * @param csvFile the CSV file
	 * @param parseSkipColumns(colString)toSkip the skip columns
	 * @param labelCol the label column
	 * @param expRunsString the experiment rows
	 */
	public CSVLoader(File file, String[] toSkip, String labelCol, String faultyTags, String avoidTags, int anomalyWindow, String experimentRows, String runsString) {
		super(file, toSkip, labelCol, faultyTags, avoidTags, anomalyWindow, experimentRows, runsString);
	}	
	
	public CSVLoader(PreferencesManager prefManager, String tag, int anomalyWindow, String datasetsFolder, String runsString) {
		this(extractFile(prefManager, datasetsFolder, tag), 
				splitString(prefManager.getPreference(SKIP_COLUMNS), ","), 
				prefManager.getPreference(LABEL_COLUMN), 
				extractFaultyTags(prefManager, tag), 
				extractAvoidTags(prefManager, tag), 
				anomalyWindow,
				FileLoader.getBatchPreference(prefManager), 
				runsString);
	}	

	/**
	 * Loads the header of the file.
	 *
	 */
	@Override
	public Map<String, Boolean> loadHeader(){
		BufferedReader reader = null;
		String readLine = null;
		Map<String, Boolean> csvHeader = null;
		try {
			csvHeader = new LinkedHashMap<>();
			if(file != null && file.exists() && !file.isDirectory()){
				reader = new BufferedReader(new FileReader(file));
				while(reader.ready() && readLine == null){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.replace(",", "").length() == 0 || isComment(readLine))
							readLine = null;
					}
				}
				readLine = AppUtility.filterInnerCommas(readLine);
				for(String splitted : readLine.split(",")){
					csvHeader.put(splitted.trim().replace("\"", ""), true);
				}
				reader.close();
			}
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		
		return csvHeader;
		
	}	
	
	/*@Override
	public Object[] getSampleValuesFor(String featureName) {
		BufferedReader reader = null;
		String readLine = null;
		Object[] values = new Object[Loader.SAMPLE_VALUES_COUNT];
		try {
			if(file != null && file.exists() && hasFeature(featureName)){
				
				reader = new BufferedReader(new FileReader(file));
				
				//skip header
				while(reader.ready() && readLine == null){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.replace(",", "").length() == 0 || readLine.startsWith("*"))
							readLine = null;
					}
				}
				
				// read data
				int rowCount = 0;
				int columnIndex = getFeatureIndex(featureName);
				while(reader.ready() && readLine == null && rowCount < Loader.SAMPLE_VALUES_COUNT){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !readLine.startsWith("*")){
							String[] splitted = readLine.split(",");
							if(splitted.length > columnIndex)
								values[rowCount] = splitted[columnIndex];
							rowCount++;
						}
					}
				}
				
				reader.close();
			}
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		
		return values;
	}*/

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#getName()
	 */
	@Override
	public String getLoaderName() {
		return "CSV - " + file.getName().substring(0, file.getName().indexOf("."));
	}
	
	

	@Override
	public LoaderType getLoaderType() {
		return LoaderType.CSV;
	}

	@Override
	protected BufferedReader skipHeader() throws IOException {
		String readLine = null;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while(reader.ready() && readLine == null){
			readLine = reader.readLine();
			if(readLine != null){
				readLine = readLine.trim();
				if(readLine.replace(",", "").length() == 0 || isComment(readLine))
					readLine = null;
			}
		}
		return reader;
	}

}
