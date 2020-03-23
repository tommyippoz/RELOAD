/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.elki.support.CustomFastABOD;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;

import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.HellingerDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class FastABODELKI. Wrapper for the Fast Angle-Based Outlier Detection algorithm from ELKI.
 *
 * @author Tommy
 */
public class FastABODELKI extends DataSeriesELKIAlgorithm {
	
	/** The Constant K. */
	private static final String K = "k";
	
	/** The Constant DEFAULT_K. */
	private static final Integer DEFAULT_K = 5;
	
	/**
	 * Instantiates a new fast abodelki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public FastABODELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomFastABOD<NumberVector>(
				HellingerDistanceFunction.STATIC, 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public double getELKIScore(Vector v) {
		return Math.sqrt(((CustomFastABOD<NumberVector>)getAlgorithm()).calculateSingleABOF(v));
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
		//defPar.put("threshold", new String[]{"LEFT_POSITIVE_CONFIDENCE_INTERVAL", "LEFT_POSITIVE_IQR"});
		defPar.put("k", new String[]{"5", "10", "20", "50"});
		return defPar;
	}
}
