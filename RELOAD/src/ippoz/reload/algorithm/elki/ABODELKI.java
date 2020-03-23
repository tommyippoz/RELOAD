/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.elki.support.CustomABOD;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;

import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.HellingerDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class ABODELKI. Wrapper for the Angle-Based Outlier Detection algorithm from ELKI.
 *
 * @author Tommy
 */
public class ABODELKI extends DataSeriesELKIAlgorithm {
	
	/**
	 * Instantiates a new abodelki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public ABODELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		return new CustomABOD<NumberVector>(HellingerDistanceFunction.STATIC);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public double getELKIScore(Vector v) {
		return ((CustomABOD<NumberVector>)getAlgorithm()).calculateSingleABOF(v);
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
		//defPar.put("threshold", new String[]{"LEFT_IQR(1)", "LEFT_IQR(0.5)", "LEFT_CONFIDENCE_INTERVAL(1)", "LEFT_CONFIDENCE_INTERVAL(0.5)"});
		return defPar;
	}

}

