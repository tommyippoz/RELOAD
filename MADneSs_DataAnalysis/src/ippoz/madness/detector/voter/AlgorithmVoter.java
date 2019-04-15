/**
 * 
 */
package ippoz.madness.detector.voter;

import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;

/**
 * The Class AlgorithmVoter.
 * Used to score a specific pre-instantiated algorithm.
 *
 * @author Tommy
 */
public class AlgorithmVoter implements Cloneable {
	
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
	public AlgorithmVoter(DetectionAlgorithm alg, double metricScore, double reputationScore) {
		this.alg = alg;
		this.metricScore = metricScore;
		this.reputationScore = reputationScore;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected AlgorithmVoter clone() throws CloneNotSupportedException {
		return new AlgorithmVoter(DetectionAlgorithm.buildAlgorithm(alg.getAlgorithmType(), alg.getDataSeries(), alg.getConfiguration()), metricScore, reputationScore);
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
		return alg.getAlgorithmType();
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

	public AlgorithmConfiguration getAlgorithmConfiguration() {
		return alg.getConfiguration();
	}
	
	public boolean usesSeries(DataSeries serie) {
		return alg.usesSeries(serie);
	}
	
	public double getAUC(){
		if(alg.getConfiguration() != null && alg.getConfiguration().hasItem(AlgorithmConfiguration.AUC_SCORE))
			return Double.valueOf(alg.getConfiguration().getItem(AlgorithmConfiguration.AUC_SCORE));
		else return -1.0;
	}

}
