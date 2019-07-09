/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.elki.support.CustomLOF;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class LOFELKI. Wrapper for the Local Outlier Factor algorithm from ELKI.
 *
 * @author Tommy
 */
public class LOFELKI extends DataSeriesELKIAlgorithm {
	
	/** The Constant K. */
	private static final String K = "k";
	
	/** The Constant DEFAULT_K. */
	private static final Integer DEFAULT_K = 5;	
	
	/**
	 * Instantiates a new lofelki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public LOFELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<NumberVector> generateELKIAlgorithm() {
		return new CustomLOF( 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K,
	    		SquaredEuclideanDistanceFunction.STATIC);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#evaluateElkiSnapshot(ippoz.reload.commons.knowledge.snapshot.Snapshot)
	 */
	@Override
	protected AlgorithmResult evaluateElkiSnapshot(Snapshot sysSnapshot) {
		AlgorithmResult ar;
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0)) && getDecisionFunction() != null){
			ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), ((CustomLOF)getAlgorithm()).calculateSingleOF(v));
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