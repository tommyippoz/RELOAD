/**
 * 
 */
package ippoz.reload.algorithm;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.custom.DBSCANDetectionAlgorithm;
import ippoz.reload.algorithm.custom.HBOSDetectionAlgorithm;
import ippoz.reload.algorithm.custom.LDCOFDBSCANDetectionAlgorithm;
import ippoz.reload.algorithm.custom.LDCOFKMeansDetectionAlgorithm;
import ippoz.reload.algorithm.custom.SDODetectionAlgorithm;
import ippoz.reload.algorithm.custom.SOMDetectionAlgorithm;
import ippoz.reload.algorithm.elki.ABODELKI;
import ippoz.reload.algorithm.elki.COFELKI;
import ippoz.reload.algorithm.elki.FastABODELKI;
import ippoz.reload.algorithm.elki.GMeansELKI;
import ippoz.reload.algorithm.elki.ISOSELKI;
import ippoz.reload.algorithm.elki.KMeansELKI;
import ippoz.reload.algorithm.elki.KNNELKI;
import ippoz.reload.algorithm.elki.LOFELKI;
import ippoz.reload.algorithm.elki.ODINELKI;
import ippoz.reload.algorithm.elki.SOSELKI;
import ippoz.reload.algorithm.elki.SVMELKI;
import ippoz.reload.algorithm.meta.BaggingMetaLearner;
import ippoz.reload.algorithm.meta.BoostingMetaLearner;
import ippoz.reload.algorithm.meta.CascadeGeneralizationMetaLearner;
import ippoz.reload.algorithm.meta.CascadingMetaLearner;
import ippoz.reload.algorithm.meta.DelegatingMetaLearner;
import ippoz.reload.algorithm.meta.FullStackingMetaLearner;
import ippoz.reload.algorithm.meta.StackingMetaLearner;
import ippoz.reload.algorithm.meta.VotingMetaLearner;
import ippoz.reload.algorithm.meta.WeightedVotingMetaLearner;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.result.DBSCANResult;
import ippoz.reload.algorithm.result.KMeansResult;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.algorithm.weka.IsolationForestWEKA;
import ippoz.reload.commons.algorithm.AlgorithmFamily;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.LabelledValue;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.metric.Metric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.lmu.ifi.dbs.elki.data.model.KMeansModel;

/**
 * The Class DetectionAlgorithm.
 *
 * @author Tommy
 */
public abstract class DetectionAlgorithm implements AutomaticTrainingAlgorithm {

	/** The configuration. */
	protected BasicConfiguration conf;
	
	protected ValueSeries loggedScores;
	
	protected ValueSeries loggedAnomalyScores;
	
	protected DecisionFunction decisionFunction;
	
	/** The Constant TMP_FILE. */
	protected static final String TMP_FILE = "tmp_file";
	
	/** The Constant TMP_FILE. */
	public static final String TAG = "tag";
	
	protected final static int DEFAULT_MINIMUM_ITEMS = 5;
	
	/** The indicator. */
	protected DataSeries dataSeries;
	
	/**
	 * Instantiates a new detection algorithm.
	 *
	 * @param conf the configuration
	 */
	public DetectionAlgorithm(DataSeries dataSeries, BasicConfiguration conf){
		this.conf = conf;
		this.dataSeries = dataSeries;
		loggedScores = new ValueSeries();
		loggedAnomalyScores = new ValueSeries();
		decisionFunction = null;
		if(conf != null && conf.hasItem(TMP_FILE)){
			clearLoggedScores();
			loadLoggedScores();
			if(getDecisionFunction() != null){
				clearLoggedScores();
			}
		}
	}
	
	public double getTrainMetricScore() {
		if(conf != null && conf.hasItem(BasicConfiguration.AVG_SCORE) && AppUtility.isNumber(conf.getItem(BasicConfiguration.AVG_SCORE)))
			return Double.valueOf(conf.getItem(BasicConfiguration.AVG_SCORE));
		else return Double.NaN;
	}
	
	public Metric getTrainMetric() {
		if(conf != null && conf.hasItem(BasicConfiguration.TRAIN_METRIC))
			return Metric.fromString(conf.getItem(BasicConfiguration.TRAIN_METRIC), "absolute");
		else return null;
	}	
	
