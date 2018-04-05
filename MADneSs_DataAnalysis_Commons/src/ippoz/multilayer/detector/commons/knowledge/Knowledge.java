/**
 * 
 */
package ippoz.multilayer.detector.commons.knowledge;

import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.dataseries.MultipleDataSeries;
import ippoz.multilayer.detector.commons.failure.InjectedElement;
import ippoz.multilayer.detector.commons.knowledge.data.MonitoredData;
import ippoz.multilayer.detector.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.multilayer.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.multilayer.detector.commons.knowledge.snapshot.SnapshotValue;
import ippoz.multilayer.detector.commons.service.ServiceStat;
import ippoz.multilayer.detector.commons.service.StatPair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public abstract class Knowledge {
	
	protected MonitoredData baseData;
	
	public Knowledge(MonitoredData baseData){
		this.baseData = baseData;
	}
	
	public List<Snapshot> buildSnapshotsFor(AlgorithmType algType, DataSeries dataSeries){
		List<Snapshot> outList = new ArrayList<Snapshot>(size());
		for(int i=0;i<size();i++){
			outList.add(buildSnapshotFor(algType, i, dataSeries));
		}
		return outList;
	}
	
	public Date getTimestamp(int index){
		return baseData.get(index).getTimestamp();
	}
	
	public Snapshot get(AlgorithmType algType, int index, DataSeries dataSeries) {
		return buildSnapshotFor(algType, index, dataSeries);
	}	
	
	public Snapshot buildSnapshotFor(AlgorithmType algType, int index){
		return buildSnapshotFor(algType, index, null);
	}
	
	public MultipleSnapshot generateMultipleSnapshot(int index,	MultipleDataSeries invDs) {
		return baseData.generateMultipleSnapshot(invDs, index);
	}
	
	public Snapshot buildSnapshotFor(AlgorithmType algType, int index, DataSeries dataSeries){
		switch(algType){
			case RCC:
				return baseData.generateSnapshot(index);
			case INV:
				if(dataSeries instanceof MultipleDataSeries)
					return baseData.generateMultipleSnapshot((MultipleDataSeries)dataSeries, index);
				else return null;
			default:
				if(dataSeries.size() == 1)
					return baseData.generateDataSeriesSnapshot(dataSeries, index);
				else return baseData.generateMultipleSnapshot((MultipleDataSeries)dataSeries, index);
		}
	}

	public List<SnapshotValue> getDataSeriesValues(DataSeries ds){
		List<SnapshotValue> outList = new ArrayList<SnapshotValue>(baseData.size());
		for(int i=0;i<baseData.size();i++){
			outList.add(ds.getSeriesValue(baseData.get(i)));
		}
		return outList;
	}
	
	public Map<String, ServiceStat> getStats(){
		return baseData.getStats();
	}
	
	public ServiceStat getStat(String serviceName){
		return baseData.getStats().get(serviceName);
	}
	
	public int size(){
		return baseData.size();
	}
	
	public String getTag(){
		return baseData.getDataTag();
	}
	
	public abstract List<Snapshot> toArray(AlgorithmType algType, DataSeries dataSeries);
	
	public abstract KnowledgeType getKnowledgeType();

	public InjectedElement getInjection(int obIndex) {
		return baseData.getInjection(obIndex);
	}
	
	/**
	 * Gets the service obs stat.
	 *
	 * @return the service obs stat
	 */
	public StatPair getServiceObsStat(String serviceName){
		return getStats().get(serviceName).getObsStat();
	}
	
	/**
	 * Gets the service timing stat.
	 *
	 * @return the service timing stat
	 */
	public StatPair getServiceTimingStat(String serviceName){
		return getStats().get(serviceName).getTimeStat();
	}
	
	public static int goldenPointsSize(List<Snapshot> knowledgeSnapshots) {
		return knowledgeSnapshots.size() - faultyPointsSize(knowledgeSnapshots);
	}

	public static int faultyPointsSize(List<Snapshot> knowledgeSnapshots) {
		int count = 0;
		for(Snapshot snap : knowledgeSnapshots){
			if(snap.getInjectedElement() != null)
				count++;
		}
		return count;
	}
	
	

}
