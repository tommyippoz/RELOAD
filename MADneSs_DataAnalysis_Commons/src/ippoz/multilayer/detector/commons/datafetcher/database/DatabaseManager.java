/**
 * 
 */
package ippoz.multilayer.detector.commons.datafetcher.database;

import ippoz.multilayer.commons.datacategory.DataCategory;
import ippoz.multilayer.commons.indicator.Indicator;
import ippoz.multilayer.commons.layers.LayerType;
import ippoz.multilayer.detector.commons.data.IndicatorData;
import ippoz.multilayer.detector.commons.data.Observation;
import ippoz.multilayer.detector.commons.failure.InjectedElement;
import ippoz.multilayer.detector.commons.service.IndicatorStat;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;
import ippoz.multilayer.detector.commons.service.StatPair;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.AppUtility;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class DatabaseManager.
 * Instantiates a MYSQL Database manager
 *
 * @author Tommy
 */
public class DatabaseManager {
	
	/** The database connector. */
	private DatabaseConnector connector;
	
	/** The runID. */
	private String runId;
	
	/** The map of the layers. */
	private HashMap<String, LayerType> layers;
	
	/**
	 * Instantiates a new database manager.
	 *
	 * @param dbName the database name
	 * @param username the database username
	 * @param password the database password
	 * @param runId the runID
	 */
	public DatabaseManager(String dbName, String username, String password, String runId){
		try {
			this.runId = runId;
			connector = new DatabaseConnector(dbName, username, password, false);
			loadSystemLayers();
		} catch(Exception ex){
			AppLogger.logInfo(getClass(), "Need to start MySQL Server...");
		}
	}

	/**
	 * Load system layers.
	 */
	private void loadSystemLayers() {
		layers = new HashMap<String, LayerType>();
		for(HashMap<String, String> ptMap : connector.executeCustomQuery(null, "select * from probe_type")){
			layers.put(ptMap.get("probe_type_id"), LayerType.valueOf(ptMap.get("pt_description")));
		}
	}

	/**
	 * Flushes database manager.
	 *
	 * @throws SQLException the SQL exception
	 */
	public void flush() throws SQLException {
		connector.closeConnection();
		connector = null;
	}
	
	/**
	 * Gets the observations for the specific runID.
	 *
	 * @return the run observations
	 */
	public LinkedList<Observation> getRunObservations() {
		Observation obs;
		LinkedList<Observation> obsList = new LinkedList<Observation>();
		HashMap<DataCategory, String> indData;
		for(HashMap<String, String> obsMap : connector.executeCustomQuery(null, "select observation_id, ob_time from observation where run_id = " + runId)){
			obs = new Observation(obsMap.get("ob_time"));
			for(HashMap<String, String> indObs : connector.executeCustomQuery(null, "select indicator_observation_id, probe_type_id, in_tag from indicator natural join indicator_observation where observation_id = " + obsMap.get("observation_id"))) {
				indData = new HashMap<DataCategory, String>();
				for(HashMap<String, String> indValues : connector.executeCustomQuery(null, "select vc_description, ioc_value from indicator_observation_category natural join value_category where indicator_observation_id = " + indObs.get("indicator_observation_id"))) {
					indData.put(DataCategory.valueOf(indValues.get("vc_description").toUpperCase()), indValues.get("ioc_value"));
				}
				obs.addIndicator(new Indicator(indObs.get("in_tag"), layers.get(indObs.get("probe_type_id")), String.class), new IndicatorData(indData));
			}
			obsList.add(obs);
		}
		return obsList;
	}

	/**
	 * Gets the service calls for the specific runID.
	 *
	 * @return the service calls
	 */
	public LinkedList<ServiceCall> getServiceCalls() {
		LinkedList<ServiceCall> callList = new LinkedList<ServiceCall>();
		for(HashMap<String, String> callMap : connector.executeCustomQuery(null, "select se_name, min(start_time) as st_time, max(end_time) as en_time, response from service_method_invocation natural join service_method natural join service where run_id = " + runId + " group by se_name order by st_time")){
			callList.add(new ServiceCall(callMap.get("se_name"), callMap.get("st_time"), callMap.get("en_time"), callMap.get("response")));
		}
		return callList;
	}
	
	/**
	 * Gets the service stats for the specific runID.
	 *
	 * @return the service stats
	 */
	public HashMap<String, ServiceStat> getServiceStats() {
		ServiceStat current;
		HashMap<String, ServiceStat> ssList = new HashMap<String, ServiceStat>();
		for(HashMap<String, String> ssInfo : connector.executeCustomQuery(null, "select * from service_stat natural join service")){
			current = new ServiceStat(ssInfo.get("se_name"), new StatPair(ssInfo.get("serv_dur_avg"), ssInfo.get("serv_dur_std")), new StatPair(ssInfo.get("serv_obs_avg"), ssInfo.get("serv_obs_std")));
			for(HashMap<String, String> isInfo : connector.executeCustomQuery(null, "select * from indicator natural join service_indicator_stat natural join service_stat natural join service where se_name = '" + ssInfo.get("se_name") + "'")){
				current.addIndicatorStat(new IndicatorStat(isInfo.get("in_tag"), new StatPair(isInfo.get("si_avg_first"), isInfo.get("si_std_first")), new StatPair(isInfo.get("si_avg_last"), isInfo.get("si_std_last")), new StatPair(isInfo.get("si_all_avg"), isInfo.get("si_all_std"))));
			}
			ssList.put(ssInfo.get("se_name"), current);
		}
		return ssList;
	}

	/**
	 * Gets the injections for the specific runID.
	 *
	 * @return the injections
	 */
	public LinkedList<InjectedElement> getInjections() {
		LinkedList<InjectedElement> injList = new LinkedList<InjectedElement>();
		for(HashMap<String, String> injInfo : connector.executeCustomQuery(null, "select * from failure natural join failure_type where run_id = " + runId + " order by fa_time")){
			injList.add(new InjectedElement(AppUtility.convertStringToDate(injInfo.get("fa_time")), injInfo.get("fa_description"), Integer.parseInt(injInfo.get("fa_duration"))));
		}
		return injList;
	}

	/**
	 * Gets the runID.
	 *
	 * @return the runID
	 */
	public String getRunID() {
		return runId;
	}

	/**
	 * Gets the performance timings for the specific runID.
	 *
	 * @return the performance timings
	 */
	public HashMap<String, HashMap<LayerType, LinkedList<Integer>>> getPerformanceTimings() {
		String perfType;
		HashMap<String, HashMap<LayerType, LinkedList<Integer>>> timings = new HashMap<String, HashMap<LayerType, LinkedList<Integer>>>();
		for(HashMap<String, String> perfIndexes : connector.executeCustomQuery(null, "select * from performance_type")){
			perfType = perfIndexes.get("pet_description");
			timings.put(perfType, new HashMap<LayerType, LinkedList<Integer>>());
			for(HashMap<String, String> timing : connector.executeCustomQuery(null, "select * from performance where run_id = " + runId + " and performance_type_id = " + perfIndexes.get("performance_type_id"))){
				if(timings.get(perfType).get(layers.get(timing.get("probe_type_id"))) == null){
					timings.get(perfType).put(layers.get(timing.get("probe_type_id")), new LinkedList<Integer>());
				}
				timings.get(perfType).get(layers.get(timing.get("probe_type_id"))).add(Integer.parseInt(timing.get("perf_time")));
			}
		}		
		return timings;
	}

}
