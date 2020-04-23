/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.elki.support.CustomGMeans;
import ippoz.reload.algorithm.elki.support.CustomKMeans.KMeansScore;
import ippoz.reload.commons.dataseries.DataSeries;

import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;

/**
 * @author Tommy
 *
 */
public class GMeansELKI extends DataSeriesELKIAlgorithm {

	/** The Constant DEFAULT_K. */
	public static final int MAX_K = 20;
	
	/**
	 * Instantiates a new k means elki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public GMeansELKI(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, false, false);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#storeAdditionalPreferences()
	 */
	@Override
	protected void storeAdditionalPreferences() {
		// TODO
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<NumberVector> generateELKIAlgorithm() {
		return new CustomGMeans<>(SquaredEuclideanDistanceFunction.STATIC, 
	    		MAX_K, 0, new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT), 
	    		null);
	}
	
	@Override
	public Pair<Double, Object> getELKIScore(Vector v) {
		KMeansScore of = ((CustomGMeans<NumberVector>)getAlgorithm()).getMinimumClustersDistance(v);
		return new Pair<Double, Object>(of.getDistance(), of.getCluster());
	}
	
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		//defPar.put("threshold", new String[]{"CLUSTER(STD)", "CLUSTER(0.1STD)", "CLUSTER(0.5STD)", "CLUSTER(VAR)"});
		return defPar;
	}

}
