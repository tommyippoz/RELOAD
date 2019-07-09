/**
 * 
 */
package ippoz.reload.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.service.ServiceCall;
import ippoz.reload.commons.support.AppLogger;

/**
 * @author Tommy
 *
 */
public class DBSCANDetectionAlgorithm extends DataSeriesDetectionAlgorithm implements AutomaticTrainingAlgorithm {

	/** The Constant THRESHOLD. */
	public static final String THRESHOLD = "threshold";
	
	/** The Constant TMP_FILE. */
	private static final String TMP_FILE = "tmp_file";

	private static final String CLUSTERS = "clusters";
	
	private List<UsableCluster> clSnaps;
	
	private List<DBSCANScore> scores;
	
	public DBSCANDetectionAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge,
			Snapshot sysSnapshot, int currentIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Filter scores.
	 *
	 * @return the list
	 */
	private List<Double> filterScores() {
		List<Double> list = new LinkedList<Double>();
		for(DBSCANScore score : scores){
			list.add(score.getDBSCAN());
		}
		return list;	
	}

	@Override
	public boolean automaticTraining(List<Knowledge> kList, boolean createOutput) {
		List<ClusterableSnapshot> clSnapList = new LinkedList<>();
		for(Snapshot snap : Knowledge.toSnapList(kList, getDataSeries())){
			clSnapList.add(new ClusterableSnapshot(snap));
		}
		
		DBSCANClusterer<ClusterableSnapshot> dbSCAN;
		dbSCAN = new DBSCANClusterer<ClusterableSnapshot>(3, 3);
		List<Cluster<ClusterableSnapshot>> cSnaps = dbSCAN.cluster(clSnapList);
		if(cSnaps != null){
			clSnaps = new LinkedList<UsableCluster>();
			for(Cluster<ClusterableSnapshot> cs : cSnaps){
				clSnaps.add(new UsableCluster(cs.getPoints()));
			}
		}
		
		scores = new LinkedList<DBSCANScore>();
		for(Snapshot snap : Knowledge.toSnapList(kList, getDataSeries())){
			scores.add(new DBSCANScore(Snapshot.snapToString(snap, getDataSeries()), calculateDBSCAN(snap)));
		}
		clearLoggedScores();
		logScores(filterScores());
		
		conf.addItem(TMP_FILE, getFilename());
		
		if(createOutput) {
			conf.addItem(CLUSTERS, clustersToConfiguration());
			if(!new File(getDefaultTmpFolder()).exists())
	    		new File(getDefaultTmpFolder()).mkdirs();
	    	printFile(new File(getFilename()));
		}
		
		return false;
	}
	
	/**
	 * Calculate hbos.
	 *
	 * @param snap the snap
	 * @return the double
	 */
	private double calculateDBSCAN(Snapshot snap){
		double[] sArr = getSnapValueArray(snap);
		double dbScan = Double.MAX_VALUE;
		for(UsableCluster uc : clSnaps){
			double dist = uc.distanceFrom(sArr);
			if(dist < dbScan)
				dbScan = dist;
		}
		return dbScan;
	}
	
	private double[] getSnapValueArray(Snapshot snap){
		double snapValue;
		double[] result = new double[getDataSeries().size()];
		if(getDataSeries().size() == 1){
			snapValue = ((DataSeriesSnapshot)snap).getSnapValue().getFirst();
			result[0] = snapValue;
		} else {
			for(int j=0;j<getDataSeries().size();j++){
				snapValue = ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
				result[j] = snapValue;
			}
		}
		return result;
	}
	
	/**
	 * Prints the file.
	 *
	 * @param file the file
	 */
	private void printFile(File file) {
		printClusters(file);
		printScores(new File(file.getPath() + "scores"));
	}
	
