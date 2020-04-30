/**
 * 
 */
package ippoz.reload.algorithm.meta;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.configuration.MetaConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.meta.MetaData;
import ippoz.reload.meta.MetaLearnerType;
import ippoz.reload.meta.MetaTrainer;
import ippoz.reload.trainer.AlgorithmTrainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javafx.util.Pair;

/**
 * @author Tommy
 *
 */
public abstract class DataSeriesMetaLearner extends DataSeriesNonSlidingAlgorithm {
	
	private MetaLearnerType mlType;
	
	protected MetaData data;
	
	protected List<DataSeriesNonSlidingAlgorithm> baseLearners;
	
	private List<MetaScore> scores;

	protected DataSeriesMetaLearner(DataSeries dataSeries, BasicConfiguration conf, MetaLearnerType mlType) {
		super(dataSeries, conf);
		this.mlType = mlType;
		if(conf.hasItem(TMP_FILE)){
			baseLearners = loadLearners(conf.getItem(TMP_FILE) + "metaPreferences.csv");
		} else if(conf instanceof MetaConfiguration){
			data = ((MetaConfiguration)conf).generateMetaData();
		}
	}
	
	private List<DataSeriesNonSlidingAlgorithm> loadLearners(String filename){
		List<AlgorithmModel> modelList = AlgorithmModel.fromFile(filename);
		List<DataSeriesNonSlidingAlgorithm> retList = new LinkedList<>();
		if(modelList != null && modelList.size() > 0){
			for(AlgorithmModel model : modelList){
				if(model != null){
					DetectionAlgorithm alg = model.getAlgorithm();
					if(alg != null && alg instanceof DataSeriesNonSlidingAlgorithm)
						retList.add((DataSeriesNonSlidingAlgorithm) alg);
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
	public boolean automaticInnerTraining(List<Knowledge> kList, boolean createOutput) {
		List<Snapshot> snapList = Knowledge.toSnapList(kList, getDataSeries());
		
		MetaTrainer mTrainer = trainMetaLearner(kList);
		
		scores = new LinkedList<MetaScore>();
		for(Snapshot snap : snapList){
			Pair<Double, Object> res = calculateSnapshotScore(getSnapValueArray(snap));
			scores.add(new MetaScore(Snapshot.snapToString(snap, getDataSeries()), res.getKey()));
		}
		
		conf.addItem(TMP_FILE, getFilename());
		
		if(createOutput) {
	    	printFile(getFilename(), mTrainer.getTrainers());
		}
		
		return true;
	}
	
	@Override
	protected String getFilename() {
		return getDefaultTmpFolder() + File.separatorChar + getLearnerType().toCompactString() + File.separatorChar;
	}

	private void printFile(String filename, List<AlgorithmTrainer> tList){
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(filename + "metaPreferences.csv")));
			writer.write("*This file contains the details and the scores of each individual base learner that builds the ensemble. \n");
			writer.write("data_series,algorithm_type,reputation_score,avg_metric_score(" + data.getTargetMetric().getMetricName() + "),std_metric_score(" + data.getTargetMetric().getMetricName() + "),dataset,configuration\n");
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

	protected abstract MetaTrainer trainMetaLearner(List<Knowledge> kList);

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
