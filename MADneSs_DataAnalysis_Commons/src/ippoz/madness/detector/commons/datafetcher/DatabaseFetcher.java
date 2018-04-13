/**
 * 
 */
package ippoz.madness.detector.commons.datafetcher;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.detector.commons.datafetcher.database.DatabaseManager;
import ippoz.madness.detector.commons.failure.InjectedElement;
import ippoz.madness.detector.commons.knowledge.data.Observation;
import ippoz.madness.detector.commons.service.ServiceCall;
import ippoz.madness.detector.commons.service.ServiceStat;
import ippoz.madness.detector.commons.support.AppLogger;

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
	
	private String runId;
	
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
	public DatabaseFetcher(String runId, String dbName, String username, String password, List<LayerType> selectedLayers){
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
	 * @see ippoz.multilayer.detector.datafetcher.DataFetcher#getServiceCalls()
	 */
	@Override
	protected List<ServiceCall> getServiceCalls() {
		return dbManager.getServiceCalls();
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
	protected String getID() {
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