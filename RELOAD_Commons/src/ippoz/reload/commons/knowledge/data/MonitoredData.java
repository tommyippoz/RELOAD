/**
 * 
 */
package ippoz.reload.commons.knowledge.data;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.loader.DatasetIndex;
import ippoz.reload.commons.loader.LoaderBatch;
import ippoz.reload.commons.support.AppLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
	
	private List<Indicator> indicatorList;
	
	/** The injections list. */
	private Map<DatasetIndex, InjectedElement> injMap;
	
	/** The observations list. */
	private List<Observation> obsList;
	
	/**
	 * Instantiates a new experiment data.
	 * @param list 
	 *
	 * @param expID the experiment id
	 * @param snapList the snapshot list
	 * @param callList the service call list
	 * @param injList the injections list
	 * @param ssList the service stats list
	 * @param timings the timings
	 */
	public MonitoredData(LoaderBatch expBatch, List<Observation> obsList, Map<DatasetIndex, InjectedElement> injMap, Map<String, Boolean> map){
		this(expBatch, obsList, injMap, sanitizeStrIndicators(map));
	}
	
	public MonitoredData(LoaderBatch expBatch, List<Observation> obsList, Map<DatasetIndex, InjectedElement> injMap, List<Indicator> indList){
		this.obsList = obsList;
		this.expBatch = expBatch;
		this.injMap = injMap;
		this.indicatorList = indList;
	}
	
	public MonitoredData(LoaderBatch expBatch, Indicator[] indicatorList){
		this.expBatch = expBatch;
		obsList = new ArrayList<>(expBatch.getDataPoints());
		this.indicatorList = sanitizeIndicators(Arrays.asList(indicatorList));
		injMap = new HashMap<>();
	}

	private List<Indicator> sanitizeIndicators(Collection<Indicator> initialList) {
		List<Indicator> finalList = new LinkedList<>();
		if(initialList != null){
			for(Indicator ind : initialList){
				if(ind != null)
					finalList.add(ind);
			}
		}
		return finalList;
	}
	
	private static List<Indicator> sanitizeStrIndicators(Map<String, Boolean> map) {
		List<Indicator> finalList = new LinkedList<>();
		if(map != null){
			for(String ind : map.keySet()){
				if(ind != null && map.get(ind))
					finalList.add(new Indicator(ind.trim(), String.class));
			}
		}
		return finalList; 
	}

	public int size(){
		return obsList.size();
	}
	
	public List<Observation> getObservationList(){
		return obsList;
	}

	/**
	 * Gets the injections for this experiment.
	 *
	 * @return the injections
	 */
	public Map<DatasetIndex, InjectedElement> getInjections() {
		return injMap;
	}
	
	/**
	 * Gets the indicators.
	 *
	 * @return the indicators
	 */
	public Indicator[] getIndicators() {
		if(indicatorList != null)
			return indicatorList.toArray(new Indicator[indicatorList.size()]);
		else return new Indicator[]{};
	}

	public LoaderBatch getDataID() {
		return expBatch;
	}
	
	public boolean hasIndicator(String indicatorName) {
		for(Indicator ind : getIndicators()){
			if(ind.getName().compareTo(indicatorName.trim()) == 0)
				return true;
		}
		return false;
	}

	public Snapshot generateSnapshot(DataSeries dataSeries, int index) {
		Map<Indicator, Object> sv = new HashMap<>();
		if(dataSeries != null && index < obsList.size() && index >= 0){
			Observation obs = obsList.get(index);
			for(Indicator ind : dataSeries.getIndicators()){
				int indIndex = indexOf(ind);
				sv.put(ind, obs.getValue(indIndex));
			}
		}
		return new Snapshot(sv, injMap.get(obsList.get(index).getIndex()), dataSeries); 
	}

	private int indexOf(Indicator ind) {
		if(ind != null && indicatorList != null){
			for(int i=0;i<indicatorList.size();i++){
				if(ind.getName().compareTo(indicatorList.get(i).getName()) == 0)
					return i;
			}
		}
		return -1;
	}

	public Observation get(int i) {
		return obsList.get(i);
	}

	public InjectedElement getInjectionAt(DatasetIndex index) {
		if(injMap != null)
			return injMap.get(index);
		else return null;
	}
	
	public InjectedElement getInjectionAt(int i) {
		return getInjectionAt(obsList.get(i).getIndex());
	}

	public void addObservation(Observation obs, InjectedElement injection) {
		obsList.add(obs);
		if(injection != null)
			injMap.put(obs.getIndex(), injection);
	}
	
	public void addIndicatorData(int obId, String indName, Object indData){
		if(obId >= 0 && obId < obsList.size()){
			if(indName != null && hasIndicator(indName)){
				obsList.get(obId).addIndicator(indData);
			} else 
				AppLogger.logError(getClass(), "ObservationUpdateError", "Unable to find indicator '" + indName + "'");
		} else AppLogger.logError(getClass(), "ObservationUpdateError", "Unable to find observation '" + obId + "'");
	}

	public void addIndicator(Indicator indicator) {
		if(indicatorList != null && indicator != null && !hasIndicator(indicator.getName()))
			indicatorList.add(indicator);
	}

	public MonitoredData subData(String tag, int min, int max) {
		Map<DatasetIndex, InjectedElement> redMap = new HashMap<>();
		if(min < obsList.size()){
			if(max >= obsList.size())
				max = obsList.size()-1;
			DatasetIndex minIndex = obsList.get(min).getIndex();
			DatasetIndex maxIndex = obsList.get(max).getIndex();
			for(DatasetIndex di : injMap.keySet()){
				if(di.compareTo(minIndex) >= 0 && di.compareTo(maxIndex) < 0)
					redMap.put(di, injMap.get(di));
			}
			return new MonitoredData(new LoaderBatch(expBatch.getTag() + tag, expBatch.getFrom() + min, expBatch.getFrom() + max), obsList.subList(min, max), redMap, indicatorList);
		} else return null;
	}
	
}
