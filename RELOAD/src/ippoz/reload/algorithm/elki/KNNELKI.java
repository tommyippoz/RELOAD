/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.elki.support.CustomKNN;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class KNNELKI. Wrapper for the k-th Nearest Neighbour algorithm from ELKI.
 * Note that this version is completely unsupervised, relying only on the distance to the k-th neighbour
 * rather than its label.
 *
 * @author Tommy
 */
public class KNNELKI extends DataSeriesELKIAlgorithm {
	
	/** The Constant K. */
	private static final String K = "k";
	
	/** The Constant DEFAULT_K. */
	private static final Integer DEFAULT_K = 5;

	/**
	 * Instantiates a new knnelki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public KNNELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, true);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomKNN(SquaredEuclideanDistanceFunction.STATIC, 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#evaluateElkiSnapshot(ippoz.reload.commons.knowledge.snapshot.Snapshot)
	 */
	@Override
	protected AlgorithmResult evaluateElkiSnapshot(Snapshot sysSnapshot) {
		AlgorithmResult ar;
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0))){
			ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), ((CustomKNN)getAlgorithm()).calculateSingleKNN(v));
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} else return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#storeAdditionalPreferences()
	 */
	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}

}
