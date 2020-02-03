/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.data.IndicatorData;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.knowledge.data.Observation;
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
 * The Class CSVPreLoader. Class That allows loading MonitoredData from CSV files.
 *
 * @author Tommy
 */
public class CSVCompleteLoader extends CSVBaseLoader {
	
	/** The anomaly window. */
	private int anomalyWindow;
	
	/**
	 * Instantiates a new CSV pre loader.
	 *
	 * @param runs the runs
	 * @param csvFile the CSV file targeted
	 * @param skip the skip
	 * @param labelCol the label column
	 * @param expRunsString the experiment rows
	 * @param faultyTags the faulty tags
	 * @param avoidTags the avoid tags
	 * @param anomalyWindow the anomaly window
	 */
	public CSVCompleteLoader(List<Integer> runs, File csvFile, String toSkip, String labelColString, String expRunsString, String faultyTags, String avoidTags, int anomalyWindow) {
		super(runs, csvFile, toSkip, labelColString, expRunsString);
		this.anomalyWindow = anomalyWindow;
		parseFaultyTags(faultyTags);
		parseAvoidTags(avoidTags);
		readCsv();
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
	public CSVCompleteLoader(List<Integer> list, PreferencesManager prefManager, String tag, int anomalyWindow, String datasetsFolder) {
		this(list, 
				extractFile(prefManager, datasetsFolder, tag), 
				prefManager.getPreference(SKIP_COLUMNS), 
				prefManager.getPreference(LABEL_COLUMN), 
				extractExperimentRows(prefManager), 
				extractFaultyTags(prefManager, tag), 
				extractAvoidTags(prefManager, tag), 
				anomalyWindow);
	}	

	/**
	 * Reads the CSV files.
	 */
	private void readCsv() {
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
				while(reader.ready() && readLine == null){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.replace(",", "").length() == 0 || readLine.startsWith("*"))
							readLine = null;
					}
				}
				while(reader.ready()){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !readLine.startsWith("*")){
							if((AppUtility.isNumber(experimentRows) && rowIndex % Integer.parseInt(experimentRows) == 0) 
								|| (!AppUtility.isNumber(experimentRows) && ((!String.valueOf(expRowsColumns[0]).equals(String.valueOf(expRowsColumns[1])) && expRowsColumns[0] != null && expRowsColumns[1] != null) || (String.valueOf(expRowsColumns[0]).equals(String.valueOf(expRowsColumns[1])) && expRowsColumns[0] == null)))){ 
								if(obList != null && obList.size() > 0){
									dataList.add(new MonitoredData("Run_" + getRun(rowIndex-1, changes-1), obList, injList));
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
										if(splitted != null){
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
								if(labelCol >= 0 && labelCol < readLine.split(",").length && readLine.split(",")[labelCol] != null) { 
									if(avoidTagList == null || !avoidTagList.contains(readLine.split(",")[labelCol])){
										obList.add(current);
										if(readLine.split(",")[labelCol] != null && hasFault(readLine.split(",")[labelCol]))
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
				if(obList != null && obList.size() > 0){
					dataList.add(new MonitoredData("Run_" + getRun(rowIndex-1, changes-1), obList, injList));
				}
				AppLogger.logInfo(getClass(), "Read " + rowIndex + " rows.");
				reader.close();
			} else AppLogger.logError(getClass(), "FileNotFound", "File '" + file.getPath() + "' not found");
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
	}
	

	@Override
	public LoaderType getLoaderType() {
		return LoaderType.CSV;
	}

	@Override
	public boolean isComment(String readedString) {
		// TODO Auto-generated method stub
		return false;
	}

}
