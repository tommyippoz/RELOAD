/**
 * 
 */
package ippoz.reload.commons.knowledge;

import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.knowledge.sliding.SlidingPolicyType;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.loader.DatasetIndex;
import ippoz.reload.commons.loader.LoaderBatch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public abstract class Knowledge implements Cloneable {
	
	private static int MAX_RANGE = 100;
	
	private DataSeries dataSeries;
	
	protected MonitoredData baseData;
	
	public Knowledge(MonitoredData baseData){
		this.baseData = baseData;
		dataSeries = new DataSeries(baseData.getIndicators());
	}
	
	@Override
	public String toString() {
		return "Knowledge [baseData=" + baseData.size() + "]";
	}
	
	public DataSeries getDataSeries(){
		return dataSeries;
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
	
	public List<Snapshot> buildSnapshotsFor(DataSeries dataSeries, int from, int to){
		List<Snapshot> outList = new ArrayList<Snapshot>(to-from);
		for(int i=from;i<to;i++){
			outList.add(buildSnapshotFor(i, dataSeries));
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
	
	public DatasetIndex getIndex(int index){
		return baseData.get(index).getIndex();
	}
	
	public Snapshot get(int index, DataSeries dataSeries) {
		return buildSnapshotFor(index, dataSeries);
	}	
	
	public Snapshot buildSnapshotFor(int index){
		return buildSnapshotFor(index, null);
	}
	
	public Snapshot buildSnapshotFor(int index, DataSeries dataSeries){
		if(dataSeries == null)
			return null;
		else return baseData.generateSnapshot(dataSeries, index);
	}
	
	public int size(){
		return baseData.size();
	}
	
	public int getInjectionCount(){
		return baseData.getInjections().size();
	}
	
	public LoaderBatch getID(){
		return baseData.getDataID();
	}
	
	public abstract List<Snapshot> toArray(DataSeries dataSeries);
	
	public abstract KnowledgeType getKnowledgeType();

	public InjectedElement getInjection(DatasetIndex obIndex) {
		return baseData.getInjectionAt(obIndex);
	}
	
	public InjectedElement getInjection(int i) {
		return baseData.getInjectionAt(i);
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
			Indicator[] indList = ds.getIndicators();
			for(int i=0;i<kSnapList.size();i++){
				Snapshot snap = kSnapList.get(i);
				if(includeFaulty || !includeFaulty && !snap.isAnomalous()) {
					for(int j=0;j<ds.size();j++){
						dataMatrix[insertIndex][j] = snap.getDoubleValueFor(indList[j]);
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
	
	public static List<Knowledge> generateKnowledge(List<MonitoredData> data) {
		return generateKnowledge(data, KnowledgeType.SINGLE, null, 0);
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
	
	public void addIndicatorData(int obId, String indName, Object indData){
		baseData.addIndicatorData(obId, indName, indData);
	}
	
	public static Knowledge findKnowledge(List<Knowledge> knowledgeList, Object expName) {
		for(Knowledge know : knowledgeList){
			if(know.getID().equals(expName))
				return know;
		}
		return null;
	}

	public Knowledge sample(double ratio) {
		MonitoredData sampled = new MonitoredData(getID(), baseData.getIndicators());
		for(int i=0;i<baseData.size();i++){
			if(Math.random() < ratio){
				sampled.addObservation(baseData.get(i), baseData.getInjectionAt(i));
			}
		}
		return new SingleKnowledge(sampled);
	}
	
	public Knowledge sample(List<Double> ratios) {
		MonitoredData sampled = new MonitoredData(getID(), baseData.getIndicators());
		for(int i=0;i<baseData.size();i++){
			if(Math.random() < ratios.get(i)){
				sampled.addObservation(baseData.get(i), baseData.getInjectionAt(i));
			}
		}
		return new SingleKnowledge(sampled);
	}

	public void addIndicator(Indicator indicator) {
		baseData.addIndicator(indicator);
	}

}
