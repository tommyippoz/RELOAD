/**
 * 
 */
package ippoz.madness.detector.loader;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.detector.commons.datafetcher.DataFetcher;
import ippoz.madness.detector.commons.datafetcher.DatabaseFetcher;
import ippoz.madness.detector.commons.knowledge.data.MonitoredData;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.PreferencesManager;
import ippoz.madness.detector.commons.support.ThreadScheduler;
import ippoz.madness.detector.manager.TimingsManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	private List<Integer> expIDs;
	
	/** The data read by the loader. */
	private List<MonitoredData> readData;
	
	private List<LayerType> selectedLayers;
	
	/**
	 * Instantiates a new loader manager.
	 *
	 * @param list the experiments IDs
	 * @param tag the loader tag
	 * @param pManager the timings manager
	 * @param dbUsername the database username
	 * @param dbPassword the database password
	 */
	public MySQLLoader(List<Integer> list, PreferencesManager preferencesManager, String tag, String layersString, TimingsManager pManager) {
		super();
		this.tag = tag;
		this.pManager = pManager;
		this.expIDs = list;
		dbName = preferencesManager.getPreference(DB_NAME);
		dbUsername = preferencesManager.getPreference(DB_USERNAME);
		dbPassword = preferencesManager.getPreference(DB_PASSWORD);
		readData = new LinkedList<MonitoredData>();
		loadLayers(layersString);
	}
	
	private void loadLayers(String layersString) {
		selectedLayers = new LinkedList<LayerType>();
		if(layersString != null && layersString.length() > 0){
			for(String str : layersString.split(",")){
				str = str.trim();
				try {
					selectedLayers.add(LayerType.valueOf(str));
				} catch(Exception ex){
					AppLogger.logError(getClass(), "UnrecognizedLayer", "Unable to parse '" + str + "' layer");
				}
			}
		} else {
			for(LayerType lt : LayerType.values()){
				selectedLayers.add(lt);
			}
			AppLogger.logInfo(getClass(), selectedLayers.size() + " Default Layers Loaded");
		}
	}

	/**
	 * Starts fetching data.
	 * For all the experiment IDs, launch a fetching on the specified DataFetcher.
	 *
	 * @return the linked list
	 */
	@Override
	public List<MonitoredData> fetch(){
		long start = System.currentTimeMillis();
		try {
			start();
			join();
			if(pManager != null){
				if(tag.equals("train")){
					pManager.addTiming(TimingsManager.LOAD_TRAIN_TIME, (double)(System.currentTimeMillis() - start));
					pManager.addTiming(TimingsManager.AVG_LOAD_TRAIN_TIME, (double)((System.currentTimeMillis() - start)/threadNumber()));
				} else {
					pManager.addTiming(TimingsManager.LOAD_VALIDATION_TIME, (double)(System.currentTimeMillis() - start));
					pManager.addTiming(TimingsManager.AVG_LOAD_VALIDATION_TIME, (double)((System.currentTimeMillis() - start)/threadNumber()));	
				}
				AppLogger.logInfo(getClass(), "'" + tag + "' data loaded in " + (System.currentTimeMillis() - start) + " ms");
				AppLogger.logInfo(getClass(), "Average per run: " + ((System.currentTimeMillis() - start)/threadNumber()) + " ms");
			}
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
		List<DataFetcher> fetchList = new ArrayList<DataFetcher>(expIDs.size());
		for(Integer runId : expIDs){
			fetchList.add(new DatabaseFetcher(runId.toString(), dbName, dbUsername, dbPassword, selectedLayers));
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
		MonitoredData data = ((DataFetcher)t).getFetchedData();
		if(data != null && data.size() > 5)
			readData.add(data);
		((DataFetcher)t).flush();
	}

	@Override
	public String getRuns() {
		return expIDs.get(0) + " - " + expIDs.get(expIDs.size() - 1);
	}

}
