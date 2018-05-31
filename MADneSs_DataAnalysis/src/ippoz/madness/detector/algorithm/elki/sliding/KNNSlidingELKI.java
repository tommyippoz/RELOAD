/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.sliding;

import ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm;
import ippoz.madness.detector.algorithm.elki.support.CustomKNN;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.support.AppUtility;

import java.util.List;

import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
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

	/** The Constant THRESHOLD. */
	private static final String THRESHOLD = "threshold";
	
	/** The Constant K. */
	private static final String K = "k";
	
	/** The anomaly threshold. */
	private double threshold;
	
	/**
	 * Instantiates a new KNN sliding elki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public KNNSlidingELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false);
		threshold = parseThreshold(conf);
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm#evaluateSlidingELKISnapshot(ippoz.madness.detector.commons.knowledge.SlidingKnowledge, de.lmu.ifi.dbs.elki.database.Database, de.lmu.ifi.dbs.elki.math.linearalgebra.Vector)
	 */
	@Override
	protected double evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance) {
		int dbSize = windowDb.getRelation(TypeUtil.NUMBER_VECTOR_FIELD).getDBIDs().size();
		CustomKNN knn = new CustomKNN(HellingerDistanceFunction.STATIC, getK(dbSize));
		List<Double> allDistances = knn.run(windowDb.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
		if(newInstance.getDimensionality() > 0 && Double.isFinite(newInstance.doubleValue(0))){
			if(knn.calculateKNN(newInstance, windowDb) >= allDistances.get((int)(threshold*dbSize)))
				return 1.0;
			else return 0.0;
		} else return 0.0;
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
