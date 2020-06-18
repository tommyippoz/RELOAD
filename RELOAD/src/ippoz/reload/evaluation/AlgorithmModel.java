/**
 * 
 */
package ippoz.reload.evaluation;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.utils.ObjectPair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class AlgorithmVoter.
 * Used to score a specific pre-instantiated algorithm.
 *
 * @author Tommy
 */
public class AlgorithmModel implements Cloneable, Comparable<AlgorithmModel> {
	
	/** The algorithm. */
	private DetectionAlgorithm alg;
	
	/** The metric score. */
	private double metricScore;
	
	/** The reputation score. */
	private double reputationScore;	
	
	/**
	 * Instantiates a new algorithm voter.
	 *
	 * @param alg the algorithm
	 * @param metricScore the metric score
	 * @param reputationScore the reputation score
	 */
	public AlgorithmModel(DetectionAlgorithm alg, double metricScore, double reputationScore) {
		this.alg = alg;
		this.metricScore = metricScore;
		this.reputationScore = reputationScore;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected AlgorithmModel clone() throws CloneNotSupportedException {
		return new AlgorithmModel(DetectionAlgorithm.buildAlgorithm(alg.getLearnerType(), alg.getDataSeries(), alg.getConfiguration()), metricScore, reputationScore);
	}

	/**
	 * Votes the selected snapshot.
	 *
	 * @param snap the snapshot
	 * @return the anomaly voting.
	 */
	public AlgorithmResult voteKnowledgeSnapshot(Knowledge knowledge, int i) {
		return alg.snapshotAnomalyRate(knowledge, i);
	}
	
	public ObjectPair<Double, Object> calculateSnapshotScore(double[] snapArray){
		return alg.calculateSnapshotScore(null, 0, null, snapArray);
	}

	/**
	 * Gets the metric score.
	 *
	 * @return the metric score
	 */
	public double getMetricScore() {
		return metricScore;
	}

	/**
	 * Gets the reputation score.
	 *
	 * @return the reputation score
	 */
	public double getReputationScore() {
		return reputationScore;
	}

	/**
	 * Prints the results.
	 *
	 * @param outFormat the output format
	 * @param outFolderName the output folder
	 * @param expTag the experiment tag
	 */
	public void printResults(String outFormat, String outFolderName, String expTag) {
		alg.printResults(outFormat, outFolderName, expTag);
	}

	/**
	 * Gets the indicator layer type.
	 *
	 * @return the layer type
	 */
	public LayerType getLayerType() {
		if(alg.getDataSeries() != null)
			return alg.getDataSeries().getLayerType();
		else return LayerType.NO_LAYER;
	}

	/**
	 * Gets the algorithm type.
	 *
	 * @return the algorithm type
	 */
	public LearnerType getAlgorithmType() {
		return alg.getLearnerType();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return alg.toString();
	}

	public DataSeries getDataSeries() {
		return alg.getDataSeries();
	}

	public BasicConfiguration getAlgorithmConfiguration() {
		return alg.getConfiguration();
	}
	
	public boolean usesSeries(DataSeries serie) {
		return alg.usesSeries(serie);
	}

	@Override
	public int compareTo(AlgorithmModel other) {
		if(getAlgorithmType().equals(other.getAlgorithmType()) && getDataSeries().equals(other.getDataSeries()))
			return 0;
		else return -1;
	}

	public static AlgorithmModel fromString(String amString) {
		if(amString != null && amString.length() > 0 && amString.indexOf("§") != -1){
			String[] splitted = AppUtility.splitAndPurify(amString, "§");
			if(splitted.length > 5){
				BasicConfiguration conf = BasicConfiguration.buildConfiguration(LearnerType.fromString(splitted[1]), (splitted.length > 6 ? splitted[6] : null));
				if(conf != null){
					conf.addItem(BasicConfiguration.WEIGHT, splitted[2]);
					conf.addItem(BasicConfiguration.AVG_SCORE, splitted[3]);
					conf.addItem(BasicConfiguration.STD_SCORE, splitted[4]);
					conf.addItem(BasicConfiguration.DATASET_NAME, splitted[5]);
				}
				// TODO
				return new AlgorithmModel(DetectionAlgorithm.buildAlgorithm(conf.getLearnerType(), DataSeries.fromString(splitted[0], true), conf), Double.parseDouble(splitted[3]), Double.parseDouble(splitted[2]));
			}
		}
		return null;
	}
	
	public static List<AlgorithmModel> fromFile(String fileString){
		return fromFile(fileString, true);
	}
	
	public static List<AlgorithmModel> fromFile(String fileString, boolean alertFlag) {
		File asFile = new File(fileString);
		BufferedReader reader;
		LinkedList<AlgorithmModel> modelList = new LinkedList<AlgorithmModel>();
		String readed;
		try {
			if(asFile.exists()){
				reader = new BufferedReader(new FileReader(asFile));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						AlgorithmModel am = AlgorithmModel.fromString(readed);
						if(am != null)
							modelList.add(am);
					}
				}
				reader.close();
			} else if(alertFlag)
				AppLogger.logError(AlgorithmModel.class, "FileNotFound", "Unable to find '" + fileString + "'");
		} catch(Exception ex){
			AppLogger.logException(AlgorithmModel.class, ex, "Unable to read scores");
		}
		Collections.sort(modelList);
		return modelList;
	}

	public DetectionAlgorithm getAlgorithm() {
		return alg;
	}
	
	public static boolean trainResultExists(String scoresFile) {
		List<AlgorithmModel> algVoters = AlgorithmModel.fromFile(scoresFile, false);
		if(algVoters != null && algVoters.size() > 0)
			return true;
		else return false;
	}

}
