/**
 * 
 */
package ippoz.reload.commons.knowledge.data;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.loader.LoaderBatch;
import ippoz.reload.commons.support.AppLogger;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Class ExperimentData.
 * Stores information about the data of each experiment (service calls, snapshots, timings, injections, stats)
 *
 * @author Tommy
 */
public class MonitoredData {
	
	/** The experiment name. */
	private LoaderBatch expBatch; 
	
	/** The injections list. */
	private Map<Date, InjectedElement> injMap;
	
	/** The observations list. */
	private List<Observation> obsList;
	
	/**
	 * Instantiates a new experiment data.
	 *
	 * @param expID the experiment id
	 * @param snapList the snapshot list
	 * @param callList the service call list
	 * @param injList the injections list
	 * @param ssList the service stats list
	 * @param timings the timings
	 */
	public MonitoredData(LoaderBatch expBatch, List<Observation> obsList, List<InjectedElement> injList){
		this.obsList = obsList;
		this.expBatch = expBatch;
		if(obsList != null && obsList.size() > 0)
			injMap = generateInjMap(injList, obsList.get(0).getTimestamp());
		else AppLogger.logError(getClass(), "NoSuchElementError", "Observation list is empty");
	}
	
	public MonitoredData(LoaderBatch expBatch){
		obsList = new LinkedList<>();
		injMap = new HashMap<>();
		this.expBatch = expBatch;
	}

	private Map<Date, InjectedElement> generateInjMap(List<InjectedElement> injList, Date refTime) {
		Map<Date, InjectedElement> iMap = new HashMap<Date, InjectedElement>();
		for(InjectedElement iEl : injList){
			iMap.put(iEl.getTimestamp(), iEl);
		}
		return iMap;
	}
	
	public int size(){
		return obsList.size();
	}
	
	public List<Observation> getObservationList(){
		return obsList;
	}

	/**
	 * Builds the snapshots of the experiment depending on the observations.
	 */
	/*private List<Snapshot> buildSnapshots(List<Observation> obsList) {
		int injIndex = 0;
		List<ServiceCall> currentCalls;
		InjectedElement currentInj;
		List<Snapshot> builtSnap = new ArrayList<Snapshot>(obsList.size());
		for(Observation obs : obsList){
			currentCalls = new LinkedList<ServiceCall>();
			if(callList != null) {
				for(ServiceCall call : callList){
					if(call.isAliveAt(obs.getTimestamp()))
						currentCalls.add(call);
				}
			}
			while(injList.size() > injIndex && injList.get(injIndex).getTimestamp().before(obs.getTimestamp())){
				injIndex++;
			}
			if(injList.size() > injIndex && injList.get(injIndex).getTimestamp().compareTo(obs.getTimestamp()) == 0)
				currentInj = injList.get(injIndex);
			else currentInj = null;		
			builtSnap.add(new Snapshot(obs.getTimestamp(), currentCalls, currentInj));
		}
		return builtSnap;
	}*/

	/**
	 * Gets the injections for this experiment.
	 *
	 * @return the injections
	 */
	public Map<Date, InjectedElement> getInjections() {
		return injMap;
	}

	public Map<LayerType, Integer> getLayerIndicators(){
		HashMap<LayerType, Integer> layerInd = new HashMap<LayerType, Integer>();
		if(obsList.size() > 0){
			for(Indicator ind : obsList.get(0).getIndicators()){
				if(layerInd.get(ind.getLayer()) == null)
					layerInd.put(ind.getLayer(), 0);
				layerInd.replace(ind.getLayer(), layerInd.get(ind.getLayer())+1);
			}
		}
		return layerInd;
	}
	
	/**
	 * Gets the indicators.
	 *
	 * @return the indicators
	 */
	public Indicator[] getIndicators() {
		return obsList.get(0).getIndicators();
	}

	public LoaderBatch getDataID() {
		return expBatch;
	}
	
	public MultipleSnapshot generateMultipleSnapshot(MultipleDataSeries invDs, int index) {
		return new MultipleSnapshot(obsList.get(index), injMap.get(obsList.get(index).getTimestamp()), invDs.getSeriesList());
	}
	
	public MultipleSnapshot generateMultipleSnapshot(List<DataSeries> dss, int index) {
		return new MultipleSnapshot(obsList.get(index), injMap.get(obsList.get(index).getTimestamp()), dss);
	}

	public DataSeriesSnapshot generateDataSeriesSnapshot(DataSeries dataSeries, int index) {
		return new DataSeriesSnapshot(obsList.get(index), injMap.get(obsList.get(index).getTimestamp()), dataSeries); 
	}

	public Observation get(int i) {
		return obsList.get(i);
	}

	public InjectedElement getInjection(int obIndex) {
		if(obIndex >= 0 && obIndex < size() && injMap.containsKey(obsList.get(obIndex).getTimestamp()))
			return injMap.get(obsList.get(obIndex).getTimestamp());
		else return null;
	}

	public void addItem(Observation obs, InjectedElement injection) {
		obsList.add(obs);
		if(injection != null)
			injMap.put(obs.getTimestamp(), injection);
		
	}
	
}
