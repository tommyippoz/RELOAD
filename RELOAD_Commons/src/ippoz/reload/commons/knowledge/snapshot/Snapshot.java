/**
 * 
 */
package ippoz.reload.commons.knowledge.snapshot;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.service.ServiceCall;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class Snapshot.
 * Stores data related to a single observation of a target system, enriching it with general information about the system (serviceCalls, injections, serviceStats).
 *
 * @author Tommy
 */
public abstract class Snapshot {
	
	/** The list of services called at that time instant. */
	private List<ServiceCall> sCall;
	
	/** The injection at that time instant. */
	private InjectedElement injEl;
	
	/** The snapshot timestamp. */
	private Date timestamp;
	
	/**
	 * Instantiates a new snapshot.
	 *
	 * @param currentCalls the current calls
	 * @param injEl the injection
	 */
	public Snapshot(Date timestamp, List<ServiceCall> currentCalls, InjectedElement injEl) {
		this.timestamp = timestamp;
		this.sCall = filterCalls(currentCalls);
		this.injEl = injEl;
	}
	
	private List<ServiceCall> filterCalls(List<ServiceCall> currentCalls) {
		List<ServiceCall> okCalls = new LinkedList<ServiceCall>();
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
	
	public List<Double> listValues(boolean first){
		List<Double> list = new LinkedList<Double>();
		for(SnapshotValue sv : listValues()){
			if(first)
				list.add(sv.getFirst());
			else list.add(sv.getLast());
		}
		return list;
	}
	
	public abstract List<SnapshotValue> listValues();
	
	/**
	 * Converts a snapshot to string.
	 *
	 * @param snap the snapshot
	 * @return the string
	 */
	public static String snapToString(Snapshot snap, DataSeries ds){
		String snapValue = "{";
		if(ds.size() == 1){
			snapValue = snapValue + ((DataSeriesSnapshot)snap).getSnapValue().getFirst();
		} else if(ds.size() > 1){
			for(int j=0;j<ds.size();j++){
				snapValue = snapValue + ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)ds).getSeries(j)).getSnapValue().getFirst() + ", ";
			}
			snapValue = snapValue.substring(0,  snapValue.length()-2);
		}
		return snapValue + "}";
	}
	
	public boolean isAnomalous(){
		return injEl != null;
	}
	
}
