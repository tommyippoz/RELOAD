/**
 * 
 */
package ippoz.multilayer.detector.commons.service;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class ServiceStat.
 *
 * @author Tommy
 */
public class ServiceStat {
	
	/** The service name. */
	private String serviceName;
	
	/** The observation statistic. */
	private StatPair obsStat;
	
	/** The time statistic. */
	private StatPair timeStat;
	
	/** The map of indicators' stats. */
	private HashMap<String, IndicatorStat> indStat;
	
	/**
	 * Instantiates a new service statistic.
	 *
	 * @param serviceName the service name
	 * @param obsStat the observation stat
	 * @param timeStat the time stat
	 */
	public ServiceStat(String serviceName, StatPair obsStat, StatPair timeStat) {
		this.serviceName = serviceName;
		this.obsStat = obsStat;
		this.timeStat = timeStat;
		indStat = new HashMap<String, IndicatorStat>();
	}
	
	/**
	 * Adds a new indicator stat.
	 *
	 * @param newStat the new indicator stat
	 */
	public void addIndicatorStat(IndicatorStat newStat){
		indStat.put(newStat.getName(), newStat);
	}

	/**
	 * Gets the service name.
	 *
	 * @return the service name
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Gets the observation stat.
	 *
	 * @return the observation stat
	 */
	public StatPair getObsStat() {
		return obsStat;
	}

	/**
	 * Gets the time stat.
	 *
	 * @return the time stat
	 */
	public StatPair getTimeStat() {
		return timeStat;
	}

	/**
	 * Gets the indicator stat by name.
	 *
	 * @param indName the indicator name
	 * @return the indicator stat
	 */
	public IndicatorStat getIndStat(String indName) {
		return indStat.get(indName);
	}

	public LinkedList<String> getIndicators() {
		LinkedList<String> indList = new LinkedList<String>();
		for(IndicatorStat is : indStat.values()){
			indList.add(is.getName());
		}
		return indList;
		
	}	

}
