/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.data.IndicatorData;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class ARFFLoader. Allows loading Knowledge from ARFF Files.
 *
 * @author Tommy
 */
public class ARFFLoader extends FileLoader {
	
	/** The anomaly window. */
	private int anomalyWindow;

	/**
	 * Instantiates a new ARFF loader.
	 *
	 * @param runs the runs
	 */
	public ARFFLoader(List<Integer> runs, File file, String toSkip, String labelCol, String experimentRows, String faultyTags, String avoidTags, int anomalyWindow) {
		super(runs, file, toSkip, labelCol, experimentRows);
		this.anomalyWindow = anomalyWindow;
		parseFaultyTags(faultyTags);
		parseAvoidTags(avoidTags);
		readARFF();
	}
	
	/**
	 * Instantiates a new CSV pre-loader.
	 *
	 * @param list the list
	 * @param prefManager the preferences manager
	 * @param tag the tag
	 * @param anomalyWindow the anomaly window
	 * @param datasetsFolder the datasets folder
	 */
	public ARFFLoader(List<Integer> list, PreferencesManager prefManager, String tag, int anomalyWindow, String datasetsFolder) {
		this(list, 
				extractFile(prefManager, datasetsFolder, tag), 
				prefManager.getPreference(SKIP_COLUMNS), 
				prefManager.getPreference(LABEL_COLUMN), 
				extractExperimentRows(prefManager), 
				extractFaultyTags(prefManager, tag), 
				extractAvoidTags(prefManager, tag), 
				anomalyWindow);
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
	public Object[] getSampleValuesFor(String featureName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Indicator> loadHeader() {
		BufferedReader reader = null;
		String readLine = null;
		List<Indicator> arffHeader = null;
		try {
			arffHeader = new LinkedList<Indicator>();
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
							arffHeader.add(new Indicator(readLine.split(" ")[1], LayerType.NO_LAYER, String.class));
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
	
	private void readARFF(){
		BufferedReader reader = null;
		String readLine = null;
		LinkedList<Observation> obList = null;
		LinkedList<InjectedElement> injList = null ;
		Observation current = null;
		String[] expRowsColumns = new String[]{null, null};
		int rowIndex = 0, i;
		int changes = 0;
		try {
			dataList = new LinkedList<MonitoredData>();
			AppLogger.logInfo(getClass(), "Loading " + file.getPath());
			if(file != null && !file.isDirectory() && file.exists()){
				reader = new BufferedReader(new FileReader(file));
				
				// Skips Header
				boolean flag = true;
				while(reader.ready() && flag){
					readLine = reader.readLine();
					if(readLine != null && !isComment(readLine)){
						readLine = readLine.trim();
						if(readLine.startsWith("@data"))
							flag = false;
					}
				}
				
				// Reads file
				while(reader.ready()){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !isComment(readLine)){
							if((AppUtility.isNumber(experimentRows) && rowIndex % Integer.parseInt(experimentRows) == 0) || 
									(!AppUtility.isNumber(experimentRows) 
										&& ((!String.valueOf(expRowsColumns[0]).equals(String.valueOf(expRowsColumns[1])) && expRowsColumns[0] != null && expRowsColumns[1] != null) 
												|| (String.valueOf(expRowsColumns[0]).equals(String.valueOf(expRowsColumns[1])) && expRowsColumns[0] == null)))){ 
								if(obList != null && obList.size() > 0){
									dataList.add(new MonitoredData("Run_" + getRun(rowIndex-1, changes), obList, injList));
								}
								injList = new LinkedList<InjectedElement>();
								obList = new LinkedList<Observation>();
							}
							readLine = AppUtility.filterInnerCommas(readLine);
							if(canReadFile(rowIndex, changes)){
								i = 0;
								current = new Observation(obList.size() > 0 ? obList.getLast().getTimestamp().getTime() + 1000 : System.currentTimeMillis());
								for(String splitted : readLine.split(",")){
									if(i < getHeader().size() && getHeader().get(i) != null){
										HashMap<DataCategory, String> iD = new HashMap<DataCategory, String>();
										if(splitted != null && splitted.length() > 0){
											splitted = splitted.replace("\"", "");
											if(AppUtility.isNumber(splitted)){
												iD.put(DataCategory.PLAIN, splitted);
												iD.put(DataCategory.DIFFERENCE, obList.size()>0 ? String.valueOf(Double.parseDouble(splitted) - Double.parseDouble(obList.getLast().getValue(getHeader().get(i), DataCategory.PLAIN))) : "0.0");
											} else {
												iD.put(DataCategory.PLAIN, "0");
												iD.put(DataCategory.DIFFERENCE, "0");
											}
										}
										current.addIndicator(getHeader().get(i), new IndicatorData(iD));
									} 
									i++;
								}
								if(labelCol < readLine.split(",").length && readLine.split(",")[labelCol] != null) { 
									if(avoidTagList == null || !avoidTagList.contains(readLine.split(",")[labelCol])){
										obList.add(current);
										if(readLine.split(",")[labelCol] != null && faultyTagList.contains(readLine.split(",")[labelCol]))
											injList.add(new InjectedElement(obList.getLast().getTimestamp(), readLine.split(",")[labelCol], anomalyWindow));
									}
								}	
							}
							if(!AppUtility.isNumber(experimentRows) && hasFeature(experimentRows)){
								expRowsColumns[0] = expRowsColumns[1];
								expRowsColumns[1] = readLine.split(",")[getFeatureIndex(experimentRows)];
								if(!String.valueOf(expRowsColumns[0]).equals(String.valueOf(expRowsColumns[1])) && expRowsColumns[0] != null && expRowsColumns[1] != null)
									changes++;
							}
							rowIndex++;
						}
					}
				}
				AppLogger.logInfo(getClass(), "Read " + rowIndex + " rows.");
				reader.close();
			} else AppLogger.logError(getClass(), "FileNotFound", "File '" + file.getPath() + "' not found");
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
	}

	@Override
	public double getAnomalyRate() {
		BufferedReader reader = null;
		String readLine = null;
		double anomalyCount = 0;
		double itemCount = 0;
		int rowIndex = 0;
		int changes = 0;
		String[] expRowsColumns = new String[]{null, null};
		try {
			if(file != null && !file.isDirectory() && file.exists() && labelCol >= 0 && faultyTagList != null && faultyTagList.size() > 0) {
				reader = new BufferedReader(new FileReader(file));

				// Skips Header
				boolean flag = true;
				while(reader.ready() && flag){
					readLine = reader.readLine();
					if(readLine != null && !isComment(readLine)){
						readLine = readLine.trim();
						if(readLine.startsWith("@data"))
							flag = false;
					}
				}
				
				// anomaly rate
				while(reader.ready()){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !isComment(readLine)){
							readLine = AppUtility.filterInnerCommas(readLine);
							if(canReadFile(rowIndex, changes)){
								if(labelCol < readLine.split(",").length && readLine.split(",")[labelCol] != null) { 
									itemCount++;
									if(avoidTagList == null || !avoidTagList.contains(readLine.split(",")[labelCol])){
										if(readLine.split(",")[labelCol] != null && faultyTagList.contains(readLine.split(",")[labelCol]))
											anomalyCount++;
									}
								}	
							}
							if(!AppUtility.isNumber(experimentRows) && hasFeature(experimentRows)){
								expRowsColumns[0] = expRowsColumns[1];
								expRowsColumns[1] = readLine.split(",")[getFeatureIndex(experimentRows)];
								if(!String.valueOf(expRowsColumns[0]).equals(String.valueOf(expRowsColumns[1])) && expRowsColumns[0] != null && expRowsColumns[1] != null)
									changes++;
							}
							rowIndex++;
						}
					}
				}
				reader.close();
			} else return 0.0;
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to parse header");
		}
		return 100.0*anomalyCount/itemCount;
	}
	
	@Override
	public double getSkipRate() {
		BufferedReader reader = null;
		String readLine = null;
		double skipCount = 0;
		double itemCount = 0;
		int rowIndex = 0;
		int changes = 0;
		String[] expRowsColumns = new String[]{null, null};
		try {
			if(file != null && file.exists() && labelCol >= 0 && avoidTagList != null && avoidTagList.size() > 0) {
				reader = new BufferedReader(new FileReader(file));

				// Skips Header
				boolean flag = true;
				while(reader.ready() && flag){
					readLine = reader.readLine();
					if(readLine != null && !isComment(readLine)){
						readLine = readLine.trim();
						if(readLine.startsWith("@data"))
							flag = false;
					}
				}
				
				// skip rate
				while(reader.ready()){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !isComment(readLine)){
							readLine = AppUtility.filterInnerCommas(readLine);
							if(canReadFile(rowIndex, changes)){
								if(readLine.split(",")[labelCol] != null) { 
									itemCount++;
									if(avoidTagList.contains(readLine.split(",")[labelCol])){
										skipCount++;
									}
								}	
							}
							if(!AppUtility.isNumber(experimentRows) && hasFeature(experimentRows)){
								expRowsColumns[0] = expRowsColumns[1];
								expRowsColumns[1] = readLine.split(",")[getFeatureIndex(experimentRows)];
								if(!String.valueOf(expRowsColumns[0]).equals(String.valueOf(expRowsColumns[1])) && expRowsColumns[0] != null && expRowsColumns[1] != null)
									changes++;
							}
							rowIndex++;
						}
					}
				}
				reader.close();
			} else return 0.0;
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		return 100.0*skipCount/itemCount;
	}
	
	@Override
	public boolean isComment(String readedString){
		return readedString != null && readedString.length() > 0 && readedString.startsWith("%");
	}

	@Override
	public int getRowNumber() {
		BufferedReader reader = null;
		String readLine = null;
		int rowIndex = 0;
		try {
			dataList = new LinkedList<MonitoredData>();
			AppLogger.logInfo(getClass(), "Loading " + file.getPath());
			if(file != null && !file.isDirectory() && file.exists()){
				reader = new BufferedReader(new FileReader(file));
				
				// Skips Header
				boolean flag = true;
				while(reader.ready() && flag){
					readLine = reader.readLine();
					if(readLine != null && !isComment(readLine)){
						readLine = readLine.trim();
						if(readLine.startsWith("@data"))
							flag = false;
					}
				}
				
				// Reads file
				while(reader.ready()){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !isComment(readLine)){
							rowIndex++;
						}
					}
				}
				reader.close();
			} else AppLogger.logError(getClass(), "FileNotFound", "File '" + file.getPath() + "' not found");
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse file");
		}
		return rowIndex;
	}

}
