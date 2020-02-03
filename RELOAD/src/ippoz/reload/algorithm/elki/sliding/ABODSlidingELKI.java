/**
 * 
 */
package ippoz.reload.algorithm.elki.sliding;

import ippoz.reload.algorithm.elki.DataSeriesSlidingELKIAlgorithm;
import ippoz.reload.algorithm.elki.ELKIAlgorithm;
import ippoz.reload.algorithm.elki.support.CustomABOD;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;

import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.HellingerDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class ABODSlidingELKI. Sliding version of the classic angle-based ABOD.
 *
 * @author Tommy
 */
public class ABODSlidingELKI extends DataSeriesSlidingELKIAlgorithm {
	
	/**
	 * Instantiates a new ABOD sliding ELKI.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public ABODSlidingELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false);
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm#evaluateSlidingELKISnapshot(ippoz.madness.detector.commons.knowledge.SlidingKnowledge, de.lmu.ifi.dbs.elki.database.Database, de.lmu.ifi.dbs.elki.math.linearalgebra.Vector)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected AlgorithmResult evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance, Snapshot dsSnapshot) {
		AlgorithmResult ar;
		if(newInstance.getDimensionality() > 0 && Double.isFinite(newInstance.doubleValue(0))){
			ar = new AlgorithmResult(dsSnapshot.listValues(true), dsSnapshot.getInjectedElement(), ((CustomABOD<NumberVector>) getAlgorithm()).calculateSingleABOF(newInstance));
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} else return AlgorithmResult.unknown(dsSnapshot.listValues(true), dsSnapshot.getInjectedElement());
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesSlidingELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomABOD<NumberVector>(HellingerDistanceFunction.STATIC);
	}
	
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		//defPar.put("threshold", new String[]{"LEFT_IQR(1)", "LEFT_IQR(0.5)", "LEFT_CONFIDENCE_INTERVAL(1)", "LEFT_CONFIDENCE_INTERVAL(0.5)"});
		return defPar;
	}

}
