/**
 * 
 */
package ippoz.reload.loader;

import ippoz.madness.commons.datacategory.DataCategory;
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
public class CSVPreLoader extends CSVLoader {

	/** The Constant TRAIN_CSV_FILE. */
	public static final String TRAIN_CSV_FILE = "TRAIN_CSV_FILE";
	
	/** The Constant TRAIN_FAULTY_TAGS. */
	public static final String TRAIN_FAULTY_TAGS = "TRAIN_FAULTY_TAGS";
	
	/** The Constant TRAIN_SKIP_ROWS. */
	public static final String TRAIN_SKIP_ROWS = "TRAIN_SKIP_ROWS";

	/** The Constant VALIDATION_CSV_FILE. */
	public static final String VALIDATION_CSV_FILE = "VALIDATION_CSV_FILE";
	
	/** The Constant VALIDATION_FAULTY_TAGS. */
	public static final String VALIDATION_FAULTY_TAGS = "VALIDATION_FAULTY_TAGS";
	
	/** The Constant VALIDATION_SKIP_ROWS. */
	public static final String VALIDATION_SKIP_ROWS = "VALIDATION_SKIP_ROWS";

	/** The Constant SKIP_COLUMNS. */
	public static final String SKIP_COLUMNS = "SKIP_COLUMNS";

	/** The Constant LABEL_COLUMN. */
	public static final String LABEL_COLUMN = "LABEL_COLUMN";

	/** The Constant EXPERIMENT_ROWS. */
	public static final String EXPERIMENT_ROWS = "EXPERIMENT_ROWS";
	
	/** The list of MonitoredData. */
	private List<MonitoredData> dataList;
	
	/** The list of faulty tags. */
	private List<String> faultyTagList;
	
	/** The list of tags to be avoided when reading. */
	private List<String> avoidTagList;
	
	/** The anomaly window. */
	private int anomalyWindow;
	
