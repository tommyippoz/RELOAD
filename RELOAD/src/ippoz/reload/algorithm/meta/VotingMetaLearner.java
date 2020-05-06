/**
 * 
 */
package ippoz.reload.algorithm.meta;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.meta.MetaLearnerType;
import ippoz.reload.meta.MetaTrainer;
import ippoz.reload.trainer.AlgorithmTrainer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;

/**
 * @author Tommy
 *
 */
public class VotingMetaLearner extends DataSeriesMetaLearner {
	
	public static final String BASE_LEARNERS = "BASE_LEARNERS";

	public VotingMetaLearner(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, MetaLearnerType.VOTING);
	}
	
	private BaseLearner[] getBaseLearners(){
		return ((MetaLearner)getLearnerType()).getBaseLearners();
	}

	@Override
	protected MetaTrainer trainMetaLearner(List<Knowledge> kList) {
		MetaTrainer mTrainer = new MetaTrainer(data, (MetaLearner)getLearnerType());
		try {
			for(BaseLearner base : getBaseLearners()){
				mTrainer.addTrainer(base, dataSeries, kList, true, true);
			}
			mTrainer.start();
			mTrainer.join();
			baseLearners = new LinkedList<>();
			for(AlgorithmTrainer at : mTrainer.getTrainers()){
				baseLearners.add((DataSeriesNonSlidingAlgorithm)DetectionAlgorithm.buildAlgorithm(at.getAlgType(), dataSeries, at.getBestConfiguration()));
			}
		} catch (InterruptedException e) {
			AppLogger.logException(getClass(), e, "Unable to complete Meta-Training for " + getLearnerType());
		}
		return mTrainer;
	}

	@Override
	public Pair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		double count = 0;
		int i = 0;
		double[] scores = new double[baseLearners.size()];
		for(DataSeriesNonSlidingAlgorithm alg : baseLearners){
			double score = alg.calculateSnapshotScore(snapArray).getKey();
			scores[i++] = score;
			if(alg.getDecisionFunction().classify(new AlgorithmResult(snapArray, null, score, 0.0, null)) == AnomalyResult.ANOMALY){
				count++;
			}
		}
		return new Pair<Double, Object>(count, scores);
	}

	@Override
	protected boolean checkCalculationCondition(double[] snapArray) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DetectionAlgorithm#getDefaultParameterValues()
	 */
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		return defPar;
	}

}
