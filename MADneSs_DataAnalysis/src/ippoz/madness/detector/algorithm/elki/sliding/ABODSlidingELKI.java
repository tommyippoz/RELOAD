/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.sliding;

import ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm;
import ippoz.madness.detector.algorithm.elki.ELKIAlgorithm;
import ippoz.madness.detector.algorithm.elki.support.CustomABOD;
import ippoz.madness.detector.algorithm.result.AlgorithmResult;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.HellingerDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class ABODSlidingELKI.
 *
 * @author Tommy
 */
public class ABODSlidingELKI extends DataSeriesSlidingELKIAlgorithm {
	
	/**
	 * Instantiates a new ABOD sliding elki.
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
			ar = new AlgorithmResult(dsSnapshot.listValues(true), dsSnapshot.getInjectedElement(), ((CustomABOD<NumberVector>) getAlgorithm()).rankSingleABOF(newInstance));
			getDecisionFunction().classifyScore(ar);
			return ar;
		} else return AlgorithmResult.unknown(dsSnapshot.listValues(true), dsSnapshot.getInjectedElement());
	}

	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomABOD<NumberVector>(HellingerDistanceFunction.STATIC);
	}

}