	protected DecisionFunction buildClassifier(ValueSeries vs, boolean revertFlag) {
		if(conf != null && conf.hasItem(BasicConfiguration.THRESHOLD))
			return DecisionFunction.buildDecisionFunction(vs, conf.getItem(BasicConfiguration.THRESHOLD), revertFlag);
		else return null;
	}
	
	protected DecisionFunction buildClassifier(String dFunctionString, ValueSeries vs, boolean revertFlag) {
		return DecisionFunction.buildDecisionFunction(vs, dFunctionString, revertFlag);
	}
	
	protected void setDecisionFunction(String dFunctionString, ValueSeries vs, boolean revertFlag) {
		decisionFunction = DecisionFunction.buildDecisionFunction(vs, dFunctionString, revertFlag);
	}
	
	public DecisionFunction setDecisionFunction(String dFunctionString){
		if(loggedAnomalyScores != null && loggedAnomalyScores.size() > 0)
			decisionFunction = buildClassifier(dFunctionString, loggedAnomalyScores, true);
		else decisionFunction = buildClassifier(dFunctionString, loggedScores, false);
		return decisionFunction;
	}
	
	protected void setDecisionFunction(){
		if(loggedAnomalyScores != null && loggedAnomalyScores.size() > 0)
			setDecisionFunction(loggedAnomalyScores, true);
		else setDecisionFunction(loggedScores, false);
	}
	
	public DecisionFunction getDecisionFunction(){
		if(decisionFunction == null){
			setDecisionFunction();
		}
		return decisionFunction;
	}
	
	protected void setDecisionFunction(ValueSeries vs, boolean flag){
		decisionFunction = buildClassifier(vs, flag);
	}
	
	protected void logScore(double score, boolean anomaly){
		loggedScores.addValue(score);
		if(anomaly)
			loggedAnomalyScores.addValue(score);
	}
	
	protected void logScores(List<? extends LabelledValue> list) {
		for(LabelledValue lv : list){
			logScore(lv.getValue(), lv.getLabel());
		}
	}
	
	public void clearLoggedScores() {
		loggedScores.clear();
		loggedAnomalyScores.clear();
	}
	
	/**
	 * Converts a double score into a 0-1 one.
	 *
	 * @param anomalyValue the anomaly value
	 * @return the double
	 */
	public static double convertResultIntoDouble(AnomalyResult anomalyResult){
		switch (anomalyResult){
			case ANOMALY:
				return 1.0;
			case NORMAL:
				return 0.0;
			case MAYBE:
				return Double.NaN;
			case UNKNOWN:
				return 0.0;
			default:
				return Double.NaN;
		}
	}
	
	/**
	 * Builds a DetectionAlgorithm.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 * @return the detection algorithm
	 */
	public static DetectionAlgorithm buildAlgorithm(LearnerType algType, DataSeries dataSeries, BasicConfiguration conf) {
		if(algType != null){
			if(algType instanceof MetaLearner){
				return buildMetaAlgorithm((MetaLearner)algType, dataSeries, conf);
			} else return buildBaseAlgorithm((BaseLearner)algType, dataSeries, conf);
		} else return null;
	}
	
	private static DetectionAlgorithm buildBaseAlgorithm(BaseLearner algType, DataSeries dataSeries, BasicConfiguration conf) {
		switch(algType.getAlgType()){
			case HBOS:
				return new HBOSDetectionAlgorithm(dataSeries, conf);
			case DBSCAN:
				return new DBSCANDetectionAlgorithm(dataSeries, conf);
			case LDCOF_KMEANS:
				return new LDCOFKMeansDetectionAlgorithm(dataSeries, conf);
			case LDCOF_DBSCAN:
				return new LDCOFDBSCANDetectionAlgorithm(dataSeries, conf);
			case SDO:
				return new SDODetectionAlgorithm(dataSeries, conf);
			case ELKI_SOS:
				return new SOSELKI(dataSeries, conf);
			case ELKI_ISOS:
				return new ISOSELKI(dataSeries, conf);
			case ELKI_KMEANS:
				return new KMeansELKI(dataSeries, conf);
			case ELKI_GMEANS:
				return new GMeansELKI(dataSeries, conf);
			case ELKI_ABOD:
				return new ABODELKI(dataSeries, conf);
			case ELKI_FASTABOD:
				return new FastABODELKI(dataSeries, conf);
			case ELKI_LOF:
				return new LOFELKI(dataSeries, conf);
			case ELKI_COF:
				return new COFELKI(dataSeries, conf);
			case ELKI_KNN:
				return new KNNELKI(dataSeries, conf);
			case ELKI_ODIN:
				return new ODINELKI(dataSeries, conf);
			case ELKI_SVM:
				return new SVMELKI(dataSeries, conf);
			case WEKA_ISOLATIONFOREST:
				return new IsolationForestWEKA(dataSeries, conf);
			case SOM:
				return new SOMDetectionAlgorithm(dataSeries, conf);		
		}
		return null;
	}
	
