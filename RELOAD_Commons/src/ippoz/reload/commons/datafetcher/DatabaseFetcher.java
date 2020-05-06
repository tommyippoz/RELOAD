/**
 * 
 */
package ippoz.reload.commons.datafetcher;

import ippoz.reload.commons.datafetcher.database.DatabaseManager;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.loader.LoaderBatch;
import ippoz.reload.commons.service.ServiceStat;
import ippoz.reload.commons.support.AppLogger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class DatabaseFetcher.
 * Concrete fetcher that uses a MYSQL database manager.
 *
 * @author Tommy
 */
public class DatabaseFetcher extends DataFetcher {
	
	/** The database manager. */
	private DatabaseManager dbManager;
	
	private LoaderBatch runId;
	
	private String dbName;
	
	private String username;
	
	private String password;
	
	private List<LayerType> selectedLayers;
	
	/**
	 * Instantiates a new database fetcher.
	 *
	 * @param runId the runID
	 * @param username the database username
	 * @param password the database password
	 * @param dbPassword 
	 */
	public DatabaseFetcher(LoaderBatch runId, String dbName, String username, String password, List<LayerType> selectedLayers){
		this.runId = runId;
		this.dbName = dbName;
		this.username = username;
		this.password = password;
		this.selectedLayers = selectedLayers;
	}
	
	public void openConnection() {
		dbManager = new DatabaseManager(dbName, username, password, runId, selectedLayers);	
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.datafetcher.DataFetcher#getObservations()
	 */
	@Override
	protected List<Observation> getObservations() {
		return dbManager.getRunObservations();
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.datafetcher.DataFetcher#getServiceStats()
	 */
	@Override
	protected Map<String, ServiceStat> getServiceStats() {
		return dbManager.getServiceStats();
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.datafetcher.DataFetcher#getInjections()
	 */
	@Override
	protected List<InjectedElement> getInjections() {
		return dbManager.getInjections();
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.datafetcher.DataFetcher#flush()
	 */
	@Override
	public void flush() {
		try {
			dbManager.flush();
			dbManager = null;
		} catch (SQLException ex) {
			AppLogger.logException(getClass(), ex, "Unable to close SQL Connection");
		}
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.datafetcher.DataFetcher#getID()
	 */
	@Override
	protected LoaderBatch getBatch() {
		return dbManager.getRunID();
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.datafetcher.DataFetcher#getPerformanceTimings()
	 */
	@Override
	protected Map<String, Map<LayerType, List<Integer>>> getPerformanceTimings() {
		return dbManager.getPerformanceTimings();
	}

}
