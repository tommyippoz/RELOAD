/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.elki.support.CustomODIN;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;

import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class ODINELKI. Wrapper for the Outlier Detection using Indigree Number algorithm from ELKI.
 *
 * @author Tommy
 */
public class ODINELKI extends DataSeriesELKIAlgorithm {
	
	/** The Constant K. */
	private static final String K = "k";
	
	/** The Constant DEFAULT_K. */
	private static final Integer DEFAULT_K = 5;

	/**
	 * Instantiates a new odinelki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public ODINELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomODIN(SquaredEuclideanDistanceFunction.STATIC, 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K);
	}
	
	@Override
	public double getELKIScore(Vector v) {
		return ((CustomODIN)getAlgorithm()).calculateSingleODIN(v);
	}

	@Override
	public boolean getELKIEvaluationFlag(Vector v) {
		return v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0));
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
		//defPar.put("threshold", new String[]{"LEFT_POSITIVE_CONFIDENCE_INTERVAL", "LEFT_POSITIVE_CONFIDENCE_INTERVAL(0.5)", "LEFT_POSITIVE_IQR", "LEFT_POSITIVE_IQR(0)"});
		defPar.put("k", new String[]{"5", "10", "20", "50"});
		return defPar;
	}

}
