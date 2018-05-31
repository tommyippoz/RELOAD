/**
 * 
 */
package ippoz.madness.detector.commons.knowledge.data;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.dataseries.MultipleDataSeries;
import ippoz.madness.detector.commons.failure.InjectedElement;
import ippoz.madness.detector.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.service.ServiceCall;
import ippoz.madness.detector.commons.service.ServiceStat;
import ippoz.madness.detector.commons.support.AppLogger;

import java.util.Date;
import java.util.HashMap;
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
	private String expName; 
	
	/** The service call list. */
	private List<ServiceCall> callList;
	
	/** The injections list. */
	private Map<Date, InjectedElement> injMap;
	
	/** The service statistics list. */
	private Map<String, ServiceStat> ssList;
	
	/** The observations list. */
	private List<Observation> obsList;
	
	/**
	 * Instantiates a new experiment data without stats.
	 *
	 * @param expID the experiment id
	 * @param obsList the observation list
	 * @param injList the injections list
	 * @param timings the timings
	 */
	public MonitoredData(String expID, List<Observation> obsList, List<InjectedElement> injList){
		this(expID, obsList, null, injList, null);
	}
	
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
	public MonitoredData(String expID, List<Observation> obsList, List<ServiceCall> callList, List<InjectedElement> injList, Map<String, ServiceStat> ssList){
		this.callList = callList;
		this.ssList = ssList;
		this.obsList = obsList;
		expName = "exp" + expID;
		if(obsList != null && obsList.size() > 0)
			injMap = generateInjMap(injList, obsList.get(0).getTimestamp());
		else AppLogger.logError(getClass(), "NoSuchElementError", "Observation list is empty");
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
	 * Gets the service stats.
	 *
	 * @return the service stats
	 */
	public Map<String, ServiceStat> getServiceStats(){
		return ssList;
	}

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

	public String getDataTag() {
		return expName;
	}

	public Map<String, ServiceStat> getStats() {
		return ssList;
	}
	
	public MultipleSnapshot generateMultipleSnapshot(MultipleDataSeries invDs, int index) {
		return new MultipleSnapshot(obsList.get(index), callList, injMap.get(obsList.get(index).getTimestamp()), invDs.getSeriesList());
	}
	
	public MultipleSnapshot generateMultipleSnapshot(List<DataSeries> dss, int index) {
		return new MultipleSnapshot(obsList.get(index), callList, injMap.get(obsList.get(index).getTimestamp()), dss);
	}

	public DataSeriesSnapshot generateDataSeriesSnapshot(DataSeries dataSeries, int index) {
		return new DataSeriesSnapshot(obsList.get(index), callList, injMap.get(obsList.get(index).getTimestamp()), dataSeries); 
	}

	public Snapshot generateSnapshot(int index) {
		return new Snapshot(obsList.get(index).getTimestamp(), callList, injMap.get(obsList.get(index).getTimestamp()));
	}

	public Observation get(int i) {
		return obsList.get(i);
	}

	public InjectedElement getInjection(int obIndex) {
		if(obIndex >= 0 && obIndex < size() && injMap.containsKey(obsList.get(obIndex).getTimestamp()))
			return injMap.get(obsList.get(obIndex).getTimestamp());
		else return null;
	}
	
}
