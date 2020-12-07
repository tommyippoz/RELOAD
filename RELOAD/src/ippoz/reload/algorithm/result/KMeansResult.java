/**
 * 
 */
package ippoz.reload.algorithm.result;


/**
 * @author Tommy
 *
 */
public class KMeansResult extends ClusteringResult {
	
	private double var;

	/**
	 * Instantiates a new clustering result.
	 *
	 * @param dataValues the data values
	 * @param injection the injection
	 * @param of the of
	 */
	public KMeansResult(boolean hasInjection, double score, double var, double confidence) {
		super(hasInjection, score, confidence);
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

}
