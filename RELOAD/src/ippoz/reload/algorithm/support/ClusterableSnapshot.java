/**
 * 
 */
package ippoz.reload.algorithm.support;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Snapshot;

import org.apache.commons.math3.ml.clustering.Clusterable;

/**
 * @author Tommy
 *
 */
public class ClusterableSnapshot implements Clusterable {
	
	private Snapshot clSnap;
	
	private DataSeries ds;
	
	public ClusterableSnapshot(Snapshot snap, DataSeries ds){
		clSnap = snap;
		this.ds = ds;
	}

	@Override
	public double[] getPoint() {
		double[] points = new double[ds.size()];
		for(int j=0;j<ds.size();j++){
			points[j] = clSnap.getDoubleValueFor(ds.getIndicators()[j]);
		}
		return points;
	}
	
}
