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
import ippoz.reload.commons.support.LabelledValue;
import ippoz.reload.commons.utils.ObjectPair;

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
public class DBSCANDetectionAlgorithm extends DataSeriesNonSlidingAlgorithm {

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

	public DBSCANDetectionAlgorithm(DataSeries dataSeries,
			BasicConfiguration conf) {
		super(dataSeries, conf);
		if (conf.hasItem(CLUSTERS)) {
			clSnaps = loadFromConfiguration();
			loadFile(getFilename());
		}
	}

	/**
	 * Loads clusters from configuration.
	 *
	 * @return the clusters
	 */
	private List<GenericCluster> loadFromConfiguration() {
		List<GenericCluster> confClusters = new LinkedList<GenericCluster>();
		for (String clString : conf.getItem(CLUSTERS).trim().split("ç")) {
			confClusters.add(new GenericCluster(clString));
		}
		return confClusters;
	}

	/**
	 * Gets the pts.
	 *
	 * @return the pts
	 */
	private double getPTS() {
		return conf.hasItem(PTS) ? Double.parseDouble(conf.getItem(PTS))
				: DEFAULT_PTS;
	}

	/**
	 * Gets the eps.
	 *
	 * @return the eps
	 */
	private int getEPS() {
		return conf.hasItem(EPS) ? Integer.parseInt(conf.getItem(EPS))
				: DEFAULT_EPS;
	}

	@Override
	public List<Double> getTrainScores() {
		List<Double> list = new LinkedList<Double>();
		for (LabelledValue score : scores) {
			list.add(score.getValue());
		}
		return list;
	}

