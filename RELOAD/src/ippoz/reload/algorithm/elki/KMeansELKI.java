/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.elki.support.CustomKMeans;
import ippoz.reload.algorithm.elki.support.CustomKMeans.KMeansScore;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.result.ClusteringResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;

/**
 * @author Tommy
 *
 */
public class KMeansELKI extends DataSeriesELKIAlgorithm {

	public static final String K = "k";
	
	public static final int DEFAULT_K = 3;
	
	public KMeansELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
	}

	@Override
	protected void storeAdditionalPreferences() {
		// TODO
	}

	/*@Override
	protected void automaticElkiTraining(Database db, boolean createOutput) {
	    CustomKMeans<NumberVector> km = new CustomKMeans<>(SquaredEuclideanDistanceFunction.STATIC, 
	    		(conf != null && conf.hasItem(K)) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K, 
	    		0, 
	    		new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT), 
	    		null);
	    
	    km.run(db);
	    clusters = km.getClusters();
	    conf.addItem(KMEANS_CLUSTERS, km.clustersToString());
	}*/
	
	@Override
	protected ELKIAlgorithm<NumberVector> generateELKIAlgorithm() {
		return new CustomKMeans<>(SquaredEuclideanDistanceFunction.STATIC, 
	    		(conf != null && conf.hasItem(K)) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K, 
	    		0, 
	    		new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT), 
	    		null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected AlgorithmResult evaluateElkiSnapshot(Snapshot sysSnapshot) {
		AlgorithmResult ar;
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0))){
			KMeansScore of = ((CustomKMeans<NumberVector>)getAlgorithm()).getMinimumClustersDistance(v);
			ar = new ClusteringResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), of);
			getDecisionFunction().classifyScore(ar, true);
			return ar;
		} else return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
	}
	
	/*private boolean evaluateThreshold(Snapshot sysSnapshot){
		String prefValue;
		double partial;
		double minValue = Double.MAX_VALUE;
		KMeansModel minKmm = null;
		Vector snapVector = convertSnapToVector(sysSnapshot);
		
		if(clusters != null && clusters.size() > 0){
			for(KMeansModel kmm : clusters){
				partial = Math.abs(dist.minDist(snapVector, kmm.getMean()));
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
	}*/

}
