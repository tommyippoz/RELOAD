/**
 * 
 */
package ippoz.reload.evaluation;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.meta.DataSeriesMetaLearner;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.support.AppUtility;

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
		if(alg instanceof DataSeriesMetaLearner){
			DataSeriesMetaLearner dsml = (DataSeriesMetaLearner)alg;
			new AlgorithmModel(DetectionAlgorithm.buildMetaAlgorithm(dsml.getMetaType(), alg.getDataSeries(), alg.getConfiguration(), dsml.getMetaData()), metricScore, reputationScore);
		}
		return new AlgorithmModel(DetectionAlgorithm.buildBaseAlgorithm(alg.getLearnerType(), alg.getDataSeries(), alg.getConfiguration()), metricScore, reputationScore);
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
	public AlgorithmType getAlgorithmType() {
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
				BasicConfiguration conf = BasicConfiguration.buildConfiguration(AlgorithmType.valueOf(splitted[1]), (splitted.length > 6 ? splitted[6] : null));
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

}
