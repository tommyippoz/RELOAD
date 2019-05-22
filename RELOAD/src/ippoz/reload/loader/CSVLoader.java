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
public abstract class CSVLoader extends SimpleLoader {

	/** The CSV file. */
	protected File csvFile;
	
	/** The label column. */
	protected int labelCol;
	
	/** The experiment rows. */
	protected int experimentRows;
	
	/** The header. */
	protected List<Indicator> header;

	/**
	 * Instantiates a new CSV loader.
	 *
	 * @param runs the runs
	 * @param csvFile the CSV file
	 * @param skip the skip columns
	 * @param labelCol the label column
	 * @param experimentRows the experiment rows
	 */
	protected CSVLoader(List<Integer> runs, File csvFile, Integer[] skip, int labelCol, int experimentRows) {
		super(runs);
		this.csvFile = csvFile;
		this.labelCol = labelCol;
		this.experimentRows = experimentRows;
		loadHeader(skip);
	}
	
	/**
	 * Loads the header of the file.
	 *
	 * @param skip columns to be skipped
	 */
	private void loadHeader(Integer[] skip){
		BufferedReader reader = null;
		String readLine = null;
		try {
			header = new LinkedList<Indicator>();
			if(csvFile != null && csvFile.exists()){
				reader = new BufferedReader(new FileReader(csvFile));
				while(reader.ready() && readLine == null){
					readLine = reader.readLine();
					if(readLine != null){
						readLine = readLine.trim();
						if(readLine.length() == 0 || readLine.startsWith("*"))
							readLine = null;
					}
				}
				readLine = AppUtility.filterInnerCommas(readLine);
				for(String splitted : readLine.split(",")){
					if(!occursIn(skip, header.size()))
						header.add(new Indicator(splitted.replace("\"", ""), LayerType.NO_LAYER, Double.class));
					else header.add(null);
				}
				reader.close();
			}
		} catch (IOException ex){
			AppLogger.logException(getClass(), ex, "unable to parse header");
		}
		
	}	

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.SimpleLoader#canRead(int)
	 */
	@Override
	public boolean canRead(int index) {
		return super.canRead(getRun(index));
	}

	/**
	 * Gets the runs to be used to load the CSV file.
	 *
	 * @param rowIndex the row index
	 * @return the run
	 */
	protected int getRun(int rowIndex){
		return rowIndex / experimentRows;
	}
	
	/**
	 * True if item should be skipped (occurs in the 'skip' list).
	 *
	 * @param skip the skip
	 * @param item the item
	 * @return true, if successful
	 */
	private static boolean occursIn(Integer[] skip, int item){
		for(int i=0;i<skip.length;i++){
			if(skip[i] == item)
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#getName()
	 */
	@Override
	public String getName() {
		return "CSV - " + csvFile.getName().split(".")[0];
	}

}
