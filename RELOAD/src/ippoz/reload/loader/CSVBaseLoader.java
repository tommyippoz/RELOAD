/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class CSVLoader. Embeds some of the functionalities to read CSV files.
 *
 * @author Tommy
 */
public abstract class CSVBaseLoader extends FileLoader {

	/**
	 * Instantiates a new CSV loader.
	 *
	 * @param runs the runs
	 * @param csvFile the CSV file
	 * @param skip the skip columns
	 * @param labelCol the label column
	 * @param experimentRows the experiment rows
	 */
	protected CSVBaseLoader(List<Integer> runs, File csvFile, Integer[] skip, int labelCol, int experimentRows) {
		super(runs, csvFile, labelCol, experimentRows);
		filterHeader(skip);
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
						if(readLine.replace(",", "").length() == 0 || readLine.startsWith("*"))
							readLine = null;
					}
				}
				readLine = AppUtility.filterInnerCommas(readLine);
				for(String splitted : readLine.split(",")){
					csvHeader.add(new Indicator(splitted.replace("\"", ""), LayerType.NO_LAYER, String.class));
				}
				reader.close();
			}
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		
		return csvHeader;
		
	}	
	
	@Override
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
				//skip header
				while(reader.ready() && readLine == null){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.replace(",", "").length() == 0 || readLine.startsWith("*"))
							readLine = null;
					}
				}
				// anomaly rate
				while(reader.ready()){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !readLine.startsWith("*")){
							readLine = AppUtility.filterInnerCommas(readLine);
							if(canReadCSV(rowIndex, changes)){
								if(labelCol < readLine.split(",").length && readLine.split(",")[labelCol] != null) { 
									itemCount++;
									if(avoidTagList == null || !avoidTagList.contains(readLine.split(",")[labelCol])){
										if(readLine.split(",")[labelCol] != null && faultyTagList.contains(readLine.split(",")[labelCol]))
											anomalyCount++;
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
				//skip header
				while(reader.ready() && readLine == null){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.replace(",", "").length() == 0 || readLine.startsWith("*"))
							readLine = null;
					}
				}
				// skip rate
				while(reader.ready()){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !readLine.startsWith("*")){
							readLine = AppUtility.filterInnerCommas(readLine);
							if(canReadCSV(rowIndex, changes)){
								if(readLine.split(",")[labelCol] != null) { 
									itemCount++;
									if(avoidTagList.contains(readLine.split(",")[labelCol])){
										skipCount++;
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
				reader.close();
			} else return 0.0;
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		return 100.0*skipCount/itemCount;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#getName()
	 */
	@Override
	public String getLoaderName() {
		return "CSV - " + file.getName().split(".")[0];
	}
	
	@Override
	public int getRowNumber() {
		BufferedReader reader = null;
		String readLine = null;
		int rowCount = 0;
		try {
			if(file != null && !file.isDirectory() && file.exists()){
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
				rowCount = 1;
				while(reader.ready()){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() > 0 && !readLine.startsWith("*")){
							rowCount++;
						}
					}
				}
				reader.close();
			} else return 0;
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		return rowCount;
	}

}
