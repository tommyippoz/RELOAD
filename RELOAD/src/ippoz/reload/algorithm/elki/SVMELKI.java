/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.elki.support.CustomSVM;
import ippoz.reload.algorithm.elki.support.CustomSVM.SVMKernel;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.utils.ObjectPair;

import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class SVMELKI. Wrapper for One-Class Support Vector Machines from ELKI.
 *
 * @author Tommy
 */
public class SVMELKI extends DataSeriesELKIAlgorithm {
	
	/** The Constant NU. */
	private static final String NU = "nu";
	
	/** The Constant KERNEL. */
	private static final String KERNEL = "kernel";
	
	/**
	 * Instantiates a new svmelki.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public SVMELKI(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, false, true);
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.elki.DataSeriesELKIAlgorithm#generateELKIAlgorithm()
	 */
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		SVMKernel kernel = getKernel(conf);
		return new CustomSVM(kernel != null ? kernel : SVMKernel.LINEAR, getNU(conf));
	}

	/**
	 * Gets the nu parameter of SVM.
	 *
	 * @param conf the configuration
	 * @return the nu
	 */
	private double getNU(BasicConfiguration conf) {
		if(conf.hasItem(NU)){
			if(conf.getItem(NU).trim().length() > 0){
				try {
					return Double.valueOf(conf.getItem(NU).trim());
				} catch(Exception ex){
					return 0.05;
				}
			} else return 0.05;
		}return 0.05;
	}

	/**
	 * Gets the kernel.
	 *
	 * @param conf the configuration
	 * @return the kernel
	 */
	private SVMKernel getKernel(BasicConfiguration conf) {
		if(conf.hasItem(KERNEL)){
			if(conf.getItem(KERNEL).trim().length() > 0){
				try {
					return SVMKernel.valueOf(conf.getItem(KERNEL).trim());
				} catch(Exception ex){
					return null;
				}
			} else return null;
		} else return null;
	}
	
	@Override
	public ObjectPair<Double, Object> getELKIScore(Vector v) {
		return new ObjectPair<Double, Object>(((CustomSVM)getAlgorithm()).calculateSVM(v), null);
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
		defPar.put("kernel", new String[]{"LINEAR", "QUADRATIC", "CUBIC", "RBF"});
		defPar.put("nu", new String[]{"0.02", "0.1", "0.2"});
		//defPar.put("threshold", new String[]{"0.9", "IQR", "RIGHT_IQR"});
		return defPar;
	}

}

