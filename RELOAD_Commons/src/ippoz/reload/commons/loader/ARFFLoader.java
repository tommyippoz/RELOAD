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
	public ARFFLoader(File file, String toSkip, String labelCol, String faultyTags, String avoidTags, int anomalyWindow, String experimentRows, String runsString) {
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
				prefManager.getPreference(SKIP_COLUMNS), 
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
							arffHeader.add(new Indicator(readLine.split(" ")[1], String.class));
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
	protected void initialize(){
		BufferedReader reader = null;
		String readLine = null;
		List<Observation> obList = null;
		Map<DatasetIndex, InjectedElement> injMap = null ;
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
					
					int indicatorNumber = getIndicatorNumber();
					// Reads file
					while(reader.ready()){
						readLine = reader.readLine();
						if(readLine != null){
							readLine = readLine.trim();
							if(readLine.length() > 0 && !isComment(readLine)){
								if(currentBatchIndex < 0 || (currentBatchIndex < getBatchesNumber() && getBatchIndex(rowIndex) >= 0 && currentBatchIndex != getBatchIndex(rowIndex))){
									if(obList != null && obList.size() > 0){
										dataList.add(new MonitoredData(getBatch(currentBatchIndex), obList, injMap, getHeader()));
									}
									injMap = new HashMap<>();
									obList = new LinkedList<Observation>();
									currentBatchIndex++;
								}
								readLine = AppUtility.filterInnerCommas(readLine);
								if(canRead(rowIndex)){
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
									if(labelCol < readLine.split(",").length && readLine.split(",")[labelCol] != null) { 
										itemCount++;
										if(avoidTagList == null || !avoidTagList.contains(readLine.split(",")[labelCol])){
											obList.add(current);
											if(readLine.split(",")[labelCol] != null && faultyTagList.contains(readLine.split(",")[labelCol])){
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
				dataList = new ArrayList<>(dataList);
				
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
	public boolean isComment(String readedString){
		return readedString != null && readedString.length() > 0 && readedString.startsWith("%");
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
				boolean flag = true;
				while(reader.ready() && flag){
					readLine = reader.readLine();
					if(readLine != null && !isComment(readLine)){
						readLine = readLine.trim();
						if(readLine.startsWith("@data"))
							flag = false;
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
