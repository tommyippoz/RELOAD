/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.elki.support.CustomKNN;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.utils.ObjectPair;

import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class KNNELKI. Wrapper for the k-th Nearest Neighbour algorithm from ELKI.
 * Note that this version is completely unsupervised, relying only on the distance to the k-th neighbour
 * rather than its label.
 *
 * @author Tommy
 */
public class KNNELKI extends DataSeriesELKIAlgorithm {
	
	/** The Constant K. */
	private static final String K = "k";
	
	/** The Constant DEFAULT_K. */
	private static final Integer DEFAULT_K = 5;

	/**
	 * Instantiates a new knnelki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public KNNELKI(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, false, true);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomKNN(SquaredEuclideanDistanceFunction.STATIC, 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K);
	}
	
	@Override
	public ObjectPair<Double, Object> getELKIScore(Vector v) {
		return new ObjectPair<Double, Object>(((CustomKNN)getAlgorithm()).calculateSingleKNN(v), null);
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#storeAdditionalPreferences()
	 */
	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		//defPar.put("threshold", new String[]{"LEFT_POSITIVE_CONFIDENCE_INTERVAL", "LEFT_POSITIVE_IQR", "LEFT_POSITIVE_IQR(0.1)"});
		defPar.put("k", new String[]{"5", "10", "20", "50"});
		return defPar;
	}

}
