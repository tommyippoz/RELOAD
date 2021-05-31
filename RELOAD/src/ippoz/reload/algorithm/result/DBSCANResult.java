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

	public DBSCANResult(boolean hasInjection, double score, double clusterVariance, double confidence, boolean isUnknown) {
		super(hasInjection, score, confidence, isUnknown);
		this.clusterVariance = clusterVariance;
	}

	@Override
	public double getClusterVariance() {
		return clusterVariance;
	}

}