	@Override
	public ObjectPair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		GenericCluster uc = calculateCluster(snapArray);
		if(uc != null){
			double score = uc.distanceFrom(snapArray);
			return new ObjectPair<Double, Object>(score, uc.getVar());
		} else return new ObjectPair<Double, Object>(Double.NaN, null);
	}

	@Override
	protected boolean checkCalculationCondition(double[] snapValue) {
		return clSnaps != null && clSnaps.size() > 0;
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
	 * @param filename
	 *            the filename
	 */
	public void loadFile(String filename) {
		loadClustersFile(new File(filename));
		// loadScoresFile(new File(filename + "scores"));
	}

	/**
	 * Load clusters file.
	 *
	 * @param file
	 *            the file
	 */
	private void loadClustersFile(File file) {
		BufferedReader reader;
		String readed;
		try {
			if (file.exists()) {
				reader = new BufferedReader(new FileReader(file));
				reader.readLine();
				clSnaps = new LinkedList<GenericCluster>();
				while (reader.ready()) {
					readed = reader.readLine();
					if (readed != null) {
						readed = readed.trim();
						clSnaps.add(new GenericCluster(readed.trim()));
					}
				}
				reader.close();
			}
		} catch (IOException ex) {
			AppLogger
					.logException(getClass(), ex, "Unable to read DBSCAN file");
		}
	}

	@Override
	public void saveLoggedScores() {
		conf.addItem(CLUSTERS, clustersToConfiguration());
		if (!new File(getDefaultTmpFolder()).exists())
			new File(getDefaultTmpFolder()).mkdirs();
		printFile(new File(getFilename()));
		super.saveLoggedScores();
	}

	@Override
	public boolean automaticInnerTraining(List<Knowledge> kList) {
		List<ClusterableSnapshot> clSnapList = new LinkedList<>();
		for (Snapshot snap : Knowledge.toSnapList(kList, getDataSeries())) {
			clSnapList.add(new ClusterableSnapshot(snap, getDataSeries()));
		}

		DBSCANClusterer<ClusterableSnapshot> dbSCAN;
		dbSCAN = new DBSCANClusterer<ClusterableSnapshot>(getEPS(),
				(int) (clSnapList.get(0).getPoint().length * getPTS()));
		List<Cluster<ClusterableSnapshot>> cSnaps = dbSCAN.cluster(clSnapList);
		if (cSnaps != null) {
			clSnaps = new LinkedList<GenericCluster>();
			for (Cluster<ClusterableSnapshot> cs : cSnaps) {
				clSnaps.add(new GenericCluster(cs.getPoints()));
			}
		}

		scores = new LinkedList<DBSCANScore>();
		for (Snapshot snap : Knowledge.toSnapList(kList, getDataSeries())) {
			scores.add(new DBSCANScore(snap.snapToString(), calculateDBSCAN(snap), snap.isAnomalous()));
		}

		conf.addItem(TMP_FILE, getFilename());

		return true;
	}

	/**
	 * Calculate DBSCAN.
	 *
	 * @param snapsArray
	 *            the snap
	 * @return the double
	 */
	private GenericCluster calculateCluster(double[] sArr) {
		double dbScan = Double.MAX_VALUE;
		GenericCluster best = null;
		if(clSnaps != null && clSnaps.size() > 0) {
			for (GenericCluster uc : clSnaps) {
				double dist = uc.distanceFrom(sArr);
				if (dist < dbScan) {
					dbScan = dist;
					best = uc;
				}
			}
		}
		return best;
	}

	/**
	 * Calculate DBSCAN.
	 *
	 * @param snap
	 *            the snap
	 * @return the double
	 */
	private double calculateDBSCAN(Snapshot snap) {
		double[] sArr = getSnapValueArray(snap);
		double dbScan = Double.MAX_VALUE;
		for (GenericCluster uc : clSnaps) {
			double dist = uc.distanceFrom(sArr);
			if (dist < dbScan)
				dbScan = dist;
		}
		return dbScan;
	}

	/**
	 * Prints the file.
	 *
	 * @param file
	 *            the file
	 */
	private void printFile(File file) {
		printClusters(file);
		printScores(new File(file.getPath() + "scores"));
	}

	/**
	 * Prints the histograms.
	 *
	 * @param file
	 *            the file
	 */
	private void printClusters(File file) {
		BufferedWriter writer;
		try {
			if (clSnaps != null && clSnaps.size() > 0) {
				if (file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("cluster_avg,cluster_std\n");
				for (GenericCluster uc : clSnaps) {
					writer.write(uc.toConfiguration().replace("@", ";") + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex,
					"Unable to write DBSCAN clusters file");
		}
	}

	/**
	 * Prints the scores.
	 *
	 * @param file
	 *            the file
	 */
	private void printScores(File file) {
		BufferedWriter writer;
		try {
			if (clSnaps != null && clSnaps.size() > 0 && scores != null) {
				if (file.exists())
					file.delete();
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("data(enclosed in {});dbscan\n");
				for (DBSCANScore score : scores) {
					writer.write(score.getSnapValue() + ";" + score.getDBSCAN()
							+ ";" + score.getLabel() + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex,
					"Unable to write DBSCAN scores file");
		}
	}

	private String clustersToConfiguration() {
		String toReturn = "";
		for (GenericCluster uc : clSnaps) {
			toReturn = toReturn + uc.toConfiguration() + "ç";
		}
		return toReturn.substring(0, toReturn.length() - 1);
	}

	private class DBSCANScore extends LabelledValue {

		/** The snap value. */
		private String snapValue;

		/**
		 * Instantiates a new HBOS score.
		 *
		 * @param snapValue
		 *            the snap value
		 * @param dbscan
		 *            the hbos
		 */
		public DBSCANScore(String snapValue, double dbscan, boolean flag) {
			super(dbscan, flag);
			this.snapValue = snapValue;
		}

		/**
		 * Gets the hbos.
		 *
		 * @return the hbos
		 */
		public double getDBSCAN() {
			return super.getValue();
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
		defPar.put("eps", new String[] { "100", "500", "1000" });
		defPar.put("pts", new String[] { "0.5", "1", "2" });
		return defPar;
	}

	@Override
	protected void storeAdditionalPreferences() {
		// TODO Auto-generated method stub

	}

}
