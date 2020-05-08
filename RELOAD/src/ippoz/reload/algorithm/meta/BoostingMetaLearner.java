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
import ippoz.reload.meta.MetaLearnerType;
import ippoz.reload.meta.MetaTrainer;
import ippoz.reload.trainer.AlgorithmTrainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;

/**
 * @author Tommy
 *
 */
public class BoostingMetaLearner extends DataSeriesMetaLearner {
	
	/** how many times on average each train sample appears in the boosting samples */
	private static final double SAMPLING_RATIO = 3.0;
	
	public static final String N_ENSEMBLES = "ENSEMBLES_NUMBER";
	
	public static final int DEFAULT_ENSEMBLES = 10;
	
	public static final String LEARNING_SPEED = "LEARNING_SPEED";
	
	public static final int DEFAULT_SPEED = 2;

	public BoostingMetaLearner(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, MetaLearnerType.BOOSTING);
	}
	
	private BaseLearner getBaseLearner(){
		return ((MetaLearner)getLearnerType()).getBaseLearners()[0];
	}

	@Override
	protected List<AlgorithmTrainer> trainMetaLearner(List<Knowledge> kList) {
		List<AlgorithmTrainer> trainers = new LinkedList<>();
		Map<Knowledge, List<Double>> weights = new HashMap<>();
		int ensembles = getEnsemblesNumber();
		baseLearners = new LinkedList<>();
		try {
			int i = 0;
			while (i < ensembles){
				List<Knowledge> boostedKnowledge = deriveKnowledge(kList, weights, ensembles);
				AlgorithmTrainer at = trainWeakLearner(boostedKnowledge, i);
				DataSeriesNonSlidingAlgorithm alg = null;
				if(at != null){
					alg = (DataSeriesNonSlidingAlgorithm)DetectionAlgorithm.buildAlgorithm(getBaseLearner(), at.getDataSeries(), at.getBestConfiguration());
					baseLearners.add(alg);
					trainers.add(at);
					weights = updateWeights(alg, weights, getLearningSpeed());
				}
				i++;
			}
			return trainers;
		} catch (Exception e) {
			AppLogger.logException(getClass(), e, "Unable to complete Boosting for " + getLearnerType());
		}
		return null;
	}
	
	public static Map<Knowledge, List<Double>> updateWeights(DataSeriesNonSlidingAlgorithm alg, Map<Knowledge, List<Double>> weights, int learningSpeed) {
		for(Knowledge know : weights.keySet()){
			double wSum = 0.0;
			List<Double> weightList = weights.get(know);
			for(int i=0;i<know.size();i++){
				AlgorithmResult res = alg.evaluateSnapshot(know, i);
				if(res.getConfidence() > 0.5){
					double wCoefficient = learningSpeed*res.getConfidence();
					if(!res.isCorrect())
						weightList.set(i, weightList.get(i)*wCoefficient);
					else weightList.set(i, weightList.get(i)/wCoefficient);
				}
				wSum = wSum + weightList.get(i);
			}
			double normalizationFactor = wSum/(know.size()/SAMPLING_RATIO);
			for(int i=0;i<know.size();i++){
				weightList.set(i, weightList.get(i)/normalizationFactor);
			}
			weights.put(know, weightList);
		}
		return weights;
	}

	public static List<Knowledge> deriveKnowledge(List<Knowledge> kList, Map<Knowledge, List<Double>> weights, double ensembles) {
		List<Knowledge> weakKnow = new LinkedList<>();
		for(Knowledge know : kList){
			if(!weights.containsKey(know) || weights.get(know) == null || weights.get(know).size() == 0){
				List<Double> list = new ArrayList<Double>(know.size());
				for(int i=0;i<know.size();i++){
					list.add(SAMPLING_RATIO/ensembles);
				}
				weights.put(know, list);
			}
			weakKnow.add(know.sample(weights.get(know)));
		}
		return weakKnow;
	}

	private AlgorithmTrainer trainWeakLearner(List<Knowledge> kList, int iteration){
		MetaTrainer mTrainer = new MetaTrainer(data, (MetaLearner)getLearnerType());
		for(DataSeries ds : dataSeries.listSubSeries()){
			mTrainer.addTrainer(getBaseLearner(), ds, kList, false, true, String.valueOf(iteration));
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
	public Pair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		return calculateDefaultSnapshotScore(snapArray);
	}

	@Override
	protected boolean checkCalculationCondition(double[] snapArray) {
		return true;
	}
	
	private int getEnsemblesNumber(){
		return getLearnerType() instanceof MetaLearner && 
				((MetaLearner)getLearnerType()).hasPreference(N_ENSEMBLES) ? 
						Integer.parseInt(((MetaLearner)getLearnerType()).getPreference(N_ENSEMBLES)) : DEFAULT_ENSEMBLES;
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
		defPar.put(N_ENSEMBLES, new String[]{String.valueOf(DEFAULT_ENSEMBLES)});
		defPar.put(LEARNING_SPEED, new String[]{String.valueOf(DEFAULT_SPEED)});
		return defPar;
	}

}
