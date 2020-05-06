/**
 * 
 */
package ippoz.reload.algorithm.result;

import ippoz.reload.commons.failure.InjectedElement;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class KMeansResult extends ClusteringResult{
	
	/** The kmm. */
	private Vector mean;
	
	private double var;

	/**
	 * Instantiates a new clustering result.
	 *
	 * @param dataValues the data values
	 * @param injection the injection
	 * @param of the of
	 */
	public KMeansResult(double[] dataValues, InjectedElement injection, double score, Vector vector, double var, double confidence) {
		super(dataValues, injection, score, confidence);
		this.mean = vector;
		this.var = var;
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
		return var;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.result.AlgorithmResult#toFileString(java.lang.String)
	 */
	@Override
	public String toFileString(String sep) {
		return super.toFileString(sep) + sep + "{" + mean + "}" + sep + var;
	}	

}
