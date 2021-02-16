/**
 * 
 */
package ippoz.reload.algorithm.meta;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.meta.MetaLearnerType;
import ippoz.reload.meta.MetaTrainer;
import ippoz.reload.trainer.AlgorithmTrainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class BaggingMetaLearner extends DataSeriesMetaLearner {
	
	public static final String N_SAMPLES = "SAMPLES_NUMBER";
	
	public static final int DEFAULT_SAMPLES = 10;

	public BaggingMetaLearner(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, MetaLearnerType.BAGGING);
	}
	
	private BaseLearner getBaseLearner(){
		return ((MetaLearner)getLearnerType()).getBaseLearners()[0];
	}

	@Override
	protected List<AlgorithmTrainer> trainMetaLearner(List<Knowledge> kList) {
		List<List<Knowledge>> sampledKnowledge = null;
		MetaTrainer mTrainer = new MetaTrainer(data, (MetaLearner)getLearnerType());
		try {
			sampledKnowledge = baggingOf(kList, getSamplesNumber());
			for(List<Knowledge> sKnow : sampledKnowledge){
				mTrainer.addTrainer(getBaseLearner(), dataSeries, sKnow, false, true);
			}
			mTrainer.start();
			mTrainer.join();
			baseLearners = new LinkedList<>();
			for(AlgorithmTrainer at : mTrainer.getTrainers()){
				at.saveAlgorithmScores();
				baseLearners.add((DataSeriesNonSlidingAlgorithm)DetectionAlgorithm.buildAlgorithm(getBaseLearner(), dataSeries, at.getBestConfiguration()));
			}
			return mTrainer.getTrainers();
		} catch (InterruptedException e) {
			AppLogger.logException(getClass(), e, "Unable to complete Meta-Training for " + getLearnerType());
		}
		return null;
	}

	private List<List<Knowledge>> baggingOf(List<Knowledge> kList, int samplesNumber) {
		List<List<Knowledge>> outList = new ArrayList<>(samplesNumber);
		for(int i=0;i<samplesNumber;i++){
			List<Knowledge> sList = new ArrayList<>(kList.size());
			for(Knowledge know : kList){
				sList.add(know.sample(1.5/samplesNumber));
			}
			outList.add(sList);
		}
		return outList;
	}

	/*@Override
	public Pair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		int count = 0, i = 0;
		double sum = 0;
		double[] scores = new double[baseLearners.size()];
		for(DataSeriesNonSlidingAlgorithm alg : baseLearners){
			double score = alg.calculateSnapshotScore(snapArray).getKey();
			scores[i++] = score;
			if(Double.isFinite(score)){
				sum = sum + score;
				count++;
			}
		}
		if(count > 0)
			return new Pair<Double, Object>(sum / count, scores);
		else return new Pair<Double, Object>(0.0, null);
	}*/
	
	@Override
	public ObjectPair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		return calculateDefaultSnapshotScore(snapArray);
	}

	@Override
	protected boolean checkCalculationCondition(double[] snapArray) {
		return true;
	}
	
	private int getSamplesNumber(){
		return getLearnerType() instanceof MetaLearner && 
				((MetaLearner)getLearnerType()).hasPreference(N_SAMPLES) ? 
						Integer.parseInt(((MetaLearner)getLearnerType()).getPreference(N_SAMPLES)) : DEFAULT_SAMPLES;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DetectionAlgorithm#getDefaultParameterValues()
	 */
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		defPar.put(N_SAMPLES, new String[]{"10"});
		return defPar;
	}
	
	@Override
	protected void updateConfiguration() {
		if(conf != null){
			conf.addItem(N_SAMPLES, getSamplesNumber());
		}
	}

}
