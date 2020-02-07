/**
 * 
 */
package ippoz.reload.commons.knowledge;

import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.knowledge.sliding.SlidingPolicyType;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.knowledge.snapshot.SnapshotValue;
import ippoz.reload.commons.service.ServiceStat;
import ippoz.reload.commons.service.StatPair;
import ippoz.reload.commons.support.AppLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public abstract class Knowledge implements Cloneable {
	
	private static int MAX_RANGE = 100;
	
	protected MonitoredData baseData;
	
	public Knowledge(MonitoredData baseData){
		this.baseData = baseData;
	}
	
	public Indicator[] getIndicators(){
		return baseData.getIndicators();
	}
	
	public List<Snapshot> buildSnapshotsFor(AlgorithmType algType, DataSeries dataSeries){
		List<Snapshot> outList = new ArrayList<Snapshot>(size());
		for(int i=0;i<size();i++){
			outList.add(buildSnapshotFor(i, dataSeries));
		}
		return outList;
	}
	
	public List<Snapshot> buildSnapshotsFor(AlgorithmType algType, DataSeries dataSeries, int from, int to){
		List<Snapshot> outList = new ArrayList<Snapshot>(to-from);
		for(int i=from;i<to;i++){
			outList.add(buildSnapshotFor(algType, i, dataSeries));
		}
		return outList;
	}
	
	public List<Snapshot> buildSnapshotsFor(DataSeries dataSeries, List<Integer> indexes){
		List<Snapshot> outList = new ArrayList<Snapshot>(indexes.size());
		for(Integer index : indexes){
			outList.add(buildSnapshotFor(index, dataSeries));
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
	
	public Snapshot buildSnapshotFor(int index, DataSeries dataSeries){
		if(dataSeries.size() == 1)
			return baseData.generateDataSeriesSnapshot(dataSeries, index);
		else return baseData.generateMultipleSnapshot((MultipleDataSeries)dataSeries, index);
	}
	
	public Snapshot buildSnapshotFor(AlgorithmType algType, int index, DataSeries dataSeries){
		if(dataSeries == null)
			return null;
		if(dataSeries.size() == 1)
			return baseData.generateDataSeriesSnapshot(dataSeries, index);
		else return baseData.generateMultipleSnapshot((MultipleDataSeries)dataSeries, index);
	}
	
	public SnapshotValue getDataSeriesValue(DataSeries ds, int i){
		return ds.getSeriesValue(baseData.get(i));
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
	
	public int getInjectionCount(){
		return baseData.getInjections().size();
	}
	
	public String getTag(){
		return baseData.getDataTag();
	}
	
	public abstract List<Snapshot> toArray(DataSeries dataSeries);
	
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
 
	public abstract Knowledge cloneKnowledge();
	
	public static double[][] convertKnowledgeIntoMatrix(List<Knowledge> kList, DataSeries ds, boolean includeFaulty, boolean needNormalization) {
		return convertSnapshotListIntoMatrix(toSnapList(kList, ds), ds, includeFaulty, needNormalization);
	}
	
	public static String[] extractLabels(List<Knowledge> kList, DataSeries ds, boolean includeFaulty) {
		return extractLabels(includeFaulty, toSnapList(kList, ds));
	}
	
	public static String[] extractLabels(boolean includeFaulty, List<Snapshot> kSnapList) {
		int insertIndex = 0;
		String[] anomalyLabels;
		if(includeFaulty)
			anomalyLabels = new String[kSnapList.size()];
		else anomalyLabels = new String[Knowledge.goldenPointsSize(kSnapList)]; 
		if(anomalyLabels.length > 0) {
			for(int i=0;i<kSnapList.size();i++){
				if(includeFaulty || !includeFaulty && kSnapList.get(i).getInjectedElement() == null) {
					anomalyLabels[insertIndex] = kSnapList.get(i).getInjectedElement() == null ? "no" : "yes";
					insertIndex++;
				}
			}
		}
		return anomalyLabels;
	}
	
	public static double[][] convertSnapshotListIntoMatrix(List<Snapshot> kSnapList, DataSeries ds, boolean includeFaulty, boolean needNormalization) {
		int insertIndex = 0;
		double[][] dataMatrix;
		double[][] minmax = new double[ds.size()][2];
		if(includeFaulty)
			dataMatrix = new double[kSnapList.size()][ds.size()];
		else dataMatrix = new double[Knowledge.goldenPointsSize(kSnapList)][ds.size()]; 
		if(dataMatrix.length > 0) {
			for(int i=0;i<kSnapList.size();i++){
				if(includeFaulty || !includeFaulty && kSnapList.get(i).getInjectedElement() == null) {
					if(ds.size() == 1){
						dataMatrix[insertIndex][0] = ((DataSeriesSnapshot)kSnapList.get(i)).getSnapValue().getFirst();
						if(insertIndex == 0){
							minmax[0][0] = dataMatrix[insertIndex][0];
							minmax[0][1] = dataMatrix[insertIndex][0];
						} else {
							if(dataMatrix[insertIndex][0] < minmax[0][0])
								minmax[0][0] = dataMatrix[insertIndex][0];
							if(dataMatrix[insertIndex][0] > minmax[0][1])
								minmax[0][1] = dataMatrix[insertIndex][0];
						}
					} else {
						for(int j=0;j<ds.size();j++){
							if(((MultipleSnapshot)kSnapList.get(i)).getSnapshot(((MultipleDataSeries)ds).getSeries(j)).getSnapValue() == null){
								AppLogger.logError(Knowledge.class, "UnrecognizableSnapshot", ((MultipleDataSeries)ds).getSeries(j).getName() + " - " + i + " - " + j);
								dataMatrix[insertIndex][j] = 0.0;
							} else dataMatrix[insertIndex][j] = ((MultipleSnapshot)kSnapList.get(i)).getSnapshot(((MultipleDataSeries)ds).getSeries(j)).getSnapValue().getFirst();
							if(insertIndex == 0){
								minmax[j][0] = dataMatrix[insertIndex][0];
								minmax[j][1] = dataMatrix[insertIndex][0];
							} else {
								if(dataMatrix[insertIndex][0] < minmax[j][0])
									minmax[j][0] = dataMatrix[insertIndex][0];
								if(dataMatrix[insertIndex][0] > minmax[j][1])
									minmax[j][1] = dataMatrix[insertIndex][0];
							}
						}
					}
					insertIndex++;
				}
			}
			if(needNormalization){
				for(int j=0;j<ds.size();j++){
					if(minmax[j][1] - minmax[j][0] >= MAX_RANGE){
						for(int i=0;i<dataMatrix.length;i++){
							dataMatrix[i][j] = (dataMatrix[i][j] - minmax[j][0])/(minmax[j][1] - minmax[j][0]);
						}
					}
				}
			}
		}
		return dataMatrix;
	}

	public static List<Snapshot> toSnapList(List<Knowledge> kList, DataSeries ds) {
		List<Snapshot> kSnapList = null;
		for(Knowledge knowledge : kList){
			if(kSnapList == null)
				kSnapList = knowledge.toArray(ds);
			else kSnapList.addAll(knowledge.toArray(ds));
		}
		return kSnapList;
	}
	
	public static List<Knowledge> generateKnowledge(List<MonitoredData> expList, KnowledgeType kt, SlidingPolicyType sPolicy, int windowSize) {
		List<Knowledge> map = new LinkedList<Knowledge>();
		for(int i=0;i<expList.size();i++){
			if(kt == KnowledgeType.GLOBAL)
				map.add(new GlobalKnowledge(expList.get(i)));
			if(kt == KnowledgeType.SLIDING)
				map.add(new SlidingKnowledge(expList.get(i), sPolicy, windowSize));
			if(kt == KnowledgeType.SINGLE)
				map.add(new SingleKnowledge(expList.get(i)));
		}
		return map;
	}
	
	public static Indicator[] getIndicators(Map<KnowledgeType, List<Knowledge>> kMap) {
		List<Knowledge> kList;
		if(kMap.size() > 0){
			kList = kMap.get(kMap.keySet().iterator().next());
			if(kList.size() > 0){
				return kList.get(0).getIndicators();
			} else return null;
		} else return null;

	}
	
	public void addIndicatorData(int obId, String indName, String indData, DataCategory dataTag){
		baseData.get(obId).addIndicatorData(indName, indData, dataTag);
	}

	public boolean hasIndicatorData(int obId, String indicatorName, DataCategory categoryTag) {
		return baseData.get(obId).hasIndicator(indicatorName, categoryTag);
	}

}
