/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.elki.support.CustomFastABOD;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.HellingerDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class FastABODELKI extends DataSeriesELKIAlgorithm {
	
	private static final String K = "k";
	
	private static final Integer DEFAULT_K = 5;
	
	public FastABODELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
	}
	
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomFastABOD<NumberVector>(
				HellingerDistanceFunction.STATIC, 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AlgorithmResult evaluateElkiSnapshot(Snapshot sysSnapshot) {
		AlgorithmResult ar;
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0))){
			ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), ((CustomFastABOD<NumberVector>)getAlgorithm()).calculateSingleABOF(v));
			getDecisionFunction().classifyScore(ar, true);
			return ar;
		} else return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
	}

	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}

}
