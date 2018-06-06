/**
 * 
 */
package ippoz.madness.detector.algorithm.elki.sliding;

import ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm;
import ippoz.madness.detector.algorithm.elki.support.CustomKMeans;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;

import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;

/**
 * The Class KMeansSlidingELKI. Represents a sliding version of the KMeans clustering.
 *
 * @author Tommy
 */
public class KMeansSlidingELKI extends DataSeriesSlidingELKIAlgorithm {

	/** The Constant K. */
	private static final String K = "k";
	
	/** The Constant THRESHOLD. */
	private static final String THRESHOLD = "threshold";
	
	/** The Constant DEFAULT_K. */
	private static final Integer DEFAULT_K = 3;
	
	/** The Constant MIN_AVG_ITEMS. */
	private static final Integer MIN_AVG_ITEMS = 3;

	/**
	 * Instantiates a new k means sliding elki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public KMeansSlidingELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false);
	}
	
	/**
	 * Evaluate treshold.
	 *
	 * @param clusters the calculated clusters
	 * @param snapVector the vector extracted from the snapshot
	 * @return true, if anomalous
	 */
	private boolean evaluateTreshold(List<KMeansModel> clusters, Vector snapVector) {
		String prefValue;
		double partial;
		double minValue = Double.MAX_VALUE;
		KMeansModel minKmm = null;
		
		if(clusters != null && clusters.size() > 0){
			for(KMeansModel kmm : clusters){
				partial = Math.abs(SquaredEuclideanDistanceFunction.STATIC.minDist(snapVector, kmm.getMean()));
				if(partial < minValue){
					minValue = partial;
					minKmm = kmm;
				}
			}
		} else return false;
		
		if(minKmm == null)
			return false;
		
		if(!conf.hasItem(THRESHOLD))
			return false;
		else {
			prefValue = conf.getItem(THRESHOLD);
			if(AppUtility.isNumber(prefValue))
				return minValue <= Math.abs(Double.parseDouble(prefValue));
			else {
				if(prefValue.contains("VAR")){
					prefValue = prefValue.replace("VAR", "");
					if(prefValue.trim().length() == 0){
						return minValue > minKmm.getVarianceContribution();
					} else if(AppUtility.isNumber(prefValue.trim())){
						return minValue > Double.parseDouble(prefValue.trim())*minKmm.getVarianceContribution();
					} else {
						AppLogger.logError(getClass(), "InputError", "Unable to process '" + prefValue + "' threshold");
						return false;
					}
				} else if(prefValue.contains("STD")){
					prefValue = prefValue.replace("STD", "");
					if(prefValue.trim().length() == 0){
						return minValue > Math.sqrt(Math.abs(minKmm.getVarianceContribution()));
					} else if(AppUtility.isNumber(prefValue.trim())){
						return minValue > Double.parseDouble(prefValue.trim())*Math.sqrt(Math.abs(minKmm.getVarianceContribution()));
					} else {
						AppLogger.logError(getClass(), "InputError", "Unable to process '" + prefValue + "' threshold");
						return false;
					}
				} else {
					AppLogger.logError(getClass(), "InputError", "Unable to process '" + prefValue + "' threshold");
					return false;
				}
			}
		}
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
			if(prefK*MIN_AVG_ITEMS < dbSize)
				return prefK;
			else return (int)(dbSize/MIN_AVG_ITEMS);
		} else {
			if(DEFAULT_K*MIN_AVG_ITEMS < dbSize)
				return DEFAULT_K;
			else return (int)(dbSize/MIN_AVG_ITEMS);
		} 
	}

	/* (non-Javadoc)
	 * @see ippoz.madness.detector.algorithm.elki.DataSeriesSlidingELKIAlgorithm#evaluateSlidingELKISnapshot(ippoz.madness.detector.commons.knowledge.SlidingKnowledge, de.lmu.ifi.dbs.elki.database.Database, de.lmu.ifi.dbs.elki.math.linearalgebra.Vector)
	 */
	@Override
	protected double evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance) {
		try {
			CustomKMeans<NumberVector> km = new CustomKMeans<>(SquaredEuclideanDistanceFunction.STATIC, 
		    		getK(windowDb.getRelation(TypeUtil.NUMBER_VECTOR_FIELD).getDBIDs().size()), 
		    		0, 
		    		new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT), 
		    		null);
		    km.run(windowDb);
		    conf.addRawItem(K, km.getClusters().size());
		    return evaluateTreshold(km.getClusters(), newInstance) ? 1.0 : 0.0;
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate K-Means window");
		}
		return 0.0;
	}
	
}
