/**
 * 
 */
package ippoz.reload.loader;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.reload.commons.support.PreferencesManager;

import java.io.File;
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
		// TODO Auto-generated method stub
		return null;
	}
	
	private void readARFF(){
		
	}

}
