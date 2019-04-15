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
import ippoz.reload.commons.support.PreferencesManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class CSVPreLoader extends CSVLoader {
	
	public static final String FILTERING_CSV_FILE = "FILTERING_CSV_FILE";

	public static final String TRAIN_CSV_FILE = "TRAIN_CSV_FILE";

	public static final String VALIDATION_CSV_FILE = "VALIDATION_CSV_FILE";

	public static final String SKIP_COLUMNS = "SKIP_COLUMNS";

	public static final String LABEL_COLUMN = "LABEL_COLUMN";

	public static final String EXPERIMENT_ROWS = "EXPERIMENT_ROWS";
	
	public static final String FAULTY_TAGS = "FAULTY_TAGS";
	
	private List<MonitoredData> dataList;
	
	private List<String> faultyTagList;
	
	private int anomalyWindow;
	
	public CSVPreLoader(List<Integer> list, PreferencesManager prefManager, String tag, int anomalyWindow, String datasetsFolder) {
		this(list, new File(datasetsFolder + prefManager.getPreference(tag.equals("filtering") ? FILTERING_CSV_FILE : tag.equals("train") ? TRAIN_CSV_FILE : VALIDATION_CSV_FILE)), parseColumns(prefManager.getPreference(SKIP_COLUMNS)), Integer.parseInt(prefManager.getPreference(LABEL_COLUMN)), Integer.parseInt(prefManager.getPreference(EXPERIMENT_ROWS)), prefManager.getPreference(FAULTY_TAGS), anomalyWindow);
	}

	public CSVPreLoader(List<Integer> runs, File csvFile, Integer[] skip, int labelCol, int experimentRows, String faultyTags, int anomalyWindow) {
		super(runs, csvFile, skip, labelCol, experimentRows);
		this.anomalyWindow = anomalyWindow;
		parseFaultyTags(faultyTags);
		readCsv();
	}
	
	private void parseFaultyTags(String faultyTags) {
		faultyTagList = new LinkedList<String>();
		for(String str : faultyTags.split(",")){
			faultyTagList.add(str.trim());
		}
	}
	
	private static Integer[] parseColumns(String colString) {
		LinkedList<Integer> iList = new LinkedList<Integer>();
		if(colString != null && colString.length() > 0){
			for(String str : colString.split(",")){
				iList.add(new Integer(str.trim()));
			}
			return iList.toArray(new Integer[iList.size()]);
		} else return new Integer[]{};
	}

	private void readCsv() {
		BufferedReader reader = null;
		String readLine = null;
		LinkedList<Observation> obList = null;
		LinkedList<InjectedElement> injList = null ;
		Observation current = null;
		int rowIndex = 0, i;
		try {
			dataList = new LinkedList<MonitoredData>();
			if(csvFile != null && csvFile.exists()){
				reader = new BufferedReader(new FileReader(csvFile));
				while(reader.ready() && readLine == null){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() == 0 || readLine.startsWith("*"))
							readLine = null;
					}
				}
				while(reader.ready()){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !readLine.startsWith("*")){
							if(rowIndex % experimentRows == 0){ 
								if(obList != null && obList.size() > 0){
									dataList.add(new MonitoredData("Run_" + getRun(rowIndex-1), obList, injList));
								}
								injList = new LinkedList<InjectedElement>();
								obList = new LinkedList<Observation>();
							}
							if(canRead(rowIndex)){
								i = 0;
								current = new Observation(obList.size() > 0 ? obList.getLast().getTimestamp().getTime() + 1000 : System.currentTimeMillis());
								readLine = filterInnerCommas(readLine);
								for(String splitted : readLine.split(",")){
									if(i < header.size() && header.get(i) != null){
										HashMap<DataCategory, String> iD = new HashMap<DataCategory, String>();
										iD.put(DataCategory.PLAIN, splitted.replace("\"", ""));
										iD.put(DataCategory.DIFFERENCE, obList.size()>0 ? String.valueOf(Double.parseDouble(splitted.replace("\"", "")) - Double.parseDouble(obList.getLast().getValue(header.get(i), DataCategory.PLAIN))) : "0.0");
										current.addIndicator(header.get(i), new IndicatorData(iD));
									} 
									i++;
								}
								obList.add(current);
								//System.out.println(readLine);
								if(readLine.split(",")[labelCol] != null && faultyTagList.contains(readLine.split(",")[labelCol]))
									injList.add(new InjectedElement(obList.getLast().getTimestamp(), readLine.split(",")[labelCol], anomalyWindow));
							}
							rowIndex++;
						}
					}
				}
				reader.close();
			} else AppLogger.logError(getClass(), "FileNotFound", "File '" + csvFile.getPath() + "' not found");
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
	}

	@Override
	public List<MonitoredData> fetch() {
		return dataList;
	}

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
