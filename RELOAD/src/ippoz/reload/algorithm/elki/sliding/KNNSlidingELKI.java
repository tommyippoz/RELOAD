/**
 * 
 */
package ippoz.reload.algorithm.elki.sliding;

import ippoz.reload.algorithm.elki.DataSeriesSlidingELKIAlgorithm;
import ippoz.reload.algorithm.elki.ELKIAlgorithm;
import ippoz.reload.algorithm.elki.support.CustomKNN;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppUtility;
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
	protected AlgorithmResult evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance, Snapshot dsSnapshot) {
		AlgorithmResult ar;
		if(newInstance.getDimensionality() > 0 && Double.isFinite(newInstance.doubleValue(0))){
			ar = new AlgorithmResult(dsSnapshot.listValues(true), dsSnapshot.getInjectedElement(), ((CustomKNN) getAlgorithm()).calculateKNN(newInstance, windowDb));
			getDecisionFunction().classifyScore(ar);
			return ar;
		} else return AlgorithmResult.unknown(dsSnapshot.listValues(true), dsSnapshot.getInjectedElement());
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