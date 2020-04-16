/**
 * 
 */
package ippoz.reload.commons.datafetcher.database;

import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.data.IndicatorData;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.service.IndicatorStat;
import ippoz.reload.commons.service.ServiceCall;
import ippoz.reload.commons.service.ServiceStat;
import ippoz.reload.commons.service.StatPair;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	private Integer runId;
	
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
	public DatabaseManager(String dbName, String username, String password, Integer runId, List<LayerType> selectedLayers){
		try {
			this.runId = runId;
			connector = new DatabaseConnector(dbName, username, password, false);
			loadSystemLayers(selectedLayers);
		} catch(Exception ex){
			AppLogger.logInfo(getClass(), "Need to start MySQL Server...");
		}
	}

	/**
	 * Load system layers.
	 * @param selectedLayers 
	 */
	private void loadSystemLayers(List<LayerType> selectedLayers) {
		layers = new HashMap<String, LayerType>();
		for(Map<String, String> ptMap : connector.executeCustomQuery(null, "select * from probe_type")){
			if(selectedLayers.contains(LayerType.valueOf(ptMap.get("pt_description"))))
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
	/*public List<Observation> getRunObservations() {
		Observation obs;
		Map<DataCategory, String> indData;
		List<Map<String, String>> queryMap = connector.executeCustomQuery(null, "select observation_id, ob_time from observation where run_id = " + runId);
		List<Observation> obsList = new ArrayList<Observation>(queryMap.size());
		for(Map<String, String> obsMap : queryMap){
			obs = new Observation(obsMap.get("ob_time"));
			for(Map<String, String> indObs : connector.executeCustomQuery(null, "select indicator_observation_id, probe_type_id, in_tag from indicator natural join indicator_observation where observation_id = " + obsMap.get("observation_id"))) {
				if(layers.get(indObs.get("probe_type_id")) != null){
					indData = new HashMap<DataCategory, String>();
					for(Map<String, String> indValues : connector.executeCustomQuery(null, "select vc_description, ioc_value from indicator_observation_category natural join value_category where indicator_observation_id = " + indObs.get("indicator_observation_id"))) {
						indData.put(DataCategory.valueOf(indValues.get("vc_description").toUpperCase()), indValues.get("ioc_value"));
					}
					obs.addIndicator(new Indicator(indObs.get("in_tag"), layers.get(indObs.get("probe_type_id")), String.class), new IndicatorData(indData));
				}
			}
			obsList.add(obs);
		}
		return obsList;
	}*/
	
	/**
	 * Gets the observations for the specific runID.
	 *
	 * @return the run observations
	 */
	public List<Observation> getRunObservations() {
		Observation obs = null;
		Map<String, String> lastObsMap = null;
		Map<DataCategory, String> indData = null;
		List<Observation> obsList = new ArrayList<Observation>(Integer.parseInt(DatabaseConnector.getFirstValueByTag(connector.executeCustomQuery(null, "select count(*) as obNumber from observation where run_id = " + runId), "obNumber")));
		List<Map<String, String>> queryMap = connector.executeCustomQuery(null, "select observation_id, ob_time, indicator_observation_id, probe_type_id, in_tag, vc_description, ioc_value from observation natural join indicator_observation natural join indicator natural join indicator_observation_category natural join value_category where run_id = " + runId + " order by observation_id asc, indicator_observation_id asc, vc_description desc"); 
		for(Map<String, String> obsMap : queryMap){
			if(layers.get(obsMap.get("probe_type_id")) != null){
				// First Iteration
				if(lastObsMap == null){
					obs = new Observation(obsMap.get("ob_time"));
					indData = new HashMap<DataCategory, String>();
				// New Observation
				} else if(!obsMap.get("ob_time").equals(lastObsMap.get("ob_time"))){
					obs.addIndicator(new Indicator(lastObsMap.get("in_tag"), layers.get(lastObsMap.get("probe_type_id")), String.class), new IndicatorData(indData));
					obsList.add(obs);
					obs = new Observation(obsMap.get("ob_time"));
					indData = new HashMap<DataCategory, String>();
				// New Indicator
				} else if(!lastObsMap.get("indicator_observation_id").equals(obsMap.get("indicator_observation_id"))){
					obs.addIndicator(new Indicator(lastObsMap.get("in_tag"), layers.get(lastObsMap.get("probe_type_id")), String.class), new IndicatorData(indData));
					indData = new HashMap<DataCategory, String>();
				} 
				indData.put(DataCategory.valueOf(obsMap.get("vc_description").toUpperCase()), obsMap.get("ioc_value"));
				lastObsMap = obsMap;
			}
		}
		if(lastObsMap != null) {
			obs.addIndicator(new Indicator(lastObsMap.get("in_tag"), layers.get(lastObsMap.get("probe_type_id")), String.class), new IndicatorData(indData));
			obsList.add(obs);
		}
		return obsList;
	}

	/**
	 * Gets the service calls for the specific runID.
	 *
	 * @return the service calls
	 */
	public List<ServiceCall> getServiceCalls() {
		LinkedList<ServiceCall> callList = new LinkedList<ServiceCall>();
		for(Map<String, String> callMap : connector.executeCustomQuery(null, "select se_name, min(start_time) as st_time, max(end_time) as en_time, response from service_method_invocation natural join service_method natural join service where run_id = " + runId + " group by se_name order by st_time")){
			callList.add(new ServiceCall(callMap.get("se_name"), callMap.get("st_time"), callMap.get("en_time"), callMap.get("response")));
		}
		return callList;
	}
	
	/**
	 * Gets the service stats for the specific runID.
	 *
	 * @return the service stats
	 */
	public Map<String, ServiceStat> getServiceStats() {
		ServiceStat current;
		Map<String, ServiceStat> ssList = new HashMap<String, ServiceStat>();
		for(Map<String, String> ssInfo : connector.executeCustomQuery(null, "select * from service_stat natural join service")){
			current = new ServiceStat(ssInfo.get("se_name"), new StatPair(ssInfo.get("serv_dur_avg"), ssInfo.get("serv_dur_std")), new StatPair(ssInfo.get("serv_obs_avg"), ssInfo.get("serv_obs_std")));
			for(Map<String, String> isInfo : connector.executeCustomQuery(null, "select * from indicator natural join service_indicator_stat natural join service_stat natural join service where se_name = '" + ssInfo.get("se_name") + "'")){
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
	public List<InjectedElement> getInjections() {
		List<Map<String, String>> queryMap = connector.executeCustomQuery(null, "select * from failure natural join failure_type where run_id = " + runId + " order by fa_time");
		List<InjectedElement> injList = new ArrayList<InjectedElement>(queryMap.size());
		for(Map<String, String> injInfo : queryMap){
			injList.add(new InjectedElement(AppUtility.convertStringToDate(injInfo.get("fa_time")), injInfo.get("fa_description"), Integer.parseInt(injInfo.get("fa_duration"))));
		}
		return injList;
	}

	/**
	 * Gets the runID.
	 *
	 * @return the runID
	 */
	public Integer getRunID() {
		return runId;
	}

	/**
	 * Gets the performance timings for the specific runID.
	 *
	 * @return the performance timings
	 */
	public Map<String, Map<LayerType, List<Integer>>> getPerformanceTimings() {
		String perfType;
		Map<String, Map<LayerType, List<Integer>>> timings = new HashMap<String, Map<LayerType, List<Integer>>>();
		for(Map<String, String> perfIndexes : connector.executeCustomQuery(null, "select * from performance_type")){
			perfType = perfIndexes.get("pet_description");
			timings.put(perfType, new HashMap<LayerType, List<Integer>>());
			for(Map<String, String> timing : connector.executeCustomQuery(null, "select * from performance where run_id = " + runId + " and performance_type_id = " + perfIndexes.get("performance_type_id"))){
				if(timings.get(perfType).get(layers.get(timing.get("probe_type_id"))) == null){
					timings.get(perfType).put(layers.get(timing.get("probe_type_id")), new LinkedList<Integer>());
				}
				timings.get(perfType).get(layers.get(timing.get("probe_type_id"))).add(Integer.parseInt(timing.get("perf_time")));
			}
		}		
		return timings;
	}

}
