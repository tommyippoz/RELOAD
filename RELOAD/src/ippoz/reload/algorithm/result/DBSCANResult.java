/**
 * 
 */
package ippoz.reload.algorithm.result;


/**
 * @author Tommy
 *
 */
public class DBSCANResult extends ClusteringResult {
	
	private double clusterVariance;

	public DBSCANResult(boolean hasInjection, double score, double clusterVariance, double confidence) {
		super(hasInjection, score, confidence);
		this.clusterVariance = clusterVariance;
	}

	@Override
	public double getClusterVariance() {
		return clusterVariance;
	}

}
