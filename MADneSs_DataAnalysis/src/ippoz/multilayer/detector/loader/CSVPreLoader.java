/**
 * 
 */
package ippoz.multilayer.detector.loader;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.data.IndicatorData;
import ippoz.multilayer.detector.commons.data.Observation;
import ippoz.multilayer.detector.commons.failure.InjectedElement;
import ippoz.multilayer.detector.commons.support.PreferencesManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class CSVPreLoader extends CSVLoader {
	
	private static final String FILTERING_CSV_FILE = "FILTERING_CSV_FILE";

	private static final String TRAIN_CSV_FILE = "TRAIN_CSV_FILE";

	private static final String VALIDATION_CSV_FILE = "VALIDATION_CSV_FILE";

	private static final String SKIP_COLUMNS = "SKIP_COLUMNS";

	private static final String LABEL_COLUMN = "LABEL_COLUMN";

	private static final String EXPERIMENT_ROWS = "EXPERIMENT_ROWS";
	
	private static final String FAULTY_TAGS = "FAULTY_TAGS";
	
	private LinkedList<ExperimentData> dataList;
	private LinkedList<String> faultyTagList;
	private int anomalyWindow;
	
	public CSVPreLoader(LinkedList<Integer> runs, PreferencesManager prefManager, String tag, int anomalyWindow) {
		this(runs, new File(prefManager.getPreference(tag.equals("filtering") ? FILTERING_CSV_FILE : tag.equals("train") ? TRAIN_CSV_FILE : VALIDATION_CSV_FILE)), parseColumns(prefManager.getPreference(SKIP_COLUMNS)), Integer.parseInt(prefManager.getPreference(LABEL_COLUMN)), Integer.parseInt(prefManager.getPreference(EXPERIMENT_ROWS)), prefManager.getPreference(FAULTY_TAGS), anomalyWindow);
	}

	public CSVPreLoader(LinkedList<Integer> runs, File csvFile, Integer[] skip, int labelCol, int experimentRows, String faultyTags, int anomalyWindow) {
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
			dataList = new LinkedList<ExperimentData>();
			if(csvFile != null && csvFile.exists()){
				reader = new BufferedReader(new FileReader(csvFile));
				readLine = reader.readLine();
				while(reader.ready()){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0){
							if(rowIndex % experimentRows == 0){ 
								if(obList != null && obList.size() > 0){
									dataList.add(new ExperimentData("Run_" + getRun(rowIndex-1), obList, injList, null));
								}
								injList = new LinkedList<InjectedElement>();
								obList = new LinkedList<Observation>();
							}
							if(canRead(rowIndex)){
								i = 0;
								current = new Observation(obList.size() > 0 ? obList.getLast().getTimestamp().getTime() + 1000 : System.currentTimeMillis());
								for(String splitted : readLine.split(",")){
									if(i < header.size() && header.get(i) != null){
										HashMap<DataCategory, String> iD = new HashMap<DataCategory, String>();
										iD.put(DataCategory.PLAIN, splitted);
										iD.put(DataCategory.DIFFERENCE, obList.size()>0 ? String.valueOf(Double.parseDouble(splitted) - Double.parseDouble(obList.getLast().getValue(header.get(i), DataCategory.PLAIN))) : "0.0");
										current.addIndicator(header.get(i), new IndicatorData(iD));
									} 
									i++;
								}
								obList.add(current);
								if(readLine.split(",")[labelCol] != null && faultyTagList.contains(readLine.split(",")[labelCol]))
									injList.add(new InjectedElement(obList.getLast().getTimestamp(), readLine.split(",")[labelCol], anomalyWindow));
							}
							rowIndex++;
						}
					}
				}
				reader.close();
			}
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
	}

	@Override
	public LinkedList<ExperimentData> fetch() {
		return dataList;
	}
	
	

}
