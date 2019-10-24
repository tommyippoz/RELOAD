/**
 * 
 */
package ippoz.reload.algorithm.custom;

import ippoz.reload.algorithm.AutomaticTrainingAlgorithm;
import ippoz.reload.algorithm.DataSeriesDetectionAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.result.DBSCANResult;
import ippoz.reload.algorithm.support.ClusterableSnapshot;
import ippoz.reload.algorithm.support.GenericCluster;
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
	
	private List<GenericCluster> clSnaps;
	
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
	private List<GenericCluster> loadFromConfiguration(){
		List<GenericCluster> confClusters = new LinkedList<GenericCluster>();
		for(String clString : conf.getItem(CLUSTERS).trim().split("ç")){
			confClusters.add(new GenericCluster(clString));
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
		GenericCluster uc;
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
				clSnaps = new LinkedList<GenericCluster>();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						clSnaps.add(new GenericCluster(readed.trim()));
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
			clSnapList.add(new ClusterableSnapshot(snap, getDataSeries()));
		}
		
		DBSCANClusterer<ClusterableSnapshot> dbSCAN;
		dbSCAN = new DBSCANClusterer<ClusterableSnapshot>(getEPS(), clSnapList.get(0).getPoint().length*getPTS());
		List<Cluster<ClusterableSnapshot>> cSnaps = dbSCAN.cluster(clSnapList);
		if(cSnaps != null){
			clSnaps = new LinkedList<GenericCluster>();
			for(Cluster<ClusterableSnapshot> cs : cSnaps){
				clSnaps.add(new GenericCluster(cs.getPoints()));
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
	private GenericCluster calculateCluster(double[] sArr){
		double dbScan = Double.MAX_VALUE;
		GenericCluster best = null;
		for(GenericCluster uc : clSnaps){
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
		for(GenericCluster uc : clSnaps){
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
				for(GenericCluster uc : clSnaps){
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
		for(GenericCluster uc : clSnaps){
			toReturn = toReturn + uc.toConfiguration() + "ç";
		}
		return toReturn.substring(0, toReturn.length()-1);
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
	
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		defPar.put("threshold", new String[]{"CLUSTER(0.1STD)", "CLUSTER(0.5STD)"});
		defPar.put("eps", new String[]{"100", "500", "1000"});
		defPar.put("pts", new String[]{"0.5", "1", "2"});
		return defPar;
	}

}