	public static DetectionAlgorithm buildMetaAlgorithm(MetaLearner ml, DataSeries dataSeries, BasicConfiguration conf) {
		switch(ml.getMetaType()){
			case BAGGING:
				return new BaggingMetaLearner(dataSeries, conf);
			case BOOSTING:
				return new BoostingMetaLearner(dataSeries, conf);
			case CASCADE_GENERALIZATION:
				return new CascadeGeneralizationMetaLearner(dataSeries, conf);
			case CASCADING:
				return new CascadingMetaLearner(dataSeries, conf);
			case DELEGATING:
				return new DelegatingMetaLearner(dataSeries, conf);
			case STACKING:
				return new StackingMetaLearner(dataSeries, conf);
			case STACKING_FULL:
				return new FullStackingMetaLearner(dataSeries, conf);
			case VOTING:
				return new VotingMetaLearner(dataSeries, conf);
			case WEIGHTED_VOTING:
				return new WeightedVotingMetaLearner(dataSeries, conf);
			default:
				break;
		}
		return null;
	}
	
	public static List<AlgorithmFamily> getFamily(LearnerType lType) {
		List<AlgorithmFamily> afl = new LinkedList<AlgorithmFamily>();
		if(lType instanceof BaseLearner){
			switch(((BaseLearner) lType).getAlgType()){
				case ELKI_SOS:
				case ELKI_ISOS:
				case HBOS:
					afl.add(AlgorithmFamily.STATISTICAL);
				default:
					break;
			}
			switch(((BaseLearner) lType).getAlgType()){
				case ELKI_KMEANS:
				case ELKI_GMEANS:
				case DBSCAN:
				case LDCOF_KMEANS:
				case LDCOF_DBSCAN:
					afl.add(AlgorithmFamily.CLUSTERING);
				default:
					break;
			}
			switch(((BaseLearner) lType).getAlgType()){
				case ELKI_ABOD:
				case ELKI_FASTABOD:
					afl.add(AlgorithmFamily.ANGLE);
				default:
					break;
			}
			switch(((BaseLearner) lType).getAlgType()){
				case SDO:
				case ELKI_LOF:
				case ELKI_COF:
				case LDCOF_KMEANS:
				case LDCOF_DBSCAN:
					afl.add(AlgorithmFamily.DENSITY);
				default:
					break;
			}
			switch(((BaseLearner) lType).getAlgType()){
				case ELKI_KNN:
				case ELKI_ODIN:
				case ELKI_FASTABOD:
				case ELKI_LOF:
				case ELKI_COF:
				case ELKI_ISOS:
					afl.add(AlgorithmFamily.NEIGHBOUR);
				default:
					break;
			}
			switch(((BaseLearner) lType).getAlgType()){
				case ELKI_SVM:
				case WEKA_ISOLATIONFOREST:
					afl.add(AlgorithmFamily.CLASSIFICATION);
				default:
					break;
			}
			switch(((BaseLearner) lType).getAlgType()){
				case SOM:
					afl.add(AlgorithmFamily.NEURAL_NETWORK);
				default:
					break;
			}
		} else afl.add(AlgorithmFamily.META);
		return afl;
	}
	
	private boolean usesSimpleSeries(DataSeries container, DataSeries serie) {
		if(container == null){
			return false;
		} else {
			return container.contains(serie);
		}
	}
	
