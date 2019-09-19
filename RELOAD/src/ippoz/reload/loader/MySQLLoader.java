/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.datafetcher.DataFetcher;
import ippoz.reload.commons.datafetcher.DatabaseFetcher;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.PreferencesManager;
import ippoz.reload.commons.support.ThreadScheduler;
import ippoz.reload.manager.TimingsManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class MySQLLoader.
 * The manager responsible of loading MYSQL data.
 *
 * @author Tommy
 */
public class MySQLLoader extends ThreadScheduler implements Loader {
	
	/** The Constant DB_USERNAME. */
	public static final String DB_NAME = "DB_NAME";
	
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
	
	/** The selected layers. */
	private List<LayerType> selectedLayers;
	
	/**
	 * Instantiates a new loader manager.
	 *
	 * @param list the experiments IDs
	 * @param preferencesManager the preferences manager
	 * @param tag the loader tag
	 * @param layersString the layers string
	 * @param pManager the timings manager
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
	
	/**
	 * Loads the layers of indicators/features.
	 *
	 * @param layersString the layers string
	 */
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

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#getRuns()
	 */
	@Override
	public String getRuns() {
		return expIDs.get(0) + " - " + expIDs.get(expIDs.size() - 1);
	}

	@Override
	public LoaderType getLoaderType() {
		return LoaderType.MYSQL;
	}

	@Override
	public String getLoaderName() {
		return "MYSQL";
	}

	@Override
	public String[] getFeatureNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getSampleValuesFor(String featureName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRowNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMBSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean canFetch() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getAnomalyRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Integer> getLoaderRuns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getSkipRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDataPoints() {
		// TODO Auto-generated method stub
		return 0;
	}

}
