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
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.meta.MetaLearnerType;
import ippoz.reload.meta.MetaTrainer;
import ippoz.reload.trainer.AlgorithmTrainer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class DelegatingMetaLearner extends DataSeriesMetaLearner {
	
	public static final String CONFIDENCE_THRESHOLD = "CONFIDENCE_THRESHOLD";
	
	public static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.99;

	public DelegatingMetaLearner(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, MetaLearnerType.DELEGATING);
	}
	
	private BaseLearner[] getBaseLearners(){
		return ((MetaLearner)getLearnerType()).getBaseLearners();
	}

	@Override
	protected List<AlgorithmTrainer> trainMetaLearner(List<Knowledge> kList) {
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
			return mTrainer.getTrainers();
		} catch (InterruptedException e) {
			AppLogger.logException(getClass(), e, "Unable to complete Meta-Training for " + getLearnerType());
		}
		return null;
	}

	@Override
	public ObjectPair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		int i = 0;
		double score = Double.NaN;
		double thr = getStopThreshold();
		double[] scores = new double[baseLearners.size()];
		for(DataSeriesNonSlidingAlgorithm alg : baseLearners){
			score = alg.calculateSnapshotScore(snapArray).getKey();
			scores[i++] = score;
			//System.out.println(alg.getConfidence(score));
			if(alg.getConfidence(score) >= thr || i >= baseLearners.size()){
				if(alg.getDecisionFunction().classify(new AlgorithmResult(false, score, 0.0, null)) == AnomalyResult.ANOMALY){
					score = 0.5 + alg.getConfidence(score)*0.5;
				} else {
					score = 0.5 - alg.getConfidence(score)*0.5;
				}
				break;
			}
		}
		return new ObjectPair<Double, Object>(score, scores);
	}
	
	private double getStopThreshold() {
		return getLearnerType() instanceof MetaLearner && 
				((MetaLearner)getLearnerType()).hasPreference(CONFIDENCE_THRESHOLD) ? 
						Double.parseDouble(((MetaLearner)getLearnerType()).getPreference(CONFIDENCE_THRESHOLD)) : DEFAULT_CONFIDENCE_THRESHOLD;
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
