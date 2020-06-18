/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.elki.support.CustomISOS;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.utils.ObjectPair;

import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.math.statistics.intrinsicdimensionality.HillEstimator;

/**
 * @author Tommy
 *
 */
public class ISOSELKI extends DataSeriesELKIAlgorithm {
	
	/** The Constant K. */
	private static final String K = "k";
	
	/** The Constant DEFAULT_K. */
	private static final Integer DEFAULT_K = 20;	
	
	/** The Constant K. */
	private static final String PHI = "phi";
	
	/** The Constant DEFAULT_K. */
	private static final Double DEFAULT_PHI = 0.1;	
	
	/**
	 * Instantiates a new soselki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public ISOSELKI(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, false, false);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<NumberVector> generateELKIAlgorithm() {
		return new CustomISOS(SquaredEuclideanDistanceFunction.STATIC, 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K, 
	    				conf.hasItem(PHI) ? Double.parseDouble(conf.getItem(PHI)) : DEFAULT_PHI,
	    						HillEstimator.STATIC);
	}
	
	@Override
	public ObjectPair<Double, Object> getELKIScore(Vector v) {
		return new ObjectPair<Double, Object>(((CustomISOS)getAlgorithm()).calculateSingleISOS(v), null);
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
		//defPar.put("threshold", new String[]{"IQR", "IQR(0)"});
		defPar.put("k", new String[]{"5", "10", "20", "50"});
		defPar.put("phi", new String[]{"0.1", "0.2", "0.5"});
		return defPar;
	}

}

