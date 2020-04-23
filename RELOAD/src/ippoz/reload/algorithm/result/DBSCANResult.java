/**
 * 
 */
package ippoz.reload.algorithm.result;

import ippoz.reload.commons.failure.InjectedElement;

/**
 * @author Tommy
 *
 */
public class DBSCANResult extends ClusteringResult {
	
	private double clusterVariance;

	public DBSCANResult(double[] dataValues, InjectedElement injection, double score, double clusterVariance, double confidence) {
		super(dataValues, injection, score, confidence);
		this.clusterVariance = clusterVariance;
	}

	@Override
	public double getClusterVariance() {
		return clusterVariance;
	}

}
