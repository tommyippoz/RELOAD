/**
 * 
 */
package ippoz.multilayer.detector.algorithm;

import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.MultipleSnapshot;
import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.invariants.Invariant;

/**
 * @author Tommy
 *
 */
public class InvariantChecker extends DetectionAlgorithm {
	
	private Invariant invariant;

	public InvariantChecker(AlgorithmConfiguration conf) {
		super(conf);
		invariant = (Invariant)conf.getRawItem(AlgorithmConfiguration.INVARIANT);
	}

	public Invariant getInvariant() {
		return invariant;
	}

	@Override
	protected double evaluateSnapshot(Snapshot sysSnapshot) {
		if(sysSnapshot instanceof MultipleSnapshot)
			return invariant.evaluateInvariant((MultipleSnapshot)sysSnapshot) ? 1.0 : 0.0;
		else return Double.MIN_VALUE;
	}

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DataSeries getDataSeries() {
		// TODO Auto-generated method stub
		return null;
	}

}
