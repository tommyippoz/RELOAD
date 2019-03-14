/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.sliding;

import ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm;
import ippoz.madness.detector.algorithm.elki.ELKIAlgorithm;
import ippoz.madness.detector.algorithm.elki.support.CustomCOF;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.decisionfunction.AnomalyResult;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class COFSlidingELKI. Sliding version of the classic density-based COF.
 *
 * @author Tommy
 */
public class COFSlidingELKI extends DataSeriesSlidingELKIAlgorithm {
	
	/** The Constant K. */
	private static final String K = "k";
	
	/** The Constant DEFAULT_K. */
	private static final Integer DEFAULT_K = 5;

	/**
	 * Instantiates a new COF sliding elki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public COFSlidingELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false);
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

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm#evaluateSlidingELKISnapshot(ippoz.madness.detector.commons.knowledge.SlidingKnowledge, de.lmu.ifi.dbs.elki.database.Database, de.lmu.ifi.dbs.elki.math.linearalgebra.Vector)
	 */
	@Override
	protected AnomalyResult evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance) {
		if(newInstance.getDimensionality() > 0 && Double.isFinite(newInstance.doubleValue(0))){
			double of = ((CustomCOF) getAlgorithm()).calculateSingleOF(newInstance);
			return getClassifier().classify(of);
		} else return AnomalyResult.UNKNOWN;
	}

	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomCOF(getK(), SquaredEuclideanDistanceFunction.STATIC);
	}

}
