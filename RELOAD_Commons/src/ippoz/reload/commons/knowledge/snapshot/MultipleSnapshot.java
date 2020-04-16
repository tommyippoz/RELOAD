/**
 * 
 */
package ippoz.reload.commons.knowledge.snapshot;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.service.ServiceCall;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class MultipleSnapshot extends Snapshot {

	private Map<DataSeries, DataSeriesSnapshot> dsMap;
	
	public MultipleSnapshot(Observation obs, InjectedElement injEl, List<DataSeries> seriesList) {
		super(obs.getTimestamp(), injEl);
		dsMap = generateMultipleSnapshots(obs, seriesList);
	}

	private Map<DataSeries, DataSeriesSnapshot> generateMultipleSnapshots(Observation obs, List<DataSeries> seriesList) {
		Map<DataSeries, DataSeriesSnapshot> outMap = new HashMap<DataSeries, DataSeriesSnapshot>();
		for(DataSeries ds : seriesList){
			outMap.put(ds, new DataSeriesSnapshot(obs, getInjectedElement(), ds));
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
	
	public SnapshotValue getSeriesValue(int index){
		Iterator<DataSeries> iterator = dsMap.keySet().iterator();
		if(index < dsMap.size()){
			while(index > 0){
				index--;
				iterator.next();
			}
			return dsMap.get(iterator.next()).getSnapValue();
		} else return null;
	}

	@Override
	public List<SnapshotValue> listValues() {
		List<SnapshotValue> list = new LinkedList<SnapshotValue>();		
		for(int i=0;i<dsMap.size();i++){
			list.add(getSeriesValue(i));
		}
		return list;
	}

}
