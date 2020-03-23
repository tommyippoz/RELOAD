/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.elki.support.CustomKMeans;
import ippoz.reload.algorithm.elki.support.CustomKMeans.KMeansScore;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.result.KMeansResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;

import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;

/**
 * The Class KMeansELKI. Wrapper for the K-Means (KMeansLloyd) algorithm from ELKI.
 *
 * @author Tommy
 */
public class KMeansELKI extends DataSeriesELKIAlgorithm {

	/** The Constant K. */
	public static final String K = "k";
	
	/** The Constant DEFAULT_K. */
	public static final int DEFAULT_K = 3;
	
	/**
	 * Instantiates a new k means elki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public KMeansELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
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
		return new CustomKMeans<>(SquaredEuclideanDistanceFunction.STATIC, 
	    		(conf != null && conf.hasItem(K)) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K, 
	    		0, 
	    		new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT), 
	    		null);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#evaluateElkiSnapshot(ippoz.reload.commons.knowledge.snapshot.Snapshot)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		AlgorithmResult ar;
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0))){
			KMeansScore of = ((CustomKMeans<NumberVector>)getAlgorithm()).getMinimumClustersDistance(v);
			ar = new KMeansResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), of, getConfidence(of.getDistance()));
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} else return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
	}
	
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		//defPar.put("threshold", new String[]{"CLUSTER(STD)", "CLUSTER(0.1STD)", "CLUSTER(0.5STD)", "CLUSTER(VAR)"});
		defPar.put("k", new String[]{"2", "5", "10"});
		return defPar;
	}

	@Override
	public double getELKIScore(Vector v) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
