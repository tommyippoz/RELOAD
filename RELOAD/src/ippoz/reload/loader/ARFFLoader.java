/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
	public ARFFLoader(List<Integer> runs, File file, Integer[] skip, int labelCol, int experimentRows, String faultyTags, String avoidTags, int anomalyWindow) {
		super(runs, file, labelCol, experimentRows);
		this.anomalyWindow = anomalyWindow;
		filterHeader(skip);
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
				parseColumns(prefManager.getPreference(SKIP_COLUMNS)), 
				Integer.parseInt(prefManager.getPreference(LABEL_COLUMN)), 
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
				while(reader.ready() && readLine == null){
					readLine = reader.readLine();
					if(readLine != null && !isComment(readLine)){
						readLine = readLine.trim();
						if(readLine.replace(",", "").length() == 0 || readLine.startsWith("*"))
							readLine = null;
					}
				}
				readLine = AppUtility.filterInnerCommas(readLine);
				for(String splitted : readLine.split(",")){
					arffHeader.add(new Indicator(splitted.replace("\"", ""), LayerType.NO_LAYER, String.class));
				}
				reader.close();
			}
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		
		return arffHeader;
	}
	
	private void readARFF(){
		
	}

	@Override
	public double getAnomalyRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSkipRate() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean isComment(String readedString){
		return readedString != null && readedString.length() > 0 && readedString.startsWith("%");
	}

	@Override
	public int getRowNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

}
