/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class FileLoader extends SimpleLoader {
	
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
	public static final String BATCH_COLUMN = "BATCH_COLUMN";
	
	/** The file. */
	protected File file;
	
	/** The label column. */
	protected int labelCol;
	
	/** The experiment rows. If String, it indicates the column that indicates experiment rows to change */
	protected String experimentRows;
	
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

	public FileLoader(List<Integer> runs, File file, String toSkip, String labelColString, String expRunsString, String faultyTags, String avoidTags, int anomalyWindow) {
		super(runs, null);
		this.file = file;
		this.labelCol = extractIndexOf(labelColString);
		this.experimentRows = expRunsString != null ? expRunsString.trim() : null;
		this.anomalyWindow = anomalyWindow;
		filterHeader(parseSkipColumns(toSkip));
		parseFaultyTags(faultyTags);
		parseAvoidTags(avoidTags);
		initialize();
	}
	
	protected abstract void initialize();
	
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
				Integer newSkip = extractIndexOf(str.trim());
				if(newSkip >= 0)
					iList.add(newSkip);
			}
		} 
		if(labelCol >= 0)
			iList.add(labelCol);
		/*if(!AppUtility.isNumber(experimentRows) && extractIndexOf(experimentRows) >= 0) 
			iList.add(extractIndexOf(experimentRows));*/
		return iList.toArray(new Integer[iList.size()]);
	}
	
	protected int extractIndexOf(String labelColString) {
		List<Indicator> header = getHeader();
		if(labelColString != null && header != null && header.size() > 0){
			labelColString = labelColString.trim();
			if(AppUtility.isNumber(labelColString))
				return Integer.parseInt(labelColString);
			else {
				int i = 0;
				for(Indicator ind : header){
					if(ind.getName().trim().toUpperCase().equals(labelColString.toUpperCase()))
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
	 * Gets the runs to be used to load the CSV file.
	 *
	 * @param rowIndex the row index
	 * @return the run
	 */
	protected int getRun(int rowIndex, int pastChanges){
		if(AppUtility.isInteger(experimentRows))
			return rowIndex / Integer.valueOf(experimentRows);
		else return pastChanges;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.loader.SimpleLoader#canRead(int)
	 */
	public boolean canReadFile(int index, int pastChanges) {
		return canRead(getRun(index, pastChanges));
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
	
	protected static String extractExperimentRows(PreferencesManager prefManager){
		if(prefManager.hasPreference(BATCH_COLUMN))
			return prefManager.getPreference(EXPERIMENT_ROWS);
			else return null;
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
	public int getDataPoints() {
		if(AppUtility.isInteger(experimentRows))
			return getRunsNumber()*Integer.parseInt(experimentRows);
		else return getRunsNumber();
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
	
}
