/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.sliding;

import ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm;
import ippoz.madness.detector.algorithm.elki.support.CustomABOD;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.support.AppUtility;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.HellingerDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class ABODSlidingELKI.
 *
 * @author Tommy
 */
public class ABODSlidingELKI extends DataSeriesSlidingELKIAlgorithm {

	/** The Constant THRESHOLD. */
	private static final String THRESHOLD = "threshold";
	
	/** The anomaly threshold. */
	private double threshold;
	
	/**
	 * Instantiates a new ABOD sliding elki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public ABODSlidingELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false);
		threshold = parseThreshold(conf);
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm#evaluateSlidingELKISnapshot(ippoz.madness.detector.commons.knowledge.SlidingKnowledge, de.lmu.ifi.dbs.elki.database.Database, de.lmu.ifi.dbs.elki.math.linearalgebra.Vector)
	 */
	@Override
	protected double evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance) {
		CustomABOD<NumberVector> abod = new CustomABOD<NumberVector>(HellingerDistanceFunction.STATIC);
		abod.run(windowDb, windowDb.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
		if(newInstance.getDimensionality() > 0 && Double.isFinite(newInstance.doubleValue(0))){
			if(abod.rankSingleABOF(newInstance) >= threshold*sKnowledge.size())
				return 1.0;
			else return 0.0;
		} else return 0.0;
	}

	/**
	 * Parses the threshold.
	 *
	 * @param conf the configuration
	 * @return the threshold
	 */
	private double parseThreshold(AlgorithmConfiguration conf) {
		if(conf != null && conf.hasItem(THRESHOLD)){
			if(AppUtility.isNumber(conf.getItem(THRESHOLD)))
				return Double.parseDouble(conf.getItem(THRESHOLD));
			else return -1;
		} else return -1;
	}

}
