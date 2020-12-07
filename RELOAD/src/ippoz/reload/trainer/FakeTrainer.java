/**
 * 
 */
package ippoz.reload.trainer;

import ippoz.reload.algorithm.DataSeriesDetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.manager.InputManager;
import ippoz.reload.meta.MetaData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class FakeTrainer extends AlgorithmTrainer {
	
	private AlgorithmModel aModel;
	
	private String confTmp;

	public FakeTrainer(LearnerType algTag, DataSeries dataSeries, List<Knowledge> kList, MetaData mData, String scoresFileString, String confTmp) {
		super(algTag, dataSeries, mData.getTargetMetric(), mData.getReputation(), kList, mData.getDatasetName(), mData.getKfold(), mData.getValidationMetrics());
		aModel = InputManager.loadAlgorithmModel(scoresFileString);
		this.confTmp = confTmp;
	}

	@Override
	protected ObjectPair<Map<Knowledge, List<AlgorithmResult>>, Double> lookForBestConfiguration() {
		Map<Knowledge, List<AlgorithmResult>> trainResult = new HashMap<>();
		aModel.getAlgorithm().loadLoggedScores();
		aModel.getAlgorithm().getConfiguration().addItem(DataSeriesDetectionAlgorithm.TAG, confTmp);
		bestConf = aModel.getAlgorithmConfiguration(); 
		// HERE
		for(Knowledge know : kList){
			trainResult.put(know, calculateResults(aModel.getAlgorithm(), know));
		}
		return new ObjectPair<Map<Knowledge, List<AlgorithmResult>>, Double>(trainResult, aModel.getMetricScore());
	}

	@Override
	public void saveAlgorithmScores() {
		aModel.getAlgorithm().saveLoggedScores();
	}

}
