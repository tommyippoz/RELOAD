/**
 * 
 */
package ippoz.reload.algorithm;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.result.DBSCANResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

/**
 * @author Tommy
 *
 */
public class DBSCANDetectionAlgorithm extends DataSeriesDetectionAlgorithm implements AutomaticTrainingAlgorithm {

	/** The Constant HISTOGRAMS. */
	public static final String CLUSTERS = "clusters";
	
	/** The Constant THRESHOLD. */
	public static final String THRESHOLD = "threshold";
	
	/** The Constant TMP_FILE. */
	private static final String TMP_FILE = "tmp_file";
	
	/** The Constant EPS. */
	public static final String EPS = "eps";
	
	/** The Constant DEFAULT_EPS. */
	public static final int DEFAULT_EPS = 3;
	
	/** The Constant PTS. */
	public static final String PTS = "pts";
	
	/** The Constant DEFAULT_PTS. */
	public static final int DEFAULT_PTS = 3;
	
	private List<UsableCluster> clSnaps;
	
	private List<DBSCANScore> scores;
	
	public DBSCANDetectionAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(CLUSTERS)){
			clSnaps = loadFromConfiguration();
			loadFile(getFilename());
			clearLoggedScores();
			logScores(filterScores());
		}
	}

	/**
	 * Loads clusters from configuration.
	 *
	 * @return the clusters
	 */
	private List<UsableCluster> loadFromConfiguration(){
		List<UsableCluster> confClusters = new LinkedList<UsableCluster>();
		for(String clString : conf.getItem(CLUSTERS).trim().split("ç")){
			confClusters.add(new UsableCluster(clString));
		}
		return confClusters;
	}
	
	/**
	 * Gets the pts.
	 *
	 * @return the pts
	 */
	private int getPTS() {
		return conf.hasItem(PTS) ? Integer.parseInt(conf.getItem(PTS)) : DEFAULT_PTS;
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
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		AlgorithmResult ar;
		UsableCluster uc;
		double[] snapsArray = getSnapValueArray(sysSnapshot);
		if(clSnaps != null){
			uc = calculateCluster(snapsArray);
			ar = new DBSCANResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), uc.distanceFrom(snapsArray), uc.getVar());
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} else return AlgorithmResult.error(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
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
	 * Load file.
	 *
	 * @param filename the filename
	 */
	public void loadFile(String filename) {
		loadClustersFile(new File(filename));
		loadScoresFile(new File(filename + "scores"));		
	}
	
	/**
	 * Load scores file.
	 *
	 * @param file the file
	 */
	private void loadScoresFile(File file) {
		BufferedReader reader;
		String readed;
		try {
			if(file.exists()){
				scores = new LinkedList<DBSCANScore>();
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && readed.split(";").length >= 2)
							scores.add(new DBSCANScore(readed.split(";")[0], Double.parseDouble(readed.split(";")[1])));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read DBSCAN Scores file");
		} 
	}
	
	/**
	 * Load clusters file.
	 *
	 * @param file the file
	 */
	private void loadClustersFile(File file){
		BufferedReader reader;
		String readed;
		try {
			if(file.exists()){
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				clSnaps = new LinkedList<UsableCluster>();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						clSnaps.add(new UsableCluster(readed.trim()));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read DBSCAN file");
		} 
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
		dbSCAN = new DBSCANClusterer<ClusterableSnapshot>(getEPS(), clSnapList.get(0).getPoint().length*getPTS());
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
		
		return true;
	}
	
	/**
	 * Calculate DBSCAN.
	 *
	 * @param snapsArray the snap
	 * @return the double
	 */
	private UsableCluster calculateCluster(double[] sArr){
		double dbScan = Double.MAX_VALUE;
		UsableCluster best = null;
		for(UsableCluster uc : clSnaps){
			double dist = uc.distanceFrom(sArr);
			if(dist < dbScan){
				dbScan = dist;
				best = uc;
			}
		}
		return best;
	}
	
	/**
	 * Calculate DBSCAN.
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
				writer.write("cluster_avg,cluster_std\n");
				for(UsableCluster uc : clSnaps){
					writer.write(uc.toConfiguration().replace("@", ";") + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write DBSCAN clusters file");
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
		return getDefaultTmpFolder() + File.separatorChar + getDataSeries().getCompactString().replace("\\", "_").replace("/", "-").replace("*", "_") + ".dbscan";
	}
	
	/**
	 * Gets the default folder used to store temporary data.
	 *
	 * @return the default temporary folder
	 */
	private String getDefaultTmpFolder(){
		return File.separatorChar + "tmp" + File.separatorChar + "dbscan_tmp_RELOAD";
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
		
		public String toConfiguration() {
			return Arrays.toString(avg) + ";" + var;
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
		
		public UsableCluster(String clString) {
			String[] splitted;
			if(clString != null){
				splitted = clString.split(";");
				var = Double.valueOf(splitted[1]);
				clString = splitted[0].replace("[", "").replace("]", "");
				splitted = clString.split(",");
				avg = new double[splitted.length];
				for(int i=0;i<avg.length;i++){
					avg[i] = Double.valueOf(splitted[i]);
				}
			}
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
