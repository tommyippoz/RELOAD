/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.elki.support.CustomSVM;
import ippoz.reload.algorithm.elki.support.CustomSVM.SVMKernel;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class SVMELKI extends DataSeriesELKIAlgorithm {
	
	private static final String NU = "nu";
	
	private static final String KERNEL = "kernel";
	
	public SVMELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, true);
	}
	
	@Override
	protected ELKIAlgorithm<?> generateELKIAlgorithm() {
		SVMKernel kernel = getKernel(conf);
		return new CustomSVM(kernel != null ? kernel : SVMKernel.LINEAR, getNU(conf));
	}

	private double getNU(AlgorithmConfiguration conf) {
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

	private SVMKernel getKernel(AlgorithmConfiguration conf) {
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
	protected AlgorithmResult evaluateElkiSnapshot(Snapshot sysSnapshot) {
		AlgorithmResult ar;
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0))){
			ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), ((CustomSVM)getAlgorithm()).calculateSVM(v));
			getDecisionFunction().classifyScore(ar, true);
			return ar;
		} else return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
	}

	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}

}

