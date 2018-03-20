/**
 * 
 */
package ippoz.multilayer.detector.commons.data;

import ippoz.multilayer.detector.commons.failure.InjectedElement;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;
import ippoz.multilayer.detector.commons.service.StatPair;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class Snapshot.
 * Stores data related to a single observation of a target system, enriching it with general information about the system (serviceCalls, injections, serviceStats).
 *
 * @author Tommy
 */
public class Snapshot {
	
	/** The list of services called at that time instant. */
	private List<ServiceCall> sCall;
	
	/** The injection at that time instant. */
	private InjectedElement injEl;
	
	/** The snapshot timestamp. */
	private Date timestamp;
	
	/** The service stat list. */
	private HashMap<String, ServiceStat> ssList;
	
	/**
	 * Instantiates a new snapshot.
	 *
	 * @param currentCalls the current calls
	 * @param injEl the injection
	 */
	public Snapshot(Date timestamp, List<ServiceCall> currentCalls, InjectedElement injEl, HashMap<String, ServiceStat> ssList) {
		this.timestamp = timestamp;
		this.sCall = filterCalls(currentCalls);
		this.injEl = injEl;
		this.ssList = ssList;
	}
	
	private List<ServiceCall> filterCalls(List<ServiceCall> currentCalls) {
		LinkedList<ServiceCall> okCalls = new LinkedList<ServiceCall>();
		if(currentCalls != null){
			for(ServiceCall sCall : currentCalls){
				if(sCall.isAliveAt(timestamp))
					okCalls.add(sCall);
			}
		}
		return okCalls;
	}

	/**
	 * Gets the timestamp of that snapshot.
	 *
	 * @return the timestamp
	 */
	public Date getTimestamp(){
		return timestamp;
	}
	
	/**
	 * Gets the service obs stat.
	 *
	 * @return the service obs stat
	 */
	public StatPair getServiceObsStat(String serviceName){
		return ssList.get(serviceName).getObsStat();
	}
	
	/**
	 * Gets the service timing stat.
	 *
	 * @return the service timing stat
	 */
	public StatPair getServiceTimingStat(String serviceName){
		return ssList.get(serviceName).getTimeStat();
	}
	
	/**
	 * Gets the service calls.
	 *
	 * @return the service calls
	 */
	public List<ServiceCall> getServiceCalls() {
		return sCall;
	}
	
	/**
	 * Gets the injected element.
	 *
	 * @return the injected element
	 */
	public InjectedElement getInjectedElement() {
		return injEl;
	}

	public HashMap<String, ServiceStat> getServiceStats() {
		return ssList;
	}
	
	public List<String> getIndicators(){
		return ssList.get(ssList.keySet().iterator().next()).getIndicators();
	}
	
}
