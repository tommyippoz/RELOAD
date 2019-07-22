/**
 * 
 */
package ippoz.reload.algorithm.result;

import ippoz.reload.commons.failure.InjectedElement;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class DBSCANResult extends ClusteringResult {
	
	private double clusterVariance;

	public DBSCANResult(List<Double> dataValues, InjectedElement injection, double score, double clusterVariance) {
		super(dataValues, injection, score);
		this.clusterVariance = clusterVariance;
	}

	@Override
	public double getClusterVariance() {
		return clusterVariance;
	}

}
