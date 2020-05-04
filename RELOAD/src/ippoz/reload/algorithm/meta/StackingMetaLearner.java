/**
 * 
 */
package ippoz.reload.algorithm.meta;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.IndicatorDataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.support.AppLogger;
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
public class StackingMetaLearner extends DataSeriesMetaLearner {
	
	public static final String BASE_LEARNERS = "BASE_LEARNERS";
	
	public static final String STACKING_LEARNER = "STACKING_LEARNER";
	
	public static final BaseLearner DEFAULT_META_LEARNER = new BaseLearner(AlgorithmType.ELKI_ODIN);
	
	private DataSeriesNonSlidingAlgorithm metaLearner;
	
	private MetaTrainer sTrainer;

	public StackingMetaLearner(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, MetaLearnerType.STACKING);
		if(conf.hasItem(TMP_FILE)){
			List<DataSeriesNonSlidingAlgorithm> mlList = loadLearners(conf.getItem(TMP_FILE) + "stackingPreferences.csv");
			if(mlList != null && mlList.size() > 0)
				metaLearner = mlList.get(0);
		}
	}
	
	private BaseLearner getStackingLearner(){
		return getLearnerType() instanceof MetaLearner && 
				((MetaLearner)getLearnerType()).hasPreference(STACKING_LEARNER) ? 
						(BaseLearner)LearnerType.fromString(((MetaLearner)getLearnerType()).getPreference(STACKING_LEARNER)) : DEFAULT_META_LEARNER;
	}
	
	private BaseLearner[] getBaseLearners(){
		return ((MetaLearner)getLearnerType()).getBaseLearners();
	}

	@Override
	protected MetaTrainer trainMetaLearner(List<Knowledge> kList) {
		MetaTrainer mTrainer = new MetaTrainer(data, (MetaLearner)getLearnerType());
		try {
			for(BaseLearner base : getBaseLearners()){
				mTrainer.addTrainer(base, dataSeries, kList, true);
			}
			mTrainer.start();
			mTrainer.join();
			baseLearners = new LinkedList<>();
			for(AlgorithmTrainer at : mTrainer.getTrainers()){
				baseLearners.add((DataSeriesNonSlidingAlgorithm)DetectionAlgorithm.buildAlgorithm(at.getAlgType(), dataSeries, at.getBestConfiguration()));
			}
			
			// Train Meta-Learner
			sTrainer = new MetaTrainer(data, (MetaLearner)getLearnerType());
			sTrainer.addTrainer(getStackingLearner(), getStackingDataSeries(), getStackingKnowledge(kList), true);
			sTrainer.start();
			sTrainer.join();
			for(AlgorithmTrainer at : sTrainer.getTrainers()){
				metaLearner = (DataSeriesNonSlidingAlgorithm)DetectionAlgorithm.buildAlgorithm(at.getAlgType(), at.getDataSeries(), at.getBestConfiguration());
			}
			
		} catch (InterruptedException e) {
			AppLogger.logException(getClass(), e, "Unable to complete Meta-Training for " + getLearnerType());
		}
		return mTrainer;
	}

	private List<Knowledge> getStackingKnowledge(List<Knowledge> kList) {
		// TODO Auto-generated method stub
		return null;
	}

	private DataSeries getStackingDataSeries() {
		List<DataSeries> sList = new LinkedList<>();
		for(DataSeriesNonSlidingAlgorithm alg : baseLearners){
			sList.add(new IndicatorDataSeries(new Indicator(alg.getLearnerType().toCompactString(), LayerType.NO_LAYER, Double.class), DataCategory.PLAIN));
		}
		return new MultipleDataSeries(sList);
	}

	@Override
	public void saveLoggedScores() {
		super.saveLoggedScores();
		printFile(getFilename(), "stackingPreferences.csv", sTrainer.getTrainers());
	}

	@Override
	public Pair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		int i = 0;
		double[] scores = new double[baseLearners.size()];
		for(DataSeriesNonSlidingAlgorithm alg : baseLearners){
			double score = alg.calculateSnapshotScore(snapArray).getKey();
			scores[i++] = score;
		}
		return new Pair<Double, Object>(metaLearner.calculateSnapshotScore(scores).getKey(), scores);
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
