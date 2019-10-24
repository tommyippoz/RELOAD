/**
 * 
 */
package ippoz.reload.algorithm.custom;

import ippoz.reload.algorithm.elki.support.CustomKMeans;
import ippoz.reload.algorithm.support.ClusterableSnapshot;
import ippoz.reload.algorithm.support.GenericCluster;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.externalutils.ELKIUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;

/**
 * @author Tommy
 *
 */
public class LDCOFKMeansDetectionAlgorithm extends LDCOFDetectionAlgorithm {
	
	/** The Constant K. */
	public static final String K = "k";
	
	/** The Constant DEFAULT_K. */
	public static final int DEFAULT_K = 3;

	public LDCOFKMeansDetectionAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
	}

	@Override
	protected List<GenericCluster> generateClusters(List<ClusterableSnapshot> clSnapList) {
		CustomKMeans<NumberVector> kMeans = new CustomKMeans<>(SquaredEuclideanDistanceFunction.STATIC, 
	    		(conf != null && conf.hasItem(K)) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K, 
	    		0, 
	    		new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT), 
	    		null);
		kMeans.run(ELKIUtils.createElkiDatabase(clSnapList));
		return kMeans.getClusters();
	}
	
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		defPar.put("threshold", new String[]{"RIGHT_CONFIDENCE_INTERVAL(0.1)", "RIGHT_CONFIDENCE_INTERVAL(0.5)", "RIGHT_IQR(0.1)", "RIGHT_IQR(0.5)"});
		defPar.put("k", new String[]{"5", "10", "20", "50"});
		defPar.put("gamma", new String[]{"0.3", "0.5"});
		return defPar;
	}

}
