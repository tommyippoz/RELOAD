/**
 * 
 */
package ippoz.multilayer.detector.commons.datafetcher;

import ippoz.multilayer.commons.layers.LayerType;
import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.data.Observation;
import ippoz.multilayer.detector.commons.failure.InjectedElement;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class DataFetcher.
 * The basic fetcher of data.
 *
 * @author Tommy
 */
public abstract class DataFetcher extends Thread {

	/** The fetched experiment data. */
	private ExperimentData expData;
	
	/**
	 * Gets the fetched data.
	 *
	 * @return the fetched data
	 */
	public ExperimentData getFetchedData(){
		return expData;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		expData = new ExperimentData(getID(), getObservations(), getServiceCalls(), getInjections(), getServiceStats(), getPerformanceTimings());
	}

	/**
	 * Gets the runID.
	 *
	 * @return the runID
	 */
	protected abstract String getID();
	
	/**
	 * Gets the experiment observations.
	 *
	 * @return the observations
	 */
	protected abstract LinkedList<Observation> getObservations();

	/**
	 * Gets the experiment service calls.
	 *
	 * @return the service calls
	 */
	protected abstract LinkedList<ServiceCall> getServiceCalls();

	/**
	 * Gets the experiment service stats.
	 *
	 * @return the service stats
	 */
	protected abstract HashMap<String, ServiceStat> getServiceStats();

	/**
	 * Gets the experiment injections.
	 *
	 * @return the injections
	 */
	protected abstract LinkedList<InjectedElement> getInjections();

	/**
	 * Gets the experiment performance timings.
	 *
	 * @return the performance timings
	 */
	protected abstract HashMap<String, HashMap<LayerType, LinkedList<Integer>>> getPerformanceTimings();
	
	/**
	 * Flushes the fetcher.
	 */
	public abstract void flush();

	/**
	 * Opens the connection to the data source.
	 */
	public abstract void openConnection();
}
