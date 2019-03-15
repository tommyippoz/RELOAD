/**
 * 
 */
package ippoz.madness.detector.algorithm.elki;

import ippoz.madness.detector.algorithm.elki.support.CustomABOD;
import ippoz.madness.detector.algorithm.result.AlgorithmResult;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.HellingerDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class ABODELKI extends DataSeriesELKIAlgorithm {
	
	public ABODELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
	}
	
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomABOD<NumberVector>(HellingerDistanceFunction.STATIC);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AlgorithmResult evaluateElkiSnapshot(Snapshot sysSnapshot) {
		AlgorithmResult ar;
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0))){
			ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), ((CustomABOD<NumberVector>)getAlgorithm()).calculateSingleABOF(v));
			getDecisionFunction().classifyScore(ar);
			return ar;
		} else return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
	}

	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}

}