	/**
	 * Instantiates a new CSV pre loader.
	 *
	 * @param runs the runs
	 * @param csvFile the CSV file targeted
	 * @param skip the skip
	 * @param labelCol the label column
	 * @param experimentRows the experiment rows
	 * @param faultyTags the faulty tags
	 * @param avoidTags the avoid tags
	 * @param anomalyWindow the anomaly window
	 */
	public CSVPreLoader(List<Integer> runs, File csvFile, Integer[] skip, int labelCol, int experimentRows, String faultyTags, String avoidTags, int anomalyWindow) {
		super(runs, csvFile, skip, labelCol, experimentRows);
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
	public CSVPreLoader(List<Integer> list, PreferencesManager prefManager, String tag, int anomalyWindow, String datasetsFolder) {
		this(list, extractFile(prefManager, datasetsFolder, tag), parseColumns(prefManager.getPreference(SKIP_COLUMNS)), Integer.parseInt(prefManager.getPreference(LABEL_COLUMN)), Integer.parseInt(prefManager.getPreference(EXPERIMENT_ROWS)), extractFaultyTags(prefManager, tag), extractAvoidTags(prefManager, tag), anomalyWindow);
	}
		/**
	 * Extracts faulty tags from preferences.
	 *
	 * @param prefManager the preferences manager
	 * @param tag the tag
	 * @return the string resembling faulty tags
	 */
	private static String extractFaultyTags(PreferencesManager prefManager, String tag) {
		if(tag.equals("train") && prefManager.hasPreference(TRAIN_FAULTY_TAGS))
			return prefManager.getPreference(TRAIN_FAULTY_TAGS);
		else if(!tag.equals("train") && prefManager.hasPreference(VALIDATION_FAULTY_TAGS))
			return prefManager.getPreference(VALIDATION_FAULTY_TAGS);
		else return prefManager.getPreference("FAULTY_TAGS");
	}
	
	/**
	 * Extracts tags to avoid reading some rows of the CSV file.
	 *
	 * @param prefManager the preferences manager
	 * @param tag the tag
	 * @return the string
	 */
	private static String extractAvoidTags(PreferencesManager prefManager, String tag) {
		if(tag.equals("train") && prefManager.hasPreference(TRAIN_SKIP_ROWS))
			return prefManager.getPreference(TRAIN_SKIP_ROWS);
		else if(!tag.equals("train") && prefManager.hasPreference(VALIDATION_SKIP_ROWS))
			return prefManager.getPreference(VALIDATION_SKIP_ROWS);
		else return prefManager.getPreference("SKIP_ROWS");
	}
	
	/**
	 * Extracts the CSV file to be read.
	 *
	 * @param prefManager the preferences manager
	 * @param datasetsFolder the datasets folder
	 * @param tag the tag
	 * @return the file
	 */
	private static File extractFile(PreferencesManager prefManager, String datasetsFolder, String tag){
		String filename = datasetsFolder;
		filename = datasetsFolder + prefManager.getPreference(tag.equals("train") ? TRAIN_CSV_FILE : VALIDATION_CSV_FILE);
		return new File(filename);
	}	
	
	/**
	 * Parses the faulty tags.
	 *
	 * @param faultyTags the faulty tags
	 */
	private void parseFaultyTags(String faultyTags) {
		faultyTagList = new LinkedList<String>();
		for(String str : faultyTags.split(",")){
			faultyTagList.add(str.trim());
		}
	}
	
	/**
	 * Parses the tags to avoid reading some rows of the CSV files.
	 *
	 * @param avoidTags the avoid tags
	 */
	private void parseAvoidTags(String avoidTags) {
		avoidTagList = new LinkedList<String>();
		if(avoidTags != null && avoidTags.trim().length() > 0) {
			for(String str : avoidTags.split(",")){
				avoidTagList.add(str.trim());
			}
		}
	}
	
	/**
	 * Parses the columns to be considered.
	 *
	 * @param colString the column string
	 * @return the integer[]
	 */
	private static Integer[] parseColumns(String colString) {
		LinkedList<Integer> iList = new LinkedList<Integer>();
		if(colString != null && colString.length() > 0){
			for(String str : colString.split(",")){
				iList.add(new Integer(str.trim()));
			}
			return iList.toArray(new Integer[iList.size()]);
		} else return new Integer[]{};
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
			AppLogger.logInfo(getClass(), "Loading " + csvFile.getPath());
			if(csvFile != null && csvFile.exists()){
				reader = new BufferedReader(new FileReader(csvFile));
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
							if((experimentRows > 0 && rowIndex % experimentRows == 0) || (experimentRows <= 0 && ((!String.valueOf(expRowsColumns[0]).equals(String.valueOf(expRowsColumns[1])) && expRowsColumns[0] != null && expRowsColumns[1] != null) || (String.valueOf(expRowsColumns[0]).equals(String.valueOf(expRowsColumns[1])) && expRowsColumns[0] == null)))){ 
								if(obList != null && obList.size() > 0){
									dataList.add(new MonitoredData("Run_" + getRun(rowIndex-1, dataList.size()), obList, injList));
								}
								injList = new LinkedList<InjectedElement>();
								obList = new LinkedList<Observation>();
							}
							readLine = AppUtility.filterInnerCommas(readLine);
							if(canReadCSV(rowIndex, changes)){
								i = 0;
								current = new Observation(obList.size() > 0 ? obList.getLast().getTimestamp().getTime() + 1000 : System.currentTimeMillis());
								for(String splitted : readLine.split(",")){
									if(i < header.size() && header.get(i) != null){
										HashMap<DataCategory, String> iD = new HashMap<DataCategory, String>();
										if(splitted != null && splitted.length() > 0){
											splitted = splitted.replace("\"", "");
											if(AppUtility.isNumber(splitted)){
												iD.put(DataCategory.PLAIN, splitted);
												iD.put(DataCategory.DIFFERENCE, obList.size()>0 ? String.valueOf(Double.parseDouble(splitted) - Double.parseDouble(obList.getLast().getValue(header.get(i), DataCategory.PLAIN))) : "0.0");
											} else {
												iD.put(DataCategory.PLAIN, "0");
												iD.put(DataCategory.DIFFERENCE, "0");
											}
										}
										current.addIndicator(header.get(i), new IndicatorData(iD));
									} 
									i++;
								}
								if(readLine.split(",")[labelCol] != null) { 
									if(avoidTagList == null || !avoidTagList.contains(readLine.split(",")[labelCol])){
										obList.add(current);
										if(readLine.split(",")[labelCol] != null && faultyTagList.contains(readLine.split(",")[labelCol]))
											injList.add(new InjectedElement(obList.getLast().getTimestamp(), readLine.split(",")[labelCol], anomalyWindow));
									}
								}	
							}
							if(experimentRows <= 0 && readLine.split(",").length > -experimentRows){
								expRowsColumns[0] = expRowsColumns[1];
								expRowsColumns[1] = readLine.split(",")[-experimentRows];
								if(!String.valueOf(expRowsColumns[0]).equals(String.valueOf(expRowsColumns[1])) && expRowsColumns[0] != null && expRowsColumns[1] != null)
									changes++;
							}
							rowIndex++;
						}
					}
				}
				AppLogger.logInfo(getClass(), "Read " + rowIndex + " rows.");
				reader.close();
			} else AppLogger.logError(getClass(), "FileNotFound", "File '" + csvFile.getPath() + "' not found");
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#fetch()
	 */
	@Override
	public List<MonitoredData> fetch() {
		return dataList;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#getRuns()
	 */
	@Override
	public String getRuns() {
		String tag, endTag;
		if(dataList != null){
			tag = dataList.get(0).getDataTag();
			if(dataList.size() == 1){
				return "[" + tag.substring(tag.indexOf("_") + 1) + "]";
			} else {
				endTag = dataList.get(dataList.size()-1).getDataTag();				
				return "[" + tag.substring(tag.indexOf("_") + 1) + "-" + endTag.substring(endTag.indexOf("_") + 1) + "]";
			}
		}
		else return null;
	}	

}
