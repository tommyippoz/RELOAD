/**
 * 
 */
package ippoz.madness.detector.algorithm.result;

import ippoz.madness.detector.algorithm.elki.support.CustomKMeans.KMeansScore;
import ippoz.reload.commons.failure.InjectedElement;

import java.util.List;

import de.lmu.ifi.dbs.elki.data.model.KMeansModel;

/**
 * @author Tommy
 *
 */
public class ClusteringResult extends AlgorithmResult {
	
	private KMeansModel kmm;

	@SuppressWarnings("rawtypes")
	public ClusteringResult(List<Double> dataValues, InjectedElement injection, KMeansScore of) {
		super(dataValues, injection, of.getDistance());
		kmm = of.getCluster();
	}

	@Override
	public String printFileHeader(String sep) {
		return super.printFileHeader(sep) + sep + "cluster mean (enclosed in {})" + sep + "cluster variance";
	}
	
	public double getClusterVariance(){
		return kmm.getVarianceContribution();
	}

	@Override
	public String toFileString(String sep) {
		return super.toFileString(sep) + sep + "{" + kmm.getMean() + "}" + sep + kmm.getVarianceContribution();
	}	

}
