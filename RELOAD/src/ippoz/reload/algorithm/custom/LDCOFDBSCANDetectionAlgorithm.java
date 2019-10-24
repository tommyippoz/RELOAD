/**
 * 
 */
package ippoz.reload.algorithm.custom;

import ippoz.reload.algorithm.support.ClusterableSnapshot;
import ippoz.reload.algorithm.support.GenericCluster;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

/**
 * @author Tommy
 *
 */
public class LDCOFDBSCANDetectionAlgorithm extends LDCOFDetectionAlgorithm {
	
	/** The Constant EPS. */
	public static final String EPS = "eps";
	
	/** The Constant DEFAULT_EPS. */
	public static final int DEFAULT_EPS = 3;
	
	/** The Constant PTS. */
	public static final String PTS = "pts";
	
	/** The Constant DEFAULT_PTS. */
	public static final double DEFAULT_PTS = 3;

	public LDCOFDBSCANDetectionAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
	}
	
	/**
	 * Gets the pts.
	 *
	 * @return the pts
	 */
	private double getPTS() {
		return conf.hasItem(PTS) ? Double.parseDouble(conf.getItem(PTS)) : DEFAULT_PTS;
	}
	
	/**
	 * Gets the eps.
	 *
	 * @return the eps
	 */
	private int getEPS() {
		return conf.hasItem(EPS) ? Integer.parseInt(conf.getItem(EPS)) : DEFAULT_EPS;
	}

	@Override
	protected List<GenericCluster> generateClusters(List<ClusterableSnapshot> clSnapList) {
		DBSCANClusterer<ClusterableSnapshot> dbSCAN;
		dbSCAN = new DBSCANClusterer<ClusterableSnapshot>(getEPS(), (int)(clSnapList.get(0).getPoint().length*getPTS()));
		List<Cluster<ClusterableSnapshot>> cSnaps = dbSCAN.cluster(clSnapList);
		List<GenericCluster> clSnaps = new LinkedList<GenericCluster>();
		for(Cluster<ClusterableSnapshot> cs : cSnaps){
			clSnaps.add(new GenericCluster(cs.getPoints()));
		}
		return clSnaps;
	}
	
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		defPar.put("threshold", new String[]{"IQR", "LEFT_IQR"});
		defPar.put("eps", new String[]{"100", "500", "1000"});
		defPar.put("pts", new String[]{"0.5", "1", "2"});
		defPar.put("gamma", new String[]{"0.3", "0.5"});
		return defPar;
	}

}
