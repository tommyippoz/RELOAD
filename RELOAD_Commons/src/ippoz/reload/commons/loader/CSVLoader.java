/**
 * 
 */
package ippoz.reload.commons.loader;

import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
	public CSVLoader(File file, String toSkip, String labelCol, String faultyTags, String avoidTags, int anomalyWindow, String experimentRows, String runsString) {
		super(file, toSkip, labelCol, faultyTags, avoidTags, anomalyWindow, experimentRows, runsString);
	}	
	
	public CSVLoader(PreferencesManager prefManager, String tag, int anomalyWindow, String datasetsFolder, String runsString) {
		this(extractFile(prefManager, datasetsFolder, tag), 
				prefManager.getPreference(SKIP_COLUMNS), 
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
	public List<Indicator> loadHeader(){
		BufferedReader reader = null;
		String readLine = null;
		List<Indicator> csvHeader = null;
		try {
			csvHeader = new LinkedList<Indicator>();
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
					csvHeader.add(new Indicator(splitted.trim().replace("\"", ""), String.class));
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
	
	protected boolean hasFault(String string){
		if(faultyTagList == null)
			return false;
		for(String fault : faultyTagList){
			if(fault.toUpperCase().trim().compareTo(string.trim().toUpperCase()) == 0)
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#getName()
	 */
	@Override
	public String getLoaderName() {
		return "CSV - " + file.getName().substring(0, file.getName().indexOf("."));
	}
	
	/**
	 * Reads the CSV files.
	 */
	@Override
	protected void initialize() {
		BufferedReader reader = null;
		String readLine = null;
		List<Observation> obList = null;
		Map<DatasetIndex, InjectedElement> injMap = null;
		Observation current = null;
		int rowIndex = 0, i;
		double skipCount = 0;
		double itemCount = 0;
		double anomalyCount = 0;
		int currentBatchIndex = -1;
		try {
			dataList = new LinkedList<MonitoredData>();
			AppLogger.logInfo(getClass(), "Loading " + file.getPath());
			if(getBatchesNumber() > 0){
				if(file != null && !file.isDirectory() && file.exists()){
					reader = new BufferedReader(new FileReader(file));
					while(reader.ready() && readLine == null){
						readLine = reader.readLine();
						if(readLine != null){
							readLine = readLine.trim();
							if(readLine.replace(",", "").length() == 0 || isComment(readLine))
								readLine = null;
						}
					}
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
									i = 0;
									current = new Observation(dIndex, indicatorNumber);
									for(String splitted : readLine.split(",")){
										if(i < getHeader().size() && getHeader().get(i) != null){
											Double indData = 0.0;
											if(splitted != null && splitted.length() > 0){
												splitted = splitted.replace("\"", "").trim();
												if(AppUtility.isNumber(splitted)){
													indData = Double.parseDouble(splitted);
												}
											}
											current.addIndicator(indData);
										} 
										i++;
									}
									if(labelCol >= 0 && labelCol < readLine.split(",").length && readLine.split(",")[labelCol] != null) { 
										itemCount++;
										if(avoidTagList == null || !avoidTagList.contains(readLine.split(",")[labelCol])){
											obList.add(current);
											if(readLine.split(",")[labelCol] != null && hasFault(readLine.split(",")[labelCol])){
												anomalyCount++;
												injMap.put(dIndex, new InjectedElement(dIndex, readLine.split(",")[labelCol]));
											}
										} else skipCount++;
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
				//dataList = new ArrayList<>(dataList);
				
				// Setting up key variables
				setTotalDataPoints(rowIndex);
				setSkipRatio(100.0*skipCount/itemCount);
				setAnomalyRatio(100.0*anomalyCount/itemCount);
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
	protected List<LoaderBatch> getFeatureBatches(String featureName) {
		BufferedReader reader = null;
		String readLine = null;
		List<LoaderBatch> bList = new LinkedList<LoaderBatch>();
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
				int startIndex = 0;
				String featValue = null;
				int columnIndex = getFeatureIndex(featureName);
				while(reader.ready() && readLine != null){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !readLine.startsWith("*")){
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

}
