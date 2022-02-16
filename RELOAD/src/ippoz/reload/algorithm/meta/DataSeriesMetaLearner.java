/**
 * 
 */
package ippoz.reload.algorithm.meta;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.configuration.MetaConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.meta.MetaData;
import ippoz.reload.meta.MetaLearnerType;
import ippoz.reload.metric.BetterBigMetric;
import ippoz.reload.trainer.AlgorithmTrainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class DataSeriesMetaLearner extends DetectionAlgorithm {
	
	public static final String BASE_LEARNERS = "BASE_LEARNERS";
	
	private MetaLearnerType mlType;
	
	protected MetaData data;
	
	protected List<DetectionAlgorithm> baseLearners;
	
	private List<MetaScore> scores;
	
	private List<AlgorithmTrainer> mTrainer;

	protected DataSeriesMetaLearner(DataSeries dataSeries, BasicConfiguration conf, MetaLearnerType mlType) {
		super(dataSeries, conf);
		this.mlType = mlType;
		if(conf.hasItem(TMP_FILE)){
			baseLearners = loadLearners(conf.getItem(TMP_FILE) + "metaPreferences.csv");
		} else if(conf instanceof MetaConfiguration){
			data = ((MetaConfiguration)conf).generateMetaData();
		}
	}
	
	protected List<DetectionAlgorithm> loadLearners(String filename){
		List<AlgorithmModel> modelList = AlgorithmModel.fromFile(filename);
		List<DetectionAlgorithm> retList = new LinkedList<>();
		if(modelList != null && modelList.size() > 0){
			for(AlgorithmModel model : modelList){
				if(model != null){
					DetectionAlgorithm alg = model.getAlgorithm();
					if(alg != null)
						retList.add((DetectionAlgorithm) alg);
					else AppLogger.logError(getClass(), "ModelLoadingError", "Unable to decode model " + model.toString());
				} else AppLogger.logError(getClass(), "ModelLoadingError", "Unable to decode model");
			}
		}
		return retList;
	}
	
	protected String getMetaTag(){
		return mlType.toString();
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm#automaticInnerTraining(java.util.List, boolean)
	 */
	@Override
	public boolean automaticInnerTraining(List<Knowledge> kList) {
		List<Snapshot> snapList = Knowledge.toSnapList(kList, getDataSeries());
		
		updateConfiguration();
		mTrainer = trainMetaLearner(kList);
		
		scores = new LinkedList<MetaScore>();
		for(Snapshot snap : snapList){
			ObjectPair<Double, Object> res = calculateSnapshotScore(getSnapValueArray(snap));
			scores.add(new MetaScore(snap.snapToString(), res.getKey()));
		}
		
		conf.addItem(TMP_FILE, getFilename());
		
		return true;
	}
	
	@Override
	public void saveLoggedScores() {
		super.saveLoggedScores();
		printFile(getFilename(), "metaPreferences.csv", mTrainer);
	}

	@Override
	protected String getFilename() {
		String folder = getDefaultTmpFolder() + File.separatorChar + getLearnerType().toCompactString() + File.separatorChar;
		if(!new File(folder).exists())
			new File(folder).mkdirs();
		return folder;
	}
	
	protected double[] parseArray(double[] snapArray, DataSeries dataSeries) {
		int index = 0;
		List<DataSeries> algDs = dataSeries.listSubSeries();
		List<DataSeries> mainDs = getDataSeries().listSubSeries();
		double[] items = new double[algDs.size()];
		for(DataSeries ds : dataSeries.listSubSeries()){
			for(int i=0;i<mainDs.size();i++){
				if(ds.compareTo(mainDs.get(i)) == 0){
					items[index] = snapArray[i];
					break;
				}
			}
			index++;
		}
		return items;
	}

	protected void printFile(String filefolder, String filename, List<AlgorithmTrainer> tList){
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(filefolder + filename)));
			writer.write("*This file contains the details and the scores of each individual base learner that builds the ensemble. \n");
			writer.write("data_series,algorithm_type,reputation_score,avg_metric_score(" + data.getTargetMetric().getName() + "),std_metric_score(" + data.getTargetMetric().getName() + "),dataset,configuration\n");
			for(AlgorithmTrainer trainer : tList){
				if(trainer.getBestConfiguration() != null) {
					writer.write(trainer.getSeriesDescription() + "§" + 
							trainer.getAlgType().toString() + "§" +
							trainer.getReputationScore() + "§" + 
							trainer.getMetricAvgScore() + "§" +  
							trainer.getMetricStdScore() + "§" + 
							trainer.getDatasetName() + "§" +
							trainer.getBestConfiguration().toFileRow(false) + "\n");
				}	
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write meta-training preferences");
		}
	}
	
	

	@Override
	protected String getScoresFilename() {
		return getFilename() + "logged_scores.metascores";
	}

	protected abstract List<AlgorithmTrainer> trainMetaLearner(List<Knowledge> kList);
	
	protected abstract void updateConfiguration();

	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Double> getTrainScores() {
		List<Double> tScores = new ArrayList<>(scores.size());
		for(MetaScore score : scores){
			tScores.add(score.getScore());
		}
		return tScores;
	}

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub	
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub	
	}
	
	protected ObjectPair<Double, Object> calculateDefaultSnapshotScore(double[] snapArray) {
		double count = 0.0;
		int i = 0;
		double[] scores = new double[baseLearners.size()];
		for(DetectionAlgorithm alg : baseLearners){
			double[] algArray = parseArray(snapArray, alg.getDataSeries());
			double score = alg.calculateSnapshotScore(algArray).getKey();
			scores[i++] = score;
			if(alg.getTrainMetric() instanceof BetterBigMetric && alg.getTrainMetricScore() < 0){
				if(alg.getDecisionFunction().classify(new AlgorithmResult(false, score, 0.0, null, false)) == AnomalyResult.ANOMALY){
					count = count + (0.5 - alg.getConfidence(score)*0.5);
				} else {
					count = count + (0.5 + alg.getConfidence(score)*0.5);
				}
			} else {
				if(alg.getDecisionFunction().classify(new AlgorithmResult(false, score, 0.0, null, false)) == AnomalyResult.ANOMALY){
					count = count + (0.5 + alg.getConfidence(score)*0.5);
				} else {
					count = count + (0.5 - alg.getConfidence(score)*0.5);
				}
			}
		}
		return new ObjectPair<Double, Object>(count/i, scores);
	}
	
	/**
	 * The Class MetaScore.
	 */
	protected class MetaScore {
		
		/** The score. */
		private double score;
		
		/** The snap value. */
		private String snapValue;

		/**
		 * Instantiates a new SDO score.
		 *
		 * @param snapValue the snap value
		 * @param score the score
		 */
		public MetaScore(String snapValue, double score) {
			this.score = score;
			this.snapValue = snapValue;
		}

		/**
		 * Gets the hbos.
		 *
		 * @return the hbos
		 */
		public double getScore() {
			return score;
		}

		/**
		 * Gets the snap value.
		 *
		 * @return the snap value
		 */
		public String getSnapValue() {
			return snapValue;
		}
		
	}

	public MetaLearnerType getMetaType() {
		return mlType;
	}

	public MetaData getMetaData() {
		return data;
	}

}
