/**
 * 
 */
package ippoz.madness.detector.algorithm.elki;

import ippoz.madness.detector.algorithm.elki.support.CustomLOF;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.decisionfunction.AnomalyResult;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class LOFELKI extends DataSeriesELKIAlgorithm {
	
	private static final String K = "k";
	
	private static final Integer DEFAULT_K = 5;	
	
	public LOFELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
	}
	
	@Override
	protected ELKIAlgorithm<NumberVector> generateELKIAlgorithm() {
		return new CustomLOF( 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K,
	    		SquaredEuclideanDistanceFunction.STATIC);
	}

	@Override
	protected AnomalyResult evaluateElkiSnapshot(Snapshot sysSnapshot) {
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0)) && getClassifier() != null){
			double of = ((CustomLOF)getAlgorithm()).calculateSingleOF(v);
			return getClassifier().classify(of);
		} else return AnomalyResult.UNKNOWN;
	}

	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}
	
}