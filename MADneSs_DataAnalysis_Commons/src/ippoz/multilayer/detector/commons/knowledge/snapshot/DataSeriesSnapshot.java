/**
 * 
 */
package ippoz.multilayer.detector.commons.knowledge.snapshot;

import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.failure.InjectedElement;
import ippoz.multilayer.detector.commons.knowledge.data.Observation;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;
import ippoz.multilayer.detector.commons.service.StatPair;

import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class DataSeriesSnapshot extends Snapshot {

	private DataSeries dataSeries;
	private SnapshotValue snapValue;
	
	public DataSeriesSnapshot(Observation obs, List<ServiceCall> list, InjectedElement injEl, DataSeries dataSeries) {
		super(obs.getTimestamp(), list, injEl);
		this.dataSeries = dataSeries;
		if(dataSeries != null)
			snapValue = dataSeries.getSeriesValue(obs);
		else snapValue = new SnapshotValue(Double.NaN);
	}

	public DataSeries getDataSeries() {
		return dataSeries;
	}

	public SnapshotValue getSnapValue() {
		return snapValue;
	}

	public StatPair getSnapStat(ServiceCall sCall, Map<String, ServiceStat> stats) {
		return dataSeries.getSeriesServiceStat(getTimestamp(), sCall, stats.get(sCall.getServiceName()));
	}

}
