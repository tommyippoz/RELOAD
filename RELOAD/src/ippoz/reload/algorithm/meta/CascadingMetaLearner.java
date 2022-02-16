/**
 * 
 */
package ippoz.reload.algorithm.meta;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.LearnerType;
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
public class CascadingMetaLearner extends DataSeriesMetaLearner {
	
	public static final String LEARNING_SPEED = "LEARNING_SPEED";
	
	public static final int DEFAULT_SPEED = 2;

	public static final String CONFIDENCE_THRESHOLD = "CONFIDENCE_THRESHOLD";
	
	public static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.99;

	public CascadingMetaLearner(DataSeries dataSeries, BasicConfiguration conf) {
		this(dataSeries, conf, MetaLearnerType.CASCADING);
	}

	protected CascadingMetaLearner(DataSeries dataSeries, BasicConfiguration conf, MetaLearnerType mlt) {
		super(dataSeries, conf, mlt);
	}

	@Override
	protected List<AlgorithmTrainer> trainMetaLearner(List<Knowledge> kList) {
		List<AlgorithmTrainer> trainers = new LinkedList<>();
		Map<Knowledge, List<Double>> weights = new HashMap<>();
		BaseLearner[] lList = getWeakLearners();
		DataSeries currentDs = getDataSeries();
		baseLearners = new LinkedList<>();
		try {
			int i = 0;
			while (lList != null && i < lList.length){
				List<Knowledge> boostedKnowledge = BoostingMetaLearner.deriveKnowledge(kList, weights, lList.length);
				AlgorithmTrainer at = trainWeakLearner(lList[i], boostedKnowledge, currentDs, i);
				if(at != null){
					at.saveAlgorithmScores();
					DetectionAlgorithm alg = DetectionAlgorithm.buildAlgorithm(lList[i], at.getDataSeries(), at.getBestConfiguration());
					baseLearners.add(alg);
					trainers.add(at);
					updateKnowledge(kList, alg, currentDs);
					weights = BoostingMetaLearner.updateWeights(alg, weights, getLearningSpeed());
				}
				i++;
			}
			return trainers;
		} catch (Exception e) {
			AppLogger.logException(getClass(), e, "Unable to complete Cascading for " + getLearnerType());
		}
		return null;
	}
	
	protected void updateKnowledge(List<Knowledge> kList, DetectionAlgorithm alg, DataSeries currentDs) {
		// TODO Auto-generated method stub
		
	}

	private BaseLearner[] getWeakLearners() {
		return ((MetaLearner)getLearnerType()).getBaseLearners();
	}

	private AlgorithmTrainer trainWeakLearner(LearnerType learner, List<Knowledge> kList, DataSeries currentDs, int iteration){
		MetaTrainer mTrainer = new MetaTrainer(data, (MetaLearner)getLearnerType());
		mTrainer.addTrainer(learner, currentDs, kList, false, true, String.valueOf(iteration));
		for(DataSeries ds : currentDs.listSubSeries()){
			mTrainer.addTrainer(learner, ds, kList, false, true, String.valueOf(iteration));
		}
		try {
			mTrainer.start();
			mTrainer.join();
			return mTrainer.getBestTrainer();
		} catch (Exception ex){
			return null;
		}
	}

	@Override
	public ObjectPair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		double count = 0.0;
		int i = 0;
		double threshold = getStopThreshold();
		double[] scores = new double[baseLearners.size()];
		for(DetectionAlgorithm alg : baseLearners){
			double[] algArray = parseArray(snapArray, alg.getDataSeries());
			double score = alg.calculateSnapshotScore(algArray).getKey();
			scores[i++] = score;
			if(alg.getDecisionFunction().classify(new AlgorithmResult(false, score, 0.0, null, false)) == AnomalyResult.ANOMALY){
				count = count + (0.5 + alg.getConfidence(score)*0.5);
			} else {
				count = count + (0.5 - alg.getConfidence(score)*0.5);
			}
			snapArray = updateScoreArray(snapArray, score);
			if(alg.getConfidence(score) >= threshold)
				break;
		}
		return new ObjectPair<Double, Object>(count/i, scores);
	}

	protected double[] updateScoreArray(double[] snapArray, double score) {
		return snapArray;
	}

	@Override
	protected boolean checkCalculationCondition(double[] snapArray) {
		return true;
	}
	
	private double getStopThreshold() {
		return getLearnerType() instanceof MetaLearner && 
				((MetaLearner)getLearnerType()).hasPreference(CONFIDENCE_THRESHOLD) ? 
						Double.parseDouble(((MetaLearner)getLearnerType()).getPreference(CONFIDENCE_THRESHOLD)) : DEFAULT_CONFIDENCE_THRESHOLD;
	}
	
	private int getLearningSpeed(){
		return getLearnerType() instanceof MetaLearner && 
				((MetaLearner)getLearnerType()).hasPreference(LEARNING_SPEED) ? 
						Integer.parseInt(((MetaLearner)getLearnerType()).getPreference(LEARNING_SPEED)) : DEFAULT_SPEED;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DetectionAlgorithm#getDefaultParameterValues()
	 */
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		defPar.put(CONFIDENCE_THRESHOLD, new String[]{String.valueOf(DEFAULT_CONFIDENCE_THRESHOLD)});
		defPar.put(LEARNING_SPEED, new String[]{String.valueOf(DEFAULT_SPEED)});
		return defPar;
	}
	
	@Override
	protected void updateConfiguration() {
		if(conf != null){
			conf.addItem(CONFIDENCE_THRESHOLD, getStopThreshold());
			conf.addItem(LEARNING_SPEED, getLearningSpeed());
		}
	}

}
