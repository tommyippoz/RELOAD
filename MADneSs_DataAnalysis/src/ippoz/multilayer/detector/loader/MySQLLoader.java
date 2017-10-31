/**
 * 
 */
package ippoz.multilayer.detector.loader;

import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.datafetcher.DataFetcher;
import ippoz.multilayer.detector.commons.datafetcher.DatabaseFetcher;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.PreferencesManager;
import ippoz.multilayer.detector.commons.support.ThreadScheduler;
import ippoz.multilayer.detector.manager.TimingsManager;

import java.util.LinkedList;

/**
 * The Class LoaderManager.
 * The manager responsible of the loading of experimental data. Uses implemented DataFetchers
 *
 * @author Tommy
 */
public class MySQLLoader extends ThreadScheduler implements Loader {
	
	/** The Constant DB_USERNAME. */
	private static final String DB_NAME = "DB_NAME";
	
	/** The Constant DB_USERNAME. */
	private static final String DB_USERNAME = "DB_USERNAME";
	
	/** The Constant DB_PASSWORD. */
	private static final String DB_PASSWORD = "DB_PASSWORD";
	
	/** The experiment tag. */
	private String tag;
	
	/** The database name. */
	private String dbName;
	
	/** The database username. */
	private String dbUsername;
	
	/** The database password. */
	private String dbPassword;
	
	/** The timings manager. */
	private TimingsManager pManager;
	
	/** The list of experiment IDs. */
	private LinkedList<Integer> expIDs;
	
	/** The data read by the loader. */
	private LinkedList<ExperimentData> readData;
	
	/**
	 * Instantiates a new loader manager.
	 *
	 * @param expIDs the experiments IDs
	 * @param tag the loader tag
	 * @param pManager the timings manager
	 * @param dbUsername the database username
	 * @param dbPassword the database password
	 */
	public MySQLLoader(LinkedList<Integer> expIDs, PreferencesManager preferencesManager, String tag, TimingsManager pManager) {
		super();
		this.tag = tag;
		dbName = preferencesManager.getPreference(DB_NAME);
		dbUsername = preferencesManager.getPreference(DB_USERNAME);
		dbPassword = preferencesManager.getPreference(DB_PASSWORD);
		this.pManager = pManager;
		this.expIDs = expIDs;
		readData = new LinkedList<ExperimentData>();
	}
	
	/**
	 * Starts fetching data.
	 * For all the experiment IDs, launch a fetching on the specified DataFetcher.
	 *
	 * @return the linked list
	 */
	@Override
	public LinkedList<ExperimentData> fetch(){
		long start = System.currentTimeMillis();
		try {
			start();
			join();
			if(tag.equals("train")){
				pManager.addTiming(TimingsManager.LOAD_TRAIN_TIME, (double)(System.currentTimeMillis() - start));
				pManager.addTiming(TimingsManager.AVG_LOAD_TRAIN_TIME, (double)((System.currentTimeMillis() - start)/threadNumber()));
			} else {
				pManager.addTiming(TimingsManager.LOAD_VALIDATION_TIME, (double)(System.currentTimeMillis() - start));
				pManager.addTiming(TimingsManager.AVG_LOAD_VALIDATION_TIME, (double)((System.currentTimeMillis() - start)/threadNumber()));	
			}
			AppLogger.logInfo(getClass(), "'" + tag + "' data loaded in " + (System.currentTimeMillis() - start) + " ms");
			AppLogger.logInfo(getClass(), "Average per run: " + ((System.currentTimeMillis() - start)/threadNumber()) + " ms");
		} catch (InterruptedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to complete training phase");
		}
		return readData;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#initRun()
	 */
	@Override
	protected void initRun() {
		LinkedList<DataFetcher> fetchList = new LinkedList<DataFetcher>();
		for(Integer runId : expIDs){
			fetchList.add(new DatabaseFetcher(runId.toString(), dbName, dbUsername, dbPassword));
		}
		setThreadList(fetchList);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#threadStart(java.lang.Thread, int)
	 */
	@Override
	protected void threadStart(Thread t, int tIndex) {
		((DataFetcher)t).openConnection();
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.support.ThreadScheduler#threadComplete(java.lang.Thread, int)
	 */
	@Override
	protected void threadComplete(Thread t, int tIndex) {
		ExperimentData data = ((DataFetcher)t).getFetchedData();
		if(data.getSnapshotNumber() > 5)
			readData.add(data);
		((DataFetcher)t).flush();
	}

}
