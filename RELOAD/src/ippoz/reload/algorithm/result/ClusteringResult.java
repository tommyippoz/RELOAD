/**
 * 
 */
package ippoz.reload.algorithm.result;


/** 
 * The Class ClusteringResult.
 *
 * @author Tommy
 */
public abstract class ClusteringResult extends AlgorithmResult {

	public ClusteringResult(boolean hasInjection, double score, double confidence) {
		super(hasInjection, score, confidence, null);
		// TODO Auto-generated constructor stub
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
	public abstract double getClusterVariance();
	
	public double getClusterStd(){
		return Math.sqrt(getClusterVariance());
	}

}
