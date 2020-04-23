/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.elki.support.CustomSOS;
import ippoz.reload.commons.dataseries.DataSeries;

import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class SOSELKI extends DataSeriesELKIAlgorithm {
	
	/** The Constant K. */
	private static final String H = "h";
	
	/** The Constant DEFAULT_K. */
	private static final Integer DEFAULT_H = 5;	
	
	/**
	 * Instantiates a new soselki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public SOSELKI(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, false, false);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<NumberVector> generateELKIAlgorithm() {
		return new CustomSOS(SquaredEuclideanDistanceFunction.STATIC, 
	    		conf.hasItem(H) ? Integer.parseInt(conf.getItem(H)) : DEFAULT_H);
	}
	
	@Override
	public Pair<Double, Object> getELKIScore(Vector v) {
		return new Pair<Double, Object>(((CustomSOS)getAlgorithm()).calculateSingleSOS(v), null);
	}

	@Override
	public boolean getELKIEvaluationFlag(Vector v) {
		return v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0)) && getDecisionFunction() != null;
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
		defPar.put("h", new String[]{"5", "10", "20", "50"});
		return defPar;
	}

}
