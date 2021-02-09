/**
 * 
 */
package ippoz.reload.commons.loader;

import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.loader.info.DatasetInfo;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public abstract class FileLoader extends Loader {
	
	/** The Constant TRAIN_SKIP_ROWS. */
	public static final String TRAIN_SKIP_ROWS = "TRAIN_SKIP_ROWS";
	
	/** The Constant VALIDATION_SKIP_ROWS. */
	public static final String VALIDATION_SKIP_ROWS = "VALIDATION_SKIP_ROWS";

	/** The Constant SKIP_COLUMNS. */
	public static final String SKIP_COLUMNS = "SKIP_COLUMNS";

	/** The Constant LABEL_COLUMN. */
	public static final String LABEL_COLUMN = "LABEL_COLUMN";

	/** The Constant EXPERIMENT_ROWS. */
	public static final String EXPERIMENT_ROWS = "EXPERIMENT_ROWS";
	
	/** The Constant BATCH_COLUMN. */
	public static final String BATCH_COLUMN = "BATCH_INFO";
	
	/** The file. */
	protected File file;
	
	/** The label column. */
	protected int labelCol;
	
	/** The list of faulty tags. */
	protected List<String> faultyTagList;
	
	/** The list of tags to be avoided when reading. */
	protected List<String> avoidTagList;
	
	/** The anomaly window. */
	private int anomalyWindow;
	
	/** The amount of data points. */
	private int totalDataPoints;
	
	/** The anomaly ratio. */
	private double anomalyRatio;
	
	/** The skip ratio. */
	private double skipRatio;

	public FileLoader(File file, String[] toSkip, String labelColString, String faultyTags, String avoidTags, int anomalyWindow, String batchString, String runsString) {
		super();
		this.file = file;
		this.labelCol = extractHeaderIndexOf(labelColString);
		this.anomalyWindow = anomalyWindow;
		filterHeader(toSkip, labelColString);
		parseFaultyTags(faultyTags);
		parseAvoidTags(avoidTags);
		setBatches(deriveBatches(batchString, runsString));
		initialize();
		updateBatches();
	}
	
	@Override
	public DatasetInfo generateDatasetInfo(){
		return initialize();
	}

	private List<LoaderBatch> deriveBatches(String batchString, String runsString){
		int batchIndex = 0;
		String[] bList;
		List<LoaderBatch> fileBatchesFeature = null;
		List<LoaderBatch> outList = new LinkedList<LoaderBatch>();
		if(runsString != null && runsString.trim().length() > 0){
			if(runsString.contains(","))
				bList = runsString.split(",");
			else bList = new String[]{runsString};
			if(batchString != null && hasFeature(batchString.trim())){
				fileBatchesFeature = getFeatureBatches(batchString.trim());
			}
			for(String s : bList){
				s = s.trim();
				if(AppUtility.isNumber(s)){
					int n = Integer.parseInt(s);
					if(batchString == null || batchString.trim().length() == 0){
						outList.add(new LoaderBatch(new Integer(batchIndex), n, n));
					} else if(AppUtility.isNumber(batchString.trim())){
						int expRows = Integer.parseInt(batchString.trim());
						outList.add(new LoaderBatch(new Integer(n), n*expRows, (n+1)*expRows-1));
					} else if(hasFeature(batchString.trim())){
						outList.add(fileBatchesFeature.get(n));
					}
				} else if(s.contains("-") && AppUtility.isNumber(s.split("-")[0].trim()) && AppUtility.isNumber(s.split("-")[1].trim())){
					int n1 = Integer.parseInt(s.split("-")[0].trim());
					int n2 = Integer.parseInt(s.split("-")[1].trim());
					if(batchString == null || batchString.trim().length() == 0){
						outList.add(new LoaderBatch(new Integer(batchIndex), n1, n2));
					} else if(AppUtility.isNumber(batchString.trim())){
						int expRows = Integer.parseInt(batchString.trim());
						for(int i=n1;i<=n2;i++){
							outList.add(new LoaderBatch(new Integer(i), i*expRows, (i+1)*expRows-1));
						}
					} else if(hasFeature(batchString.trim())){
						for(int i=n1;i<=n2;i++){
							outList.add(fileBatchesFeature.get(i));
						}
					}
				} 
				batchIndex++;
			}
		} 
		outList = LoaderBatch.compactBatches(outList);
		return outList;
	}
	
	/**
	 * Reads the files.
	 */
	protected DatasetInfo initialize() {
		BufferedReader reader = null;
		List<Observation> obList = null;
		Map<DatasetIndex, InjectedElement> injMap = null;
		Boolean[] headBool = null;
		String[] headName = null;
		int rowIndex = 0;
		double skipCount = 0;
		double itemCount = 0;
		double anomalyCount = 0;
		int currentBatchIndex = -1;
		DatasetInfo dInfo = new DatasetInfo(getHeader());
		try {
			dataList = new LinkedList<MonitoredData>();
			headBool = getHeader().values().toArray(new Boolean[getHeader().values().size()]);
			headName = getHeader().keySet().toArray(new String[getHeader().keySet().size()]);
			AppLogger.logInfo(getClass(), "Loading " + file.getPath());
			if(getBatchesNumber() > 0){
				if(file != null && !file.isDirectory() && file.exists()){
					String readLine = null;
					reader = skipHeader();
					int indicatorNumber = getIndicatorNumber();
					while(reader.ready()){
						readLine = reader.readLine();
						if(readLine != null){
							readLine = readLine.trim();
							if(readLine.length() > 0 && !readLine.startsWith("*")){
								if(currentBatchIndex < 0 || (currentBatchIndex < getBatchesNumber() && getBatchIndex(rowIndex) >= 0 && currentBatchIndex != getBatchIndex(rowIndex))){
									if(obList != null && obList.size() > 0){
										dataList.add(new MonitoredData(getBatch(currentBatchIndex), new ArrayList<>(obList), injMap, getHeader()));
									}
									injMap = new HashMap<>();
									obList = new LinkedList<Observation>();
									currentBatchIndex++;
								}
								readLine = AppUtility.filterInnerCommas(readLine);
								if(canRead(rowIndex)/* && currentBatchIndex < getBatchesNumber()*/){
									DatasetIndex dIndex = new DatasetIndex(rowIndex);
									String[] splitLine = readLine.split(",");
									Observation current = new Observation(dIndex, indicatorNumber);
									int i = 0;
									boolean rowLabel = false;
									if(labelCol >= 0 && labelCol < splitLine.length && splitLine[labelCol] != null) { 
										itemCount++;
										if(avoidTagList == null || !avoidTagList.contains(splitLine[labelCol])){
											obList.add(current);
											rowLabel = hasFault(splitLine[labelCol]);
											if(readLine.split(",")[labelCol] != null && rowLabel){
												anomalyCount++;
												injMap.put(dIndex, new InjectedElement(dIndex, splitLine[labelCol]));
											}
										} else skipCount++;
									}
									for(String splitted : splitLine){
										if(i < headBool.length && headBool[i]){
											Double indData = 0.0;
											if(splitted != null && splitted.trim().length() > 0){	
												splitted = splitted.replace("\"", "").trim();
												if(AppUtility.isNumber(splitted)){
													indData = Double.parseDouble(splitted);
												}
											}
											current.addIndicator(indData);
											dInfo.addValue(headName[i], indData, rowLabel);
										}
										i++;
									}
								}
								rowIndex++;
							}
						}
					}
					if(obList != null && obList.size() > 0){
						dataList.add(new MonitoredData(getBatch(currentBatchIndex), obList, injMap, getHeader()));
					}
					AppLogger.logInfo(getClass(), "Read " + rowIndex + " rows.");
					reader.close();
				}

				// Setting up key variables
				dInfo.setDataPoints(rowIndex);
				setTotalDataPoints(rowIndex);
				dInfo.setAnomalyRatio(100.0*anomalyCount/itemCount);
				dInfo.setSkipRatio(100.0*skipCount/itemCount);
				setSkipRatio(100.0*skipCount/itemCount);
				setAnomalyRatio(100.0*anomalyCount/itemCount);
			} else AppLogger.logError(getClass(), "FileNotFound", "File '" + file.getPath() + "' not found");
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		return dInfo;
	}

	public List<Integer> readRunIds(String idPref){
		String from, to;
		List<Integer> idList = new LinkedList<Integer>();
		if(idPref != null && idPref.length() > 0){
			for(String id : idPref.split(",")){
				if(id.contains("-")){
					from = id.split("-")[0].trim();
					to = id.split("-")[1].trim();
					for(int i=Integer.parseInt(from);i<=Integer.parseInt(to);i++){
						idList.add(i);
					}
				} else idList.add(Integer.parseInt(id.trim()));
			}
		}
		return idList;
	}
	
	@Override
	public String getCompactName() {
		if(file != null && file.getName().contains("."))
			return file.getName().substring(0, file.getName().indexOf("."));
		else if (file != null)
			return file.getName();
		return "";
	}

	/**
	 * Parses the columns to be considered.
	 *
	 * @param colString the column string
	 * @return the integer[]
	 */
	protected Integer[] parseSkipColumns(String colString) {
		LinkedList<Integer> iList = new LinkedList<Integer>();
		if(colString != null && colString.length() > 0){
			for(String str : colString.split(",")){
				Integer newSkip = extractHeaderIndexOf(str.trim());
				if(newSkip >= 0)
					iList.add(newSkip);
			}
		} 
		if(labelCol >= 0)
			iList.add(labelCol);
		return iList.toArray(new Integer[iList.size()]);
	}
	
	protected int extractHeaderIndexOf(String labelColString) {
		Map<String, Boolean> header = getHeader();
		if(labelColString != null && header != null && header.size() > 0){
			labelColString = labelColString.trim();
			if(AppUtility.isNumber(labelColString))
				return Integer.parseInt(labelColString);
			else {
				int i = 0;
				for(String ind : header.keySet()){
					if(ind != null && ind.trim().toUpperCase().equals(labelColString.toUpperCase()))
						return i;
					i++;
				}
			}
		}
		return -1;
	}

	/**
	 * Extracts the CSV file to be read.
	 *
	 * @param prefManager the preferences manager
	 * @param datasetsFolder the datasets folder
	 * @param tag the tag
	 * @return the file
	 */
	public static File extractFile(PreferencesManager prefManager, String datasetsFolder, String tag){
		String filename = datasetsFolder;
		if(tag.equals("train")){
			if(prefManager.hasPreference(TRAIN_FILE))
				filename = filename + prefManager.getPreference(TRAIN_FILE);
			else filename = filename + prefManager.getPreference("TRAIN_" + prefManager.getPreference(Loader.LOADER_TYPE) + "_FILE");
		} else {
			if(prefManager.hasPreference(VALIDATION_FILE))
				filename = filename + prefManager.getPreference(VALIDATION_FILE);
			else filename = filename + prefManager.getPreference("VALIDATION_" + prefManager.getPreference(Loader.LOADER_TYPE) + "_FILE");
		}
		return new File(filename);
	}

	/**
	 * Extracts tags to avoid reading some rows of the CSV file.
	 *
	 * @param prefManager the preferences manager
	 * @param tag the tag
	 * @return the string
	 */
	protected static String extractAvoidTags(PreferencesManager prefManager, String tag) {
		if(tag.equals("train") && prefManager.hasPreference(TRAIN_SKIP_ROWS))
			return prefManager.getPreference(TRAIN_SKIP_ROWS);
		else if(!tag.equals("train") && prefManager.hasPreference(VALIDATION_SKIP_ROWS))
			return prefManager.getPreference(VALIDATION_SKIP_ROWS);
		else return prefManager.getPreference("SKIP_ROWS");
	}
	
	/**
	 * Parses the faulty tags.
	 *
	 * @param faultyTags the faulty tags
	 */
	protected void parseFaultyTags(String faultyTags) {
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
	protected void parseAvoidTags(String avoidTags) {
		avoidTagList = new LinkedList<String>();
		if(avoidTags != null && avoidTags.trim().length() > 0) {
			for(String str : avoidTags.split(",")){
				avoidTagList.add(str.trim());
			}
		}
	}

	@Override
	public double getMBSize() {
		return (file.length() / 1024) / 1024;
	}

	@Override
	public boolean canFetch() {
		return file != null && file.exists();
	}
	
	@Override
	public boolean hasBatches(String preferenceString) {
		if(preferenceString != null && preferenceString.trim().length() > 0){
			preferenceString = preferenceString.trim();
			if(AppUtility.isNumber(preferenceString)){
				if(Double.valueOf(preferenceString) > 0)
					return true;
				else return false;
			} else return this.hasFeature(preferenceString);
		} else return false;
	}
	
	protected int getAnomalyWindow() {
		return anomalyWindow;
	}
	
	public void setTotalDataPoints(int totalDataPoints) {
		this.totalDataPoints = totalDataPoints;
	}

	public void setAnomalyRatio(double anomalyRatio) {
		this.anomalyRatio = anomalyRatio;
	}
	
	public void setSkipRatio(double skipRatio) {
		this.skipRatio = skipRatio;
	}

	@Override
	public double getAnomalyRate() {
		return anomalyRatio;
	}

	@Override
	public double getSkipRate() {
		return skipRatio;
	}
	
	@Override
	public int getRowNumber() {
		return totalDataPoints;
	}
	
	public boolean isComment(String readedString) {
		return readedString != null && readedString.startsWith("*");
	}
	
	protected boolean hasFault(String string){
		if(faultyTagList == null)
			return false;
		for(String fault : faultyTagList){
			if(fault.toUpperCase().trim().compareTo(string.trim().toUpperCase()) == 0)
				return true;
		}
		return false;
	}
	
	public static String getBatchPreference(PreferencesManager pManager){
		if (pManager != null){
			if(pManager.hasPreference(BATCH_COLUMN))
				return pManager.getPreference(BATCH_COLUMN);
			else if(pManager.hasPreference(FileLoader.EXPERIMENT_ROWS))
				return pManager.getPreference(FileLoader.EXPERIMENT_ROWS);
		}
		return null;
	}
	
	public static String[] splitString(String toSplit, String sep){
		String[] splitted = toSplit != null && toSplit.trim().length() > 0 ? toSplit.split(sep) : new String[]{};
		for(int i=0;i<splitted.length;i++){
			splitted[i] = splitted[i].trim();
		}
		return splitted;
	}
	
	private List<LoaderBatch> getFeatureBatches(String featureName) {
		BufferedReader reader = null;
		String readLine = null;
		List<LoaderBatch> bList = new LinkedList<LoaderBatch>();
		try {
			if(file != null && file.exists() && hasFeature(featureName)){				
				reader = skipHeader();
				int rowCount = 0;
				int startIndex = 0;
				String featValue = null;
				int columnIndex = getFeatureIndex(featureName);
				while(reader.ready() && readLine != null){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !isComment(readLine)){
							String[] splitted = readLine.split(",");
							if(splitted.length > columnIndex){
								if(featValue == null || !featValue.equals(splitted[columnIndex])){
									if(featValue != null){
										bList.add(new LoaderBatch(new String(featValue + " (batch " + bList.size() + ")"), startIndex, rowCount-1));
									}
									featValue = splitted[columnIndex];
									startIndex = rowCount;
								}
							}
							rowCount++;
						}
					}
				}
				
				reader.close();
			}
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to get feature batches");
		}
		return new ArrayList<>(bList);
	}
	
	protected abstract BufferedReader skipHeader() throws IOException;
	
}
