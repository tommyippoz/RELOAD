/**
 * 
 */
package ippoz.reload.algorithm.support;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;

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
		if(ds.size() == 1){
			points[0] = ((DataSeriesSnapshot)clSnap).getSnapValue().getFirst();
		} else {
			for(int j=0;j<ds.size();j++){
				points[j] = ((MultipleSnapshot)clSnap).getSnapshot(((MultipleDataSeries)ds).getSeries(j)).getSnapValue().getFirst();
			}
		}
		return points;
	}
	
}
