/**
 * 
 */
package ippoz.multilayer.detector.commons.service;

import ippoz.multilayer.detector.commons.support.AppUtility;

import java.util.Date;

/**
 * The Class ServiceCall.
 * Represents the data of a generic service call (serviceName, start, end, responseCode)
 *
 * @author Tommy
 */
public class ServiceCall implements Comparable<ServiceCall> {
	
	/** The service name. */
	private String serviceName;
	
	/** The start time. */
	private Date startTime;
	
	/** The end time. */
	private Date endTime;
	
	/** The HTTP response code. */
	private String responseCode;
	
	/**
	 * Instantiates a new service call.
	 *
	 * @param serviceName the service name
	 * @param startTime the start time
	 * @param endTime the end time
	 * @param responseCode the HTTP response code
	 */
	public ServiceCall(String serviceName, Date startTime, Date endTime, String responseCode) {
		this.serviceName = serviceName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.responseCode = responseCode;
	}

	/**
	 * Instantiates a new service call.
	 *
	 * @param serviceName the service name
	 * @param startTime the start time
	 * @param endTime the end time
	 * @param responseCode the HTTP response code
	 */
	public ServiceCall(String serviceName, String startTime, String endTime, String responseCode) {
		this(serviceName, AppUtility.convertStringToDate(startTime), AppUtility.convertStringToDate(endTime), responseCode);
	}
	
	/**
	 * Gets the service name.
	 *
	 * @return the service name
	 */
	public String getServiceName(){
		return serviceName;
	}

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Gets the end time.
	 *
	 * @return the end time
	 */
	public Date getEndTime() {
		return endTime;
	}
	
	/**
	 * Gets the HTTP response code.
	 *
	 * @return the HTTP response code
	 */
	public String getResponseCode(){
		return responseCode;
	}
	
	/**
	 * Checks if the service is alive at a given time.
	 *
	 * @param timestamp the given timestamp
	 * @return true, if the service is alive at time 'timestamp'
	 */
	public boolean isAliveAt(Date timestamp){
		return timestamp.getTime() >= startTime.getTime() && timestamp.getTime() <= endTime.getTime();
	}

	@Override
	public int compareTo(ServiceCall other) {
		if(other.getServiceName().equals(serviceName) && other.getResponseCode().equals(responseCode)){
			if(other.getStartTime().equals(startTime) && other.getEndTime().equals(endTime))
				return 0;
			else return 1;
		} else return -1;
	}
	
}
