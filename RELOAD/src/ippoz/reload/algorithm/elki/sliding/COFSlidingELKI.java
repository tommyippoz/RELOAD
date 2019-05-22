/**
 * 
 */
package ippoz.reload.algorithm.elki.sliding;

import ippoz.reload.algorithm.elki.DataSeriesSlidingELKIAlgorithm;
import ippoz.reload.algorithm.elki.ELKIAlgorithm;
import ippoz.reload.algorithm.elki.support.CustomCOF;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppUtility;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

// TODO: Auto-generated Javadoc
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
	protected AlgorithmResult evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance, Snapshot dsSnapshot) {
		AlgorithmResult ar;
		if(newInstance.getDimensionality() > 0 && Double.isFinite(newInstance.doubleValue(0))){
			ar = new AlgorithmResult(dsSnapshot.listValues(true), dsSnapshot.getInjectedElement(), ((CustomCOF) getAlgorithm()).calculateSingleOF(newInstance));
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} else return AlgorithmResult.unknown(dsSnapshot.listValues(true), dsSnapshot.getInjectedElement());
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesSlidingELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomCOF(getK(), SquaredEuclideanDistanceFunction.STATIC);
	}

}
