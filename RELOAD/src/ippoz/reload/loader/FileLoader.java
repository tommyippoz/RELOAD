/**
 * 
 */
package ippoz.reload.loader;

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
	public static final String TRAIN_EXPERIMENT_ROWS = "EXPERIMENT_ROWS";

	public static final String TRAIN_EXPERIMENT_SPLIT_ROWS = "EXPERIMENT_SPLIT_ROWS";
	
	/** The Constant EXPERIMENT_ROWS. */
	public static final String VALIDATION_EXPERIMENT_ROWS = "VALIDATION_EXPERIMENT_ROWS";

	public static final String VALIDATION_EXPERIMENT_SPLIT_ROWS = "VALIDATION_EXPERIMENT_SPLIT_ROWS";
	
	/** The file. */
	protected File file;
	
	/** The label column. */
	protected int labelCol;
	
	/** The experiment rows. If <= 0, it indicates the column that indicates experiment rows to change */
	protected int experimentRows;
	
	/** The list of faulty tags. */
	protected List<String> faultyTagList;
	
	/** The list of tags to be avoided when reading. */
	protected List<String> avoidTagList;

	public FileLoader(List<Integer> runs, File file, int labelCol, int experimentRows) {
		super(runs, null);
		this.file = file;
		this.labelCol = labelCol;
		this.experimentRows = experimentRows;
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
		if(experimentRows > 0)
			return rowIndex / experimentRows;
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
	
	protected static int extractExperimentRows(PreferencesManager prefManager){
		return prefManager.hasPreference(TRAIN_EXPERIMENT_ROWS) && Integer.parseInt(prefManager.getPreference(TRAIN_EXPERIMENT_ROWS)) > 0  
				? Integer.parseInt(prefManager.getPreference(TRAIN_EXPERIMENT_ROWS)) : -Integer.parseInt(prefManager.getPreference(TRAIN_EXPERIMENT_ROWS));
	}

	public abstract int getRowNumber();

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
		return getRunsNumber()*experimentRows;
	}	
	
	public abstract boolean isComment(String readedString);
	
}