	/**
	 * Prints the histograms.
	 *
	 * @param file the file
	 */
	private void printClusters(File file){
		BufferedWriter writer;
		try {
			if(clSnaps != null && clSnaps.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("histogram\n");
				for(UsableCluster uc : clSnaps){
					writer.write(uc.toConfiguration().replace("@", ";") + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write KMEANS clusters file");
		} 
	}
	
	/**
	 * Prints the scores.
	 *
	 * @param file the file
	 */
	private void printScores(File file){
		BufferedWriter writer;
		try {
			if(clSnaps != null && clSnaps.size() > 0){
				if(file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("data(enclosed in {});dbscan\n");
				for(DBSCANScore score : scores){
					writer.write(score.getSnapValue() + ";" + score.getDBSCAN() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write DBSCAN scores file");
		} 
	}
	
	private String clustersToConfiguration() {
		String toReturn = "";
		for(UsableCluster uc : clSnaps){
			toReturn = toReturn + uc.toConfiguration() + "ç";
		}
		return toReturn.substring(0, toReturn.length()-1);
	}

	/**
	 * Gets the filename used to store data about scores and histograms.
	 *
	 * @return the filename
	 */
	private String getFilename(){
		return getDefaultTmpFolder() + File.separatorChar + getDataSeries().getCompactString().replace("\\", "_").replace("/", "-").replace("*", "_") + ".hbos";
	}
	
	/**
	 * Gets the default folder used to store temporary data.
	 *
	 * @return the default temporary folder
	 */
	private String getDefaultTmpFolder(){
		return "DBSCAN_tmp_RELOAD";
	}
	
	private class ClusterableSnapshot implements Clusterable {
		
		private Snapshot clSnap;
		
		public ClusterableSnapshot(Snapshot snap){
			clSnap = snap;
		}

		@Override
		public double[] getPoint() {
			double[] points = new double[getDataSeries().size()];
			if(getDataSeries().size() == 1){
				points[0] = ((DataSeriesSnapshot)clSnap).getSnapValue().getFirst();
			} else {
				for(int j=0;j<getDataSeries().size();j++){
					points[j] = ((MultipleSnapshot)clSnap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
				}
			}
			return points;
		}
		
	}
	
	private class UsableCluster {
		
		private double[] avg;
		
		private Double var;
		
		private List<double[]> points;
		
		public UsableCluster(double[] avg, Double var) {
			this.avg = avg;
			this.var = var;
			points = null;
		}
		
		public String toConfiguration() {
			return String.valueOf(avg) + "@" + var;
		}

		public UsableCluster(List<ClusterableSnapshot> cSnaps) {
			avg = null;
			points = new LinkedList<double[]>();
			for(ClusterableSnapshot cs : cSnaps){
				points.add(cs.getPoint());
			}
			calculateAvg();
			calculateVar();
		}
		
		public double distanceFrom(double[] point){
			return euclideanDistance(avg, point);
		}

		private void calculateVar() {
			double val = 0;
			if(points != null && points.size() > 0){
				if(avg != null){
					for(double[] point : points){
						if(point != null){
							val = val + Math.pow(euclideanDistance(avg, point), 2);
						}
					}
				}
			}
			var = val;
		}
		
		private double euclideanDistance(double[] d1, double[] d2){
			double res = 0;
			if(d1 == null || d2 == null)
				return Double.MAX_VALUE;
			if(d1.length == d2.length){
				for(int i=0;i<d1.length;i++){
					res = res + Math.pow(d1[i] - d2[i], 2);
				}
			}
			return Math.sqrt(res);
		}

		private void calculateAvg() {
			int count = 0;
			if(points != null && points.size() > 0 && points.get(0) != null){
				avg = new double[points.get(0).length];
				for(double[] point : points){
					if(point != null){
						for(int i=0;i<avg.length;i++){
							avg[i] = avg[i] + point[i];
						}
						count++;
					}
				}
				for(int i=0;i<avg.length;i++){
					avg[i] = avg[i]/count;
				} 
			}
			
		}

		public double[] getAvg(){
			return avg;
		} 
		
		public double getVar(){
			return var;
		}
		
		public double getStd(){
			return Math.sqrt(getVar());
		}
		
	}
	
	private class DBSCANScore {
		
		/** The DBSCAN. */
		private double dbscan;
		
		/** The snap value. */
		private String snapValue;

		/**
		 * Instantiates a new HBOS score.
		 *
		 * @param snapValue the snap value
		 * @param dbscan the hbos
		 */
		public DBSCANScore(String snapValue, double dbscan) {
			this.dbscan = dbscan;
			this.snapValue = snapValue;
		}

		/**
		 * Gets the hbos.
		 *
		 * @return the hbos
		 */
		public double getDBSCAN() {
			return dbscan;
		}

		/**
		 * Gets the snap value.
		 *
		 * @return the snap value
		 */
		public String getSnapValue() {
			return snapValue;
		}
		
	}

}
