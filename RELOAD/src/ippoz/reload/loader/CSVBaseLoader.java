/**
 * 
 */
package ippoz.reload.loader;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.commons.layers.LayerType;
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
			if(file != null && file.exists()){
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

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#getName()
	 */
	@Override
	public String getLoaderName() {
		return "CSV - " + file.getName().split(".")[0];
	}

}
