/**
 * 
 */
package ippoz.reload.algorithm.meta;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.meta.MetaData;
import ippoz.reload.meta.MetaLearnerType;

import java.io.File;
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

	protected DataSeriesMetaLearner(DataSeries dataSeries, BasicConfiguration conf, MetaLearnerType mlType, MetaData data) {
		super(dataSeries, conf);
		this.mlType = mlType;
		this.data = data;
		if(conf.hasItem(TMP_FILE)){
			loadFile(getFilename());
		}
	}
	
	protected abstract void loadFile(String filename);

	@Override
	protected String getFilename(){
		return super.getFilename() + "_" + mlType.toString();
	}
	
	public static DataSeriesMetaLearner buildMetaLearner(DataSeries dataSeries,	BasicConfiguration conf, MetaLearnerType mlType, MetaData data){
		switch(mlType){
			case BAGGING:
				return new BaggingMetaLearner(dataSeries, conf, data);
			default: 
				return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm#automaticInnerTraining(java.util.List, boolean)
	 */
	@Override
	public boolean automaticInnerTraining(List<Knowledge> kList, boolean createOutput) {
		List<Snapshot> snapList = Knowledge.toSnapList(kList, getDataSeries());
		
		trainMetaLearner(kList);
		
		scores = new LinkedList<MetaScore>();
		for(Snapshot snap : snapList){
			Pair<Double, Object> res = calculateSnapshotScore(getSnapValueArray(snap));
			scores.add(new MetaScore(Snapshot.snapToString(snap, getDataSeries()), res.getKey()));
		}
		
		conf.addItem(TMP_FILE, getFilename());
		
		if(createOutput) {
	    	printFile(new File(getFilename()));
		}
		
		return true;
	}

	protected abstract void printFile(File file);

	protected abstract void trainMetaLearner(List<Knowledge> kList);

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
