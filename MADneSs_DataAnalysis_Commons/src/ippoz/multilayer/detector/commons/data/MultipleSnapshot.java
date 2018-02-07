/**
 * 
 */
package ippoz.multilayer.detector.commons.data;

import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.failure.InjectedElement;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class MultipleSnapshot extends Snapshot {

	private HashMap<DataSeries, DataSeriesSnapshot> dsMap;
	
	public MultipleSnapshot(Observation obs, LinkedList<ServiceCall> currentCalls, InjectedElement injEl, HashMap<String, ServiceStat> ssList, DataSeries[] seriesList) {
		super(obs.getTimestamp(), currentCalls, injEl, ssList);
		dsMap = generateMultipleSnapshots(obs, seriesList);
	}

	private HashMap<DataSeries, DataSeriesSnapshot> generateMultipleSnapshots(Observation obs, DataSeries[] seriesList) {
		HashMap<DataSeries, DataSeriesSnapshot> outMap = new HashMap<DataSeries, DataSeriesSnapshot>();
		for(DataSeries ds : seriesList){
			outMap.put(ds, new DataSeriesSnapshot(obs, getServiceCalls(), getInjectedElement(), getServiceStats(), ds));
		}
		return outMap;
	}
	
	public DataSeriesSnapshot getSnapshot(DataSeries dataSeries){
		DataSeriesSnapshot out = dsMap.get(dataSeries);
		if(out == null){
			for(DataSeries ds : dsMap.keySet()){
				if(ds.toString().equals(dataSeries.toString()))
					return dsMap.get(ds);
			}
		} else return out;
		return null;
	}
	
	public SnapshotValue getFirstSeriesValue(){
		return getSeriesValue(0);
	}
	
	public SnapshotValue getLastSeriesValue(){
		return getSeriesValue(dsMap.size()-1);
	}
	
	private SnapshotValue getSeriesValue(int index){
		Iterator<DataSeries> iterator = dsMap.keySet().iterator();
		if(index < dsMap.size()){
			while(index > 0){
				index--;
				iterator.next();
			}
			return dsMap.get(iterator.next()).getSnapValue();
		} else return null;
	}

}
