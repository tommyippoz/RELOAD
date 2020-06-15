/**
 * 
 */
package ippoz.reload.algorithm.custom;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.support.ClusterableSnapshot;
import ippoz.reload.algorithm.support.GenericCluster;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;

/**
 * @author Tommaso Capecchi, Tommaso Zoppi
 *
 */
public abstract class LDCOFDetectionAlgorithm extends DataSeriesNonSlidingAlgorithm {
	
	/** The Constant HISTOGRAMS. */
	public static final String CLUSTERS = "clusters";
	
	/** The Constant HISTOGRAMS. */
	public static final String GAMMA = "gamma";
	
	/** The Constant HISTOGRAMS. */
	public static final double DEFAULT_GAMMA = 0.6;
	
	private List<GenericCluster> clSnaps;
	
	private List<LDCOFScore> scores;
	
	private LDCOFModel model;

	public LDCOFDetectionAlgorithm(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(CLUSTERS)){
			clSnaps = loadFromConfiguration();
			model = new LDCOFModel(clSnaps);
			model.train(getGamma());
			loadFile(getFilename());
		}
	}
	
	@Override
	public void saveLoggedScores() {
		conf.addItem(CLUSTERS, clustersToConfiguration());
    	printFile(new File(getFilename()));
		super.saveLoggedScores();
	}
	
	@Override
	public boolean automaticInnerTraining(List<Knowledge> kList) {
		List<ClusterableSnapshot> clSnapList = new LinkedList<>();
		for(Snapshot snap : Knowledge.toSnapList(kList, getDataSeries())){
			clSnapList.add(new ClusterableSnapshot(snap, getDataSeries()));
		}
		 
		clSnaps = generateClusters(clSnapList);
		model = new LDCOFModel(clSnaps);
		
		//Recognize small and large clusters
		model.train(getGamma());
		
		scores = new LinkedList<LDCOFScore>();
		for(Snapshot snap : Knowledge.toSnapList(kList, getDataSeries())){
			scores.add(new LDCOFScore(Snapshot.snapToString(snap, getDataSeries()), calculateLDCOF(getSnapValueArray(snap))));
		}
		
		return true;
	}
	
	private double getGamma(){
		if(conf.hasItem(GAMMA))
			return Double.parseDouble(conf.getItem(GAMMA));
		else return DEFAULT_GAMMA;
	}
	
	private double calculateLDCOF(double[] snapArray) {
		return model.evaluate(snapArray);
	}

	protected abstract List<GenericCluster> generateClusters(List<ClusterableSnapshot> clSnapList);
	
	@Override
	protected boolean checkCalculationCondition(double[] snapArray) {
		return clSnaps != null;
	}

	@Override
	public Pair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		return new Pair<Double, Object>(calculateLDCOF(snapArray), null);
	}

	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		// TODO Auto-generated method stub
		return null;
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
				writer.write("cluster_avg;cluster_std;distance_center;size;isLarge\n");
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
				for(LDCOFScore score : scores){
					writer.write(score.getSnapValue() + ";" + score.getLDCOF() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write LDCOF scores file");
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
	
	private String clustersToConfiguration() {
		String toReturn = "";
		for(GenericCluster uc : clSnaps){
			toReturn = toReturn + uc.toConfiguration() + "ç";
		}
		return toReturn.substring(0, toReturn.length()-1);
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
	@Override
	public List<Double> getTrainScores() {
		List<Double> list = new LinkedList<Double>();
		for(LDCOFScore score : scores){
			list.add(score.getLDCOF());
		}
		return list;	
	}
	
	/**
	 * Load file.
	 *
	 * @param filename the filename
	 */
	public void loadFile(String filename) {
		loadClustersFile(new File(filename));
		//loadScoresFile(new File(filename + "scores"));		
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
	 * Load scores file.
	 *
	 * @param file the file
	 */
	private void loadScoresFile(File file) {
		BufferedReader reader;
		String readed;
		try {
			if(file.exists()){
				scores = new LinkedList<LDCOFScore>();
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && readed.split(";").length >= 2)
							scores.add(new LDCOFScore(readed.split(";")[0], Double.parseDouble(readed.split(";")[1])));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read DBSCAN Scores file");
		} 
	}
	
	private class LDCOFScore {
		
		/** The DBSCAN. */
		private double ldcof;
		
		/** The snap value. */
		private String snapValue;

		/**
		 * Instantiates a new HBOS score.
		 *
		 * @param snapValue the snap value
		 * @param dbscan the hbos
		 */
		public LDCOFScore(String snapValue, double dbscan) {
			this.ldcof = dbscan;
			this.snapValue = snapValue;
		}

		/**
		 * Gets the hbos.
		 *
		 * @return the hbos
		 */
		public double getLDCOF() {
			return ldcof;
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
	
	private class LDCOFModel {
		
		private List<GenericCluster> centroids;
		
		private int k;
		
		private int numberOfDatas;
		
		private static final int LIMIT_VALUE = 100000;
		
		private double thresHoldLargeCluster;
		
		public LDCOFModel(List<GenericCluster>centroids) {
			this.centroids = centroids;
			this.k = centroids.size();
			this.numberOfDatas = findNumberOfDatas();
		}
		
		private int findNumberOfDatas() {
			int result = 0;
			for(GenericCluster centroid : centroids) {
				result = result + centroid.getSize();
			}
			return result;
		}
		
		/*
		 * Returns a List of Centroids (which represents the clusters in the model)
		 * where each cluster is recognize as small or large cluster
		 */
		public void train(double gamma) {
			this.thresHoldLargeCluster = (gamma * numberOfDatas) / k;
			this.assignLargeClusters(thresHoldLargeCluster);
		}
		
		private void assignLargeClusters(double minimumSizeThreshold) {
			for(GenericCluster centroid : centroids) {
				if(centroid.getSize() >= minimumSizeThreshold) {
					centroid.setLarge(true);
				}
			}
		}
		
		public double evaluate(double[] data) {
			/*
			 * -Find the cluster which 'data' belongs to.
			 * -If it's a small cluster, find the nearest large cluster: then compute the score
			 * as follow: euclideanDistance(data, centroid)/centroid.getAvgDistance()
			 * -If it's a large cluster, then compute the score as follow:
			 * euclideanDistance(data, centroid)/centroid.getAvgDistance()	
			 */
			
			double score = 0, distanceFrom;
			GenericCluster centroid = getClusterFor(data);
			if(centroid == null)
				return Double.NaN;
			else {
				if(!centroid.isLarge() && findNearestLargeClusterFor(data) != null)
					centroid = findNearestLargeClusterFor(data);
				distanceFrom = centroid.distanceFrom(data);
				if(centroid.getAvgDistanceFromCenter() != 0)
					score = (centroid.distanceFrom(data))/centroid.getAvgDistanceFromCenter();
				else score = distanceFrom;
			} 
			return score;
		}
		
		private GenericCluster findNearestLargeClusterFor(double[] d) {
			double minValue = LIMIT_VALUE;
			for(GenericCluster centroid : centroids) {
				if(centroid.isLarge() && minValue >= centroid.distanceFrom(d)){
					return centroid;
				}
			}
			return null;
		}
		
		private GenericCluster getClusterFor(double[] d) {
			double minDist = Double.MAX_VALUE;
			GenericCluster best = null;
			for(GenericCluster uc : clSnaps){
				double dist = uc.distanceFrom(d);
				if(dist < minDist){
					minDist = dist;
					best = uc;
				}
			}
			return best;
		}
		
	}

}