	public boolean usesSeries(DataSeries serie) {
		boolean out = false;
		if(serie != null) {
			for(DataSeries ds : serie.listSubSeries()){
				out = out || usesSimpleSeries(getDataSeries(), ds);		
			}
		}
		return out;
	}
	
	/**
	 * Defines the anomaly rate of a given snapshot.
	 * @param knowledge 
	 *
	 * @param sysSnapshot the given snapshot
	 * @return the anomaly rate of the snapshot
	 */
	public AlgorithmResult snapshotAnomalyRate(Knowledge knowledge, int currentIndex){
		if(getDecisionFunction() == null)
			setDecisionFunction();
		return evaluateSnapshot(knowledge, currentIndex);//*getWeight();
	}
	
	/**
	 * Evaluates a snapshot.
	 *
	 * @param sysSnapshot the snapshot
	 * @param knowledge 
	 * @return the result of the evaluation
	 */
	public AlgorithmResult evaluateSnapshot(Knowledge knowledge, int currentIndex) {
		AlgorithmResult ar;
		ObjectPair<Double, Object> score;
		Snapshot dsSnap = knowledge.get(currentIndex, getDataSeries());
		double[] snapArray = getSnapValueArray(dsSnap);
		if(dsSnap != null && snapArray != null && checkCalculationCondition(snapArray)){
			boolean isUnknown = dsSnap.getInjectedElement() != null && dsSnap.getInjectedElement().isUnknown();
			score = calculateSnapshotScore(snapArray);
			if(getLearnerType() instanceof BaseLearner){
				AlgorithmType at = ((BaseLearner)getLearnerType()).getAlgType(); 
				if(at == AlgorithmType.DBSCAN) {
					ar = new DBSCANResult(dsSnap.getInjectedElement() != null, score.getKey(), (Double)score.getValue(), getConfidence(score.getKey()), isUnknown);
				} else if(score.getValue() != null && score.getValue() instanceof KMeansModel){
					KMeansModel kms = (KMeansModel)score.getValue();
					ar = new KMeansResult(dsSnap.getInjectedElement() != null, score.getKey(), kms.getVarianceContribution(), getConfidence(score.getKey()), isUnknown);
				} else ar = new AlgorithmResult(dsSnap.getInjectedElement() != null, score.getKey(), getConfidence(score.getKey()), score.getValue(), isUnknown);
			} else ar = new AlgorithmResult(dsSnap.getInjectedElement() != null, score.getKey(), getConfidence(score.getKey()), score.getValue(), isUnknown);
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} else return AlgorithmResult.error(dsSnap.getInjectedElement() != null);
	}
	
	protected double[] getSnapValueArray(Snapshot snap){
		return snap.getDoubleValues();
	}

	protected abstract boolean checkCalculationCondition(double[] snapArray);
	
	/**
	 * Prints the results of the detection.
	 *
	 * @param typeTag the output type tag
	 * @param outFolderName the output folder name
	 * @param expTag the experiment tag
	 */
	public void printResults(String typeTag, String outFolderName, String expTag){
		if(typeTag.equalsIgnoreCase("TEXT"))
			printTextResults(outFolderName, expTag);
		else if(typeTag.equalsIgnoreCase("IMAGE"))
			printImageResults(outFolderName, expTag);
		else if(!typeTag.equalsIgnoreCase("BASIC") && !typeTag.equalsIgnoreCase("UI")){
			AppLogger.logError(getClass(), "OutputTypeError", "Unable to recognize chosen output type");
		}
	}
	
	/**
	 * Gets the weight of the algorithm.
	 *
	 * @return the weight
	 */
	protected Double getWeight(){
		if(conf != null && conf.getItem(BasicConfiguration.WEIGHT) != null)
			return Double.valueOf(conf.getItem(BasicConfiguration.WEIGHT));
		else return 1.0;
	}

	/**
	 * Prints the image results.
	 *
	 * @param outFolderName the out folder name
	 * @param expTag the exp tag
	 */
	protected abstract void printImageResults(String outFolderName, String expTag);

	/**
	 * Prints the text results.
	 *
	 * @param outFolderName the out folder name
	 * @param expTag the exp tag
	 */
	protected abstract void printTextResults(String outFolderName, String expTag);
	
