/**
 * 
 */
package ippoz.multilayer.detector.algorithm.elki;

import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import ippoz.multilayer.detector.algorithm.elki.support.CustomKMeans;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.dataseries.MultipleDataSeries;
import ippoz.multilayer.detector.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.multilayer.detector.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.multilayer.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.AppUtility;

/**
 * @author Tommy
 *
 */
public class KMeansELKI extends DataSeriesElkiAlgorithm {
	
	public static final String KMEANS_CLUSTERS = "clusters";

	public static final String K = "k";
	
	public static final String THRESHOLD = "threshold";
	
	public static final int DEFAULT_K = 3;
	
	private List<KMeansModel> clusters;
	
	private SquaredEuclideanDistanceFunction dist;
	
	public KMeansELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		dist = SquaredEuclideanDistanceFunction.STATIC;
		if(conf.hasItem(KMEANS_CLUSTERS))
			clusters = CustomKMeans.loadClusters(conf.getItem(KMEANS_CLUSTERS));
	}

	@Override
	protected void automaticElkiTraining(Database db) {
	    CustomKMeans<NumberVector> km = new CustomKMeans<>(dist, 
	    		(conf != null && conf.hasItem(K)) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K, 
	    		0, 
	    		new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT), 
	    		null);
	    
	    km.run(db);
	    clusters = km.getClusters();
	    conf.addItem(KMEANS_CLUSTERS, km.clustersToString());
	}
	
	@Override
	protected double evaluateElkiSnapshot(Snapshot sysSnapshot) {
		return evaluateThreshold(sysSnapshot) ? 1.0 : 0.0;
	}
	
	private boolean evaluateThreshold(Snapshot sysSnapshot){
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
	}

}
