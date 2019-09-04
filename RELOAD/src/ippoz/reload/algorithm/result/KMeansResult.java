/**
 * 
 */
package ippoz.reload.algorithm.result;

import ippoz.reload.algorithm.elki.support.CustomKMeans.KMeansScore;
import ippoz.reload.commons.failure.InjectedElement;

import java.util.List;

import de.lmu.ifi.dbs.elki.data.model.KMeansModel;

/**
 * @author Tommy
 *
 */
public class KMeansResult extends ClusteringResult{
	
	/** The kmm. */
	private KMeansModel kmm;

	/**
	 * Instantiates a new clustering result.
	 *
	 * @param dataValues the data values
	 * @param injection the injection
	 * @param of the of
	 */
	@SuppressWarnings("rawtypes")
	public KMeansResult(List<Double> dataValues, InjectedElement injection, KMeansScore of) {
		super(dataValues, injection, of.getDistance());
		kmm = of.getCluster();
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.result.AlgorithmResult#printFileHeader(java.lang.String)
	 */
	@Override
	public String printFileHeader(String sep) {
		return super.printFileHeader(sep) + sep + "cluster mean (enclosed in {})" + sep + "cluster variance";
	}
	
	/**
	 * Gets the cluster variance.
	 *
	 * @return the cluster variance
	 */
	public double getClusterVariance(){
		return kmm.getVarianceContribution();
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.result.AlgorithmResult#toFileString(java.lang.String)
	 */
	@Override
	public String toFileString(String sep) {
		return super.toFileString(sep) + sep + "{" + kmm.getMean() + "}" + sep + kmm.getVarianceContribution();
	}	

}
