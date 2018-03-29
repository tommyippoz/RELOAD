/**
 * 
 */
package ippoz.multilayer.detector.commons.datafetcher;

import ippoz.madness.commons.layers.LayerType;
import ippoz.multilayer.detector.commons.failure.InjectedElement;
import ippoz.multilayer.detector.commons.knowledge.data.MonitoredData;
import ippoz.multilayer.detector.commons.knowledge.data.Observation;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;

import java.util.List;
import java.util.Map;

/**
 * The Class DataFetcher.
 * The basic fetcher of data.
 *
 * @author Tommy
 */
public abstract class DataFetcher extends Thread {

	/** The fetched experiment data. */
	private MonitoredData mData;
	
	/**
	 * Gets the fetched data.
	 *
	 * @return the fetched data
	 */
	public MonitoredData getFetchedData(){
		return mData;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		List<Observation> obList = getObservations();
		if(obList != null && !obList.isEmpty())
			mData = new MonitoredData(getID(), obList, getServiceCalls(), getInjections(), getServiceStats());
		else mData = null;
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
	protected abstract List<Observation> getObservations();

	/**
	 * Gets the experiment service calls.
	 *
	 * @return the service calls
	 */
	protected abstract List<ServiceCall> getServiceCalls();

	/**
	 * Gets the experiment service stats.
	 *
	 * @return the service stats
	 */
	protected abstract Map<String, ServiceStat> getServiceStats();

	/**
	 * Gets the experiment injections.
	 *
	 * @return the injections
	 */
	protected abstract List<InjectedElement> getInjections();

	/**
	 * Gets the experiment performance timings.
	 *
	 * @return the performance timings
	 */
	protected abstract Map<String, Map<LayerType, List<Integer>>> getPerformanceTimings();
	
	/**
	 * Flushes the fetcher.
	 */
	public abstract void flush();

	/**
	 * Opens the connection to the data source.
	 */
	public abstract void openConnection();
}
