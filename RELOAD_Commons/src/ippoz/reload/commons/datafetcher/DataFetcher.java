/**
 * 
 */
package ippoz.reload.commons.datafetcher;

import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.loader.LoaderBatch;
import ippoz.reload.commons.service.ServiceStat;

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
			mData = new MonitoredData(getBatch(), obList, getInjections());
		else mData = null;
	}

	/**
	 * Gets the runID.
	 *
	 * @return the runID
	 */
	protected abstract LoaderBatch getBatch();
	
	/**
	 * Gets the experiment observations.
	 *
	 * @return the observations
	 */
	protected abstract List<Observation> getObservations();

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
