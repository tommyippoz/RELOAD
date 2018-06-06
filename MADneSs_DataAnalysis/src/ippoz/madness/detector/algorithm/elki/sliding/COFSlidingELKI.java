/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.sliding;

import ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm;
import ippoz.madness.detector.algorithm.elki.support.CustomCOF;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
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
	
	/** The Constant THRESHOLD. */
	private static final String THRESHOLD = "threshold";
	
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
	 * Parses the threshold.
	 *
	 * @param cCOF the calculated COF
	 * @return the threshold
	 */
	private double parseThreshold(CustomCOF cCOF) {
		double ratio;
		if(conf != null && conf.hasItem(THRESHOLD)){
			if(AppUtility.isNumber(conf.getItem(THRESHOLD))){
				ratio = Double.parseDouble(conf.getItem(THRESHOLD));
				if(ratio <= 1)
					ratio = ratio * cCOF.size();
				return cCOF.getScore((int) ratio);
			}
			else return -1;
		} else return cCOF.getScore(cCOF.size()-1);
	}
	
	/**
	 * Gets the k, starting from the preference and applying default values when needed.
	 *
	 * @param dbSize the db size
	 * @return the k
	 */
	private int getK(int dbSize){
		int prefK;
		if(conf.hasItem(K) && AppUtility.isInteger(conf.getItem(K))){
			prefK = Integer.parseInt(conf.getItem(K));
			if(prefK < dbSize)
				return prefK;
			else return dbSize;
		} else {
			if(DEFAULT_K < dbSize)
				return DEFAULT_K;
			else return dbSize;
		} 
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm#evaluateSlidingELKISnapshot(ippoz.madness.detector.commons.knowledge.SlidingKnowledge, de.lmu.ifi.dbs.elki.database.Database, de.lmu.ifi.dbs.elki.math.linearalgebra.Vector)
	 */
	@Override
	protected double evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance) {
		double threshold;
		CustomCOF cCOF;
		int windowSize = windowDb.getRelation(TypeUtil.NUMBER_VECTOR_FIELD).getDBIDs().size();
		try {
			if(windowSize >= 5){
				cCOF = new CustomCOF(getK(windowSize), SquaredEuclideanDistanceFunction.STATIC);
				cCOF.run(windowDb, windowDb.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
				threshold = parseThreshold(cCOF);
				if(newInstance.getDimensionality() > 0 && Double.isFinite(newInstance.doubleValue(0))){
					double of = cCOF.calculateSingleOF(newInstance);
					if(of >= threshold)
						return 1.0;
					else return 0.0;
				} else return 0.0;
			} else return -1.0;
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate COF window");
		}
		return 0.0;
	}

}
