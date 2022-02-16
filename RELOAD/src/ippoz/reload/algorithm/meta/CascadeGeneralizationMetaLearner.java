/**
 * 
 */
package ippoz.reload.algorithm.meta;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.Snapshot;
import ippoz.reload.meta.MetaLearnerType;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class CascadeGeneralizationMetaLearner extends CascadingMetaLearner {

	public CascadeGeneralizationMetaLearner(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, MetaLearnerType.CASCADE_GENERALIZATION);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void updateKnowledge(List<Knowledge> kList, DetectionAlgorithm alg, DataSeries currentDs) {
		if(alg != null){
			for(Knowledge know : kList){
				List<Snapshot> snapList = Knowledge.toSnapList(kList, getDataSeries());
				for(int i=0;i<know.size();i++){
					double[] snapArray = getSnapValueArray(snapList.get(i));
					know.addIndicatorData(i, alg.getLearnerType().toCompactString(), Double.valueOf(alg.calculateSnapshotScore(parseArray(snapArray, alg.getDataSeries())).getKey()));
				}
			}
			currentDs = updateDataSeries(currentDs, alg);
		}
	}
	
	private DataSeries updateDataSeries(DataSeries old, DetectionAlgorithm alg){
		List<DataSeries> list = old.listSubSeries();
		list.add(new DataSeries(new Indicator(alg.getLearnerType().toCompactString(), String.class)));
		return new DataSeries(list);
	}

	@Override
	protected double[] updateScoreArray(double[] snapArray, double score) {
		double[] newArr = new double[snapArray.length + 1];
		for(int i=0;i<snapArray.length;i++){
			newArr[i] = snapArray[i];
		}
		newArr[snapArray.length] = score;
		return newArr;
	}
	
	

}