	/**
	 * Gets the algorithm type.
	 *
	 * @return the algorithm type
	 */
	public LearnerType getLearnerType() {
		return conf.getLearnerType();
	}

	/**
	 * Gets the configuration.
	 *
	 * @return the configuration
	 */
	public BasicConfiguration getConfiguration() {
		return conf;
	}

	/**
	 * Gets the data series.
	 *
	 * @return the data series
	 */
	public DataSeries getDataSeries() {
		return dataSeries;
	}

	public static boolean isSliding(LearnerType algType) {
		return algType instanceof BaseLearner && ((BaseLearner)algType).getAlgType().toString().contains("SLIDING");
	}

	public static String getFullName(AlgorithmType algType) {
		switch(algType){
			case ELKI_ABOD:
				return "ABOD: Angle-Based Outlier Factor (ELKI)";
			case ELKI_LOF:
				return "LOF: Local Outlier Factor (ELKI)";
			case ELKI_COF:
				return "COF: Connectivity-based Outlier Factor (ELKI)";
			case ELKI_FASTABOD:
				return "FastABOD: Fast Angle-Based Outlier Factor (ELKI)";
			case ELKI_KMEANS:
				return "K-Means (ELKI)";
			case ELKI_GMEANS:
				return "G-Means (builds on K-Means from ELKI)";
			case LDCOF_KMEANS:
				return "LDCOF: Local Density-based Connectivity Outlier Factor (embeds KMeans)";
			case ELKI_SVM:
				return "SVM: One-Class Support Vector Machines (ELKI)";
			case HBOS:
				return "HBOS: Histogram-based Outlier Score";
			case ELKI_ODIN:
				return "ODIN: outlier Detection using Indegree Number (ELKI)";
			case WEKA_ISOLATIONFOREST:
				return "iForest: Isolation Forest (WEKA)";
			case DBSCAN:
				return "DBSCAN: Density-Based";
			case LDCOF_DBSCAN:
				return "LDCOF: Local Density-based Connectivity Outlier Factor (embeds DBSCAN)";
			case SDO:
				return "SDO: Stochastic Density Outlier";
			default:
				return algType.toString();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + dataSeries.getName();
	}
	
	public static String explainAlgorithm(LearnerType algType) {
		if(algType instanceof BaseLearner){
			switch(((BaseLearner)algType).getAlgType()){
				case ELKI_ABOD:
					return "Angle-based algorithm. <br>"
							+ "Angles between the new data point and all the possible couples of data points of <br>"
							+ "the training set are calculated, and then their variance (ABOF) is calsulated. <br>"
							+ "The smaller the ABOF, the greater the anomality of the data point.";
				case ELKI_SOS:
					return "Stochastic Outlier Selection (SOS), a Statistical algorithm. <br>"
							+ "It takes as input either a feature matrix or a dissimilarity matrix and outputs for each data point an outlier probability. <br>"
							+ "Intuitively, a data point is considered to be an outlier when the other data points have insufficient affinity with it.";
				case ELKI_ISOS:
					return "Fast version of SOS (Intrinsic Stochastic Outlier Selection), calculating dissimilarity with the kNN of the data point.";
				case ELKI_LOF:
					return "Local Outlier Factor algorithm. <br>"
							+ "LOF is based on a concept of a local density, where locality is given by kNN, <br>"
							+ "whose distance with respect to the data point is used to estimate the density. <br>"
							+ "The lower the density, the greater the anomality of the data point.";
				case ELKI_COF:
					return "Connectivity-based Outlier Factor algorithm. <br>"
							+ "COF computes the connectivity-based outlier factor for data points through the comparison <br>"
							+ "of chaining-distances between data points subject to neighboring observations. <br>"
							+ "The greater the COF, the greater the anomality of the data point.";
				case ELKI_FASTABOD:
					return "Fast (Quadratic) version of ABOD, considering agnles with the kNN of the data point.";
				case ELKI_KMEANS:
					return "Classic clustering algorithm, given the K number of clusters. <br>"
							+ "During training, clusters are created. Then, a new data point is scored as anomalous if it is too far <br>"
							+ "from the nearest cluster, evaluated using Euclidean Distance.";
				case ELKI_GMEANS:
					return "Classic clustering algorithm, automatically finds the optimal K number of clusters. <br>"
							+ "During training, clusters are created. Then, a new data point is scored as anomalous if it is too far <br>"
							+ "from the nearest cluster, evaluated using Euclidean Distance.";
				case ELKI_SVM:
					return "Support Vector Machines. <br>"
							+ "Support vectors are the data points that lie closest to the decision surface (or hyperplane), <br>"
							+ "and have direct bearing on the optimum location of the decision surface. <br>"
							+ "The algorithm tries to reconstruct such hyperplane that differentiates between normal and anomalous data. <br>"
							+ "The score reprsent the distance with respect to the boundary: big negative distances represent anomalies. ";
				case HBOS:
					return "Generates histograms for each feature that describes the frequence of occurrence of specific values. <br> "
							+ "Then, a data point is scored with an high HBOS is falls in an area of the histogram with short bars.";		
				case ELKI_KNN:	
					return "Unsupervised k-th Nearest Neighbour Algorithm. <br>"
							+ "Calculates the knn score as the distance to its k-th neighbour";
				case ELKI_ODIN:	
					return "Outlier Detection using Indegree Number. <br>"
							+ "Calculates the odin according to the KNN graph. <br>"
							+ "The lower the ODIN, the higher the probability of anomaly.";
				case WEKA_ISOLATIONFOREST:
					return "Creates a forest of Isolation Trees, which are evaluated as an ensemble.";
				case DBSCAN:
					return "DBSCAN is based on this intuitive notions of 'clusters' and 'radius'. <br>"
							+ "The key idea is that for each point of a cluster, the neighborhood of a given radius has to contain at least a minimum number of points.";
				case LDCOF_KMEANS:
				case LDCOF_DBSCAN:
					return "Local Density Cluster-Based Outlier Factor (LDCOF). <br>"
							+ "The LDCOF score is defined as the distance to the nearest large cluster divided by the average distance of each element <br>"
							+ "to the center of the large cluster. When small clusters are considered anomalous, the elements inside the small clusters <br>"
							+ "are assigned to the nearest large cluster which becomes its local neighborhood. <br>"
							+ "It can be istantiated with any clustering algorithm, in RELOAD with KMeans or DBSCAN.";
				case SDO:
					return "Sparse Data Observers (SDO). <br>"
							+ "SDO builds a low density data model formed by observers. <br> "
							+ "An observer is a data object placed within the data mass and ideally equidistant to other observers within the same cluster. <br>"
							+ "The outlierness of a data object is evaluated based on the distance to its x-closest observers.";
				case SOM:
					return "Self-Organizing Maps (SOM). <br>"
							+ "Single-hidden layer SOM, able to perform binary classification and training its hidden layer through subsequent refinements.";
				default:
					return "Algorithms' details not available.";
			}
		} else return "metalearner";
	}
	
	public static String explainParameters(LearnerType algType) {
		String base = "Parameters: ";
		if(algType instanceof BaseLearner){
			switch(((BaseLearner)algType).getAlgType()){
				case ELKI_ABOD:
					return base;
				case ELKI_LOF:
				case ELKI_COF:
				case ELKI_FASTABOD:
					return base + "(k) the number of neighbours.";
				case ELKI_GMEANS:
					return "no parameters.";
				case ELKI_KMEANS:
					return base + "(k) the number of clusters.";
				case LDCOF_KMEANS:
					return base + "(k) the number of clusters, <br>"
							+ "(gamma) the LDCOF parameter to separate small/large clusters";
				case ELKI_SVM:
					return base + "(kernel) the type of kernel, <br>"
							+ "(nu) an upper bound on the fraction of margin errors and a lower bound <br>"
							+ " of the fraction of support vectors relative to training set <br>"
							+ "e.g., nu=0.05 guarantees at most 5% of training examples being misclassified <br>"
							+ " and at least 5% of training examples being support vectors.";
				case HBOS:
					return base + "(k) the number of histograms to generate for each indicator.";
				case ELKI_KNN:
				case ELKI_ODIN:
				case WEKA_ISOLATIONFOREST:
					return "Parameters: (ntrees) number of trees in the forest, <br>"
							+ "(sample_size) instances to be sampled to train each tree.";
				case DBSCAN:
					return base + "(eps) defines the radius of neighborhood around a data point, <br>"
							+ "(pts) is the minimum number of neighbors within 'eps' radius";
				case LDCOF_DBSCAN:
					return base + "(eps) defines the radius of neighborhood around a data point, <br>"
							+ "(pts) is the minimum number of neighbors within 'eps' radius, <br>"
							+ "(gamma) the LDCOF parameter to separate small/large clusters";
				case SDO:
					return base + "(k) the amount of observers <br>"
							+ "(q) the 'observation threshold' to derive observers (% of training set size), <br>"
							+ "(x) the amount of 'closest' observers";
				case ELKI_SOS:
					return base + "(h) the minimum number of 'affine' data points";
				case ELKI_ISOS:
					return base + "(k) the number of neighbours <br>"
							+ "(phi) the ratio of items wrt the size of the training set to consider 'affine' data points.";
				case SOM:
					return base + "(min_alpha) minimum acceptable value for alpha <br>"
							+ "(decay) the decay speed of alpha until it reaches min_alpha <br>"
							+ "(alpha) the learning coefficient (0 <= alpha <= 1).";
				default:
					return "Parameters are shown in the table.";
			}
		} else return "metalearner";
	}
	
	public static List<AlgorithmType> availableAlgorithms(){
		List<AlgorithmType> types = new LinkedList<AlgorithmType>();
		types.addAll(Arrays.asList(AlgorithmType.values()));
		return types;
	}

	public abstract Map<String, String[]> getDefaultParameterValues();
	
	public double getConfidence(double algorithmScore){
		if(decisionFunction != null)
			return decisionFunction.calculateConfidence(algorithmScore);
		else {
			setDecisionFunction();
			if(decisionFunction != null)
				return decisionFunction.calculateConfidence(algorithmScore);
			else return Double.NaN;
		}
	}
	
	public static String[] extractLabels(boolean includeFaulty, List<Snapshot> kSnapList) {
		int insertIndex = 0;
		String[] anomalyLabels;
		if(includeFaulty)
			anomalyLabels = new String[kSnapList.size()];
		else anomalyLabels = new String[Knowledge.goldenPointsSize(kSnapList)]; 
		if(anomalyLabels.length > 0) {
			for(int i=0;i<kSnapList.size();i++){
				if(includeFaulty || !includeFaulty && kSnapList.get(i).getInjectedElement() == null) {
					anomalyLabels[insertIndex] = kSnapList.get(i).getInjectedElement() == null ? "no" : "yes";
					insertIndex++;
				}
			}
		}
		return anomalyLabels;
	}

	public ValueSeries getLoggedScores() {
		return loggedScores;
	}
	
	public String getMainConfString(){
		String confString = "";
		for(String key : getDefaultParameterValues().keySet()){
			confString = confString + " " + key + ":" + conf.getItem(key);
		}
		return confString;
	}
	
	public static AlgorithmType isAlgorithm(String value){
		try {
			return AlgorithmType.valueOf(value.trim());
		} catch(Exception ex){
			return null;
		}
	}

	public static AlgorithmComplexity getMemoryComplexity(AlgorithmType algType) {
		if(algType != null){
			switch(algType){
				case ELKI_ABOD:
					return AlgorithmComplexity.CUBIC;
				case ELKI_FASTABOD:
				case DBSCAN:
				case LDCOF_DBSCAN:
				case ELKI_COF:
					return AlgorithmComplexity.QUADRATIC;
				default: 
					return AlgorithmComplexity.LINEAR;
			}
		} else return null;
	}
	
	/**
	 * Gets the filename used to store data about scores and histograms.
	 *
	 * @return the filename
	 */
	protected String getFilename(){
		String folder = getDefaultTmpFolder() + File.separatorChar;
		if(!new File(folder).exists())
			new File(folder).mkdirs();
		return folder + getDataSeries().getCompactString().replace("\\", "_").replace("/", "-").replace("*", "_") + "." + getLearnerType().toString().toLowerCase();
	}
	
	/**
	 * Gets the default folder used to store temporary data.
	 *
	 * @return the default temporary folder
	 */
	protected String getDefaultTmpFolder(){
		if(conf.hasItem(BasicConfiguration.DATASET_NAME) && conf.getItem(BasicConfiguration.DATASET_NAME).length() > 0){
			if(conf.hasItem(TAG))
				return "tmp" + File.separatorChar + conf.getItem(BasicConfiguration.DATASET_NAME) + File.separatorChar + conf.getItem(TAG);
			else if(getLearnerType() instanceof BaseLearner)
				return "tmp" + File.separatorChar + conf.getItem(BasicConfiguration.DATASET_NAME) + File.separatorChar + getLearnerType().toString();
			else return "tmp" + File.separatorChar + conf.getItem(BasicConfiguration.DATASET_NAME);
		} else {
			if(conf.hasItem(TAG))
				return "tmp" + File.separatorChar + conf.getItem(TAG);
			else if(getLearnerType() instanceof BaseLearner)
				return "tmp" + File.separatorChar + getLearnerType().toString();
			else return "tmp";
		}
	}
	
	public void loadLoggedScores() {
		BufferedReader reader;
		String readed;
		try {
			loggedScores = new ValueSeries();		
			loggedAnomalyScores = new ValueSeries();	
			if(new File(getScoresFilename()).exists()){
				reader = new BufferedReader(new FileReader(new File(getScoresFilename())));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null && !readed.startsWith("*") && readed.contains(";")){
						readed = readed.trim();
						if(readed.contains(";")){
							boolean flag = Boolean.valueOf(readed.split(";")[1]);
							double score = Double.parseDouble(readed.split(";")[0]);
							if(flag)
								loggedAnomalyScores.addValue(score);
							else loggedScores.addValue(score);
						} else loggedScores.addValue(Double.parseDouble(readed));
					}
				}
				reader.close();
			} else AppLogger.logError(getClass(), "NoLoggedScoresError", "Unable to find logged scores in '" + getScoresFilename() + "'");
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read logged scores file");
		} 
	}
	
	public void saveLoggedScores() {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(getScoresFilename()));
			writer.write("score;anomaly\n");
			if(loggedScores != null && loggedScores.size() > 0){
				for(Double d : loggedScores.getValues()){
					writer.write(d + ";false\n");
				}
			}
			if(loggedAnomalyScores != null && loggedAnomalyScores.size() > 0){
				for(Double d : loggedAnomalyScores.getValues()){
					writer.write(d + ";true\n");
				}
			}
			writer.close();
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write logged scores file");
		} 
	}
	
	protected String getScoresFilename(){
		String base = getFilename();
		base = base.substring(0, base.indexOf("."));
		base = base + "_logged_scores." + getLearnerType().toString().toLowerCase();
		return base;
	}

	@Override
	public boolean automaticTraining(List<Knowledge> kList) {
		boolean trainOut;
		trainOut = automaticInnerTraining(kList);
		if(trainOut){
			ValueSeries vs = new ValueSeries(getTrainScores());
			if(vs.size() > 0)
				setDecisionFunction("IQR", vs, false);
			else setDecisionFunction("STATIC_THRESHOLD_GREATERTHAN(1)", vs, false);
			
			clearLoggedScores();
			for(Knowledge know : kList){
				for(int i=0;i<know.size();i++){
					Snapshot snap = know.buildSnapshotFor(i, getDataSeries());
					AlgorithmResult ar = evaluateSnapshot(know, i);
					logScore(ar.getScore(), snap.isAnomalous());
				}
			}
			
			conf.addItem(TMP_FILE, getFilename());		    
		    storeAdditionalPreferences();
		    
		} else AppLogger.logError(getClass(), "UnvalidDataSeries", "Unable to apply " + getLearnerType() + " to dataseries " + getDataSeries().getName());
		return trainOut;
	}

	public abstract ObjectPair<Double, Object> calculateSnapshotScore(double[] snapArray);

	/**
	 * Stores additional preferences (if any).
	 */
	protected abstract void storeAdditionalPreferences();
	
	public abstract List<Double> getTrainScores();
	
	public abstract boolean automaticInnerTraining(List<Knowledge> kList);
}
