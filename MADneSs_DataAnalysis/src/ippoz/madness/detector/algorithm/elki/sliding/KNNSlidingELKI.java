/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.sliding;

import ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm;
import ippoz.madness.detector.algorithm.elki.ELKIAlgorithm;
import ippoz.madness.detector.algorithm.elki.support.CustomKNN;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.decisionfunction.AnomalyResult;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.HellingerDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class KNNSlidingELKI. Represents a sliding version of the KNN neighbour-based algorithm.
 *
 * @author Tommy
 */
public class KNNSlidingELKI extends DataSeriesSlidingELKIAlgorithm {
	
	/** The Constant DEFAULT_K. */
	private static final Integer DEFAULT_K = 3;
	
	/** The Constant K. */
	private static final String K = "k";
	
	/**
	 * Instantiates a new KNN sliding elki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public KNNSlidingELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false);
	}
	
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomKNN(HellingerDistanceFunction.STATIC, getK());
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm#evaluateSlidingELKISnapshot(ippoz.madness.detector.commons.knowledge.SlidingKnowledge, de.lmu.ifi.dbs.elki.database.Database, de.lmu.ifi.dbs.elki.math.linearalgebra.Vector)
	 */
	@Override
	protected AnomalyResult evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance) {
		if(newInstance.getDimensionality() > 0 && Double.isFinite(newInstance.doubleValue(0))){
			double knnScore = ((CustomKNN) getAlgorithm()).calculateKNN(newInstance, windowDb);
			return getClassifier().classify(knnScore);
		} else return AnomalyResult.UNKNOWN;
	}
	
	/**
	 * Gets the k, starting from the preference and applying default values when needed.
	 *
	 * @return the k
	 */
	private int getK(){
		if(conf.hasItem(K) && AppUtility.isInteger(conf.getItem(K))){
			return Integer.parseInt(conf.getItem(K));
		} else return DEFAULT_K;
	}
	
}
