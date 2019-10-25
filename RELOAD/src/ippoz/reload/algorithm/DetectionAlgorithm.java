/**
 * 
 */
package ippoz.reload.algorithm;

import ippoz.reload.algorithm.custom.DBSCANDetectionAlgorithm;
import ippoz.reload.algorithm.custom.HBOSDetectionAlgorithm;
import ippoz.reload.algorithm.custom.LDCOFDBSCANDetectionAlgorithm;
import ippoz.reload.algorithm.custom.LDCOFKMeansDetectionAlgorithm;
import ippoz.reload.algorithm.elki.ABODELKI;
import ippoz.reload.algorithm.elki.COFELKI;
import ippoz.reload.algorithm.elki.FastABODELKI;
import ippoz.reload.algorithm.elki.ISOSELKI;
import ippoz.reload.algorithm.elki.KMeansELKI;
import ippoz.reload.algorithm.elki.KNNELKI;
import ippoz.reload.algorithm.elki.LOFELKI;
import ippoz.reload.algorithm.elki.ODINELKI;
import ippoz.reload.algorithm.elki.SOSELKI;
import ippoz.reload.algorithm.elki.SVMELKI;
import ippoz.reload.algorithm.elki.sliding.ABODSlidingELKI;
import ippoz.reload.algorithm.elki.sliding.COFSlidingELKI;
import ippoz.reload.algorithm.elki.sliding.KMeansSlidingELKI;
import ippoz.reload.algorithm.elki.sliding.KNNSlidingELKI;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.sliding.SPSSlidingAlgorithm;
import ippoz.reload.algorithm.weka.IsolationForestSlidingWEKA;
import ippoz.reload.algorithm.weka.IsolationForestWEKA;
import ippoz.reload.commons.algorithm.AlgorithmFamily;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.ComplexDataSeries;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.service.StatPair;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.LabelledValue;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Class DetectionAlgorithm.
 *
 * @author Tommy
 */
public abstract class DetectionAlgorithm {
	
	/** The configuration. */
	protected AlgorithmConfiguration conf;
	
	protected ValueSeries loggedScores;
	
	protected ValueSeries loggedAnomalyScores;
	
	protected DecisionFunction decisionFunction;
	
	/**
	 * Instantiates a new detection algorithm.
	 *
	 * @param conf the configuration
	 */
	public DetectionAlgorithm(AlgorithmConfiguration conf){
		this.conf = conf;
		loggedScores = new ValueSeries();
		loggedAnomalyScores = new ValueSeries();
		decisionFunction = null;
	}
	
	protected DecisionFunction buildClassifier(ValueSeries vs, boolean revertFlag) {
		if(conf != null && conf.hasItem(AlgorithmConfiguration.THRESHOLD))
			return DecisionFunction.buildDecisionFunction(vs, conf.getItem(AlgorithmConfiguration.THRESHOLD), revertFlag);
		else return null;
	}
	
	protected DecisionFunction buildClassifier(String dFunctionString, ValueSeries vs, boolean revertFlag) {
		return DecisionFunction.buildDecisionFunction(vs, dFunctionString, revertFlag);
	}
	
	protected void setDecisionFunction(String dFunctionString, ValueSeries vs, boolean revertFlag) {
		decisionFunction = DecisionFunction.buildDecisionFunction(vs, dFunctionString, revertFlag);
	}
	
	public void setDecisionFunction(String dFunctionString){
		if(loggedAnomalyScores != null && loggedAnomalyScores.size() > 0)
			decisionFunction = buildClassifier(dFunctionString, loggedAnomalyScores, true);
		else decisionFunction = buildClassifier(dFunctionString, loggedScores, false);
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
	public static DetectionAlgorithm buildAlgorithm(AlgorithmType algType, DataSeries dataSeries, AlgorithmConfiguration conf) {
		switch(algType){
			case HBOS:
				return new HBOSDetectionAlgorithm(dataSeries, conf);
			case DBSCAN:
				return new DBSCANDetectionAlgorithm(dataSeries, conf);
			case LDCOF_KMEANS:
				return new LDCOFKMeansDetectionAlgorithm(dataSeries, conf);
			case LDCOF_DBSCAN:
				return new LDCOFDBSCANDetectionAlgorithm(dataSeries, conf);
			case ELKI_SOS:
				return new SOSELKI(dataSeries, conf);
			case ELKI_ISOS:
				return new ISOSELKI(dataSeries, conf);
			case ELKI_KMEANS:
				return new KMeansELKI(dataSeries, conf);
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
			case SLIDING_SPS:
				return new SPSSlidingAlgorithm(dataSeries, conf);
			case SLIDING_ELKI_ABOD:
				return new ABODSlidingELKI(dataSeries, conf);
			case SLIDING_ELKI_CLUSTERING:
				return new KMeansSlidingELKI(dataSeries, conf);
			case SLIDING_ELKI_COF:
				return new COFSlidingELKI(dataSeries, conf);
			case SLIDING_ELKI_KNN:
				return new KNNSlidingELKI(dataSeries, conf);
			case SLIDING_WEKA_ISOLATIONFOREST:
				return new IsolationForestSlidingWEKA(dataSeries, conf);		
		}
		return null;
	}
	
	public static List<AlgorithmFamily> getFamily(AlgorithmType algType) {
		List<AlgorithmFamily> afl = new LinkedList<AlgorithmFamily>();
		switch(algType){
			case ELKI_SOS:
			case ELKI_ISOS:
			case HBOS:
			case SLIDING_SPS:
				afl.add(AlgorithmFamily.STATISTICAL);
			default:
				break;
		}
		switch(algType){
			case ELKI_KMEANS:
			case SLIDING_ELKI_CLUSTERING:
			case DBSCAN:
			case LDCOF_KMEANS:
			case LDCOF_DBSCAN:
				afl.add(AlgorithmFamily.CLUSTERING);
			default:
				break;
		}
		switch(algType){
			case ELKI_ABOD:
			case ELKI_FASTABOD:
			case SLIDING_ELKI_ABOD:
				afl.add(AlgorithmFamily.ANGLE);
			default:
				break;
		}
		switch(algType){
			case ELKI_LOF:
			case ELKI_COF:
			case LDCOF_KMEANS:
			case LDCOF_DBSCAN:
			case SLIDING_ELKI_COF:
				afl.add(AlgorithmFamily.DENSITY);
			default:
				break;
		}
		switch(algType){
			case ELKI_KNN:
			case ELKI_ODIN:
			case SLIDING_ELKI_KNN:
			case ELKI_FASTABOD:
			case ELKI_LOF:
			case ELKI_COF:
			case ELKI_ISOS:
				afl.add(AlgorithmFamily.NEIGHBOUR);
			default:
				break;
		}
		switch(algType){
			case ELKI_SVM:
			case WEKA_ISOLATIONFOREST:
			case SLIDING_WEKA_ISOLATIONFOREST:
				afl.add(AlgorithmFamily.CLASSIFICATION);
			default:
				break;
		}
		return afl;
	}
	
	public static KnowledgeType getKnowledgeType(AlgorithmType algType) {
		if(algType.name().toUpperCase().contains("SLIDING"))
			return KnowledgeType.SLIDING;
		else return KnowledgeType.SINGLE;
	}
	
	/**
	 * Builds a DetectionAlgorithm.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 * @return the detection algorithm
	 */
	public static DetectionAlgorithm buildAlgorithm(DataSeries dataSeries, AlgorithmType algType, String[] splitted) {
		AlgorithmConfiguration conf = new AlgorithmConfiguration(algType);
		return buildAlgorithm(algType, dataSeries, conf);
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
				if(getDataSeries() instanceof ComplexDataSeries){
					out = out || usesSimpleSeries(((ComplexDataSeries)getDataSeries()).getFirstOperand(), ds) || usesSimpleSeries(((ComplexDataSeries)getDataSeries()).getSecondOperand(), ds);
				} else out = out || usesSimpleSeries(getDataSeries(), ds);		
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
	
	// TODO
	
	/**
	 * Evaluates a snapshot.
	 *
	 * @param sysSnapshot the snapshot
	 * @param knowledge 
	 * @return the result of the evaluation
	 */
	public abstract AlgorithmResult evaluateSnapshot(Knowledge knowledge, int currentIndex);
	
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
		else if(!typeTag.equalsIgnoreCase("NULL")){
			AppLogger.logError(getClass(), "OutputTypeError", "Unable to recognize chosen output type");
		}
	}
	
	/**
	 * Gets the weight of the algorithm.
	 *
	 * @return the weight
	 */
	protected Double getWeight(){
		if(conf != null && conf.getItem(AlgorithmConfiguration.WEIGHT) != null)
			return Double.valueOf(conf.getItem(AlgorithmConfiguration.WEIGHT));
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
	 * Evaluates a value.
	 *
	 * @param value the value
	 * @param stats the stats
	 * @param varTimes the var times
	 * @return the double
	 */
	protected double evaluateValue(Double value, StatPair stats, double varTimes){
		if(value >= (stats.getAvg() - varTimes*stats.getStd()) && value <= (stats.getAvg() + varTimes*stats.getStd()))
			return 0.0;
		else return 1.0;
	}
	
	/**
	 * Evaluates absolute difference.
	 *
	 * @param value the value
	 * @param stats the stats
	 * @param varTimes the tolerance (the range is defined by std*tolerance)
	 * @return the evaluation
	 */
	protected double evaluateAbsDiff(Double value, StatPair stats, double varTimes){
		double outVal = Math.abs(value - stats.getAvg());
		outVal = outVal - varTimes*stats.getStd();
		if(outVal < 0)
			return 0.0;
		else return outVal;
	}
	
	/**
	 * Evaluate absolute difference rate.
	 *
	 * @param value the value
	 * @param stats the stats
	 * @param varTimes the tolerance (the range is defined by std*tolerance)
	 * @return the evaluation
	 */
	protected double evaluateAbsDiffRate(Double value, StatPair stats, double varTimes){
		double outVal = Math.abs(value - stats.getAvg());
		outVal = outVal - varTimes*stats.getStd();
		if(outVal <= 0 || stats.getAvg() == 0.0)
			return 0.0;
		else return outVal/stats.getAvg();
	}
	
	/**
	 * Evaluate over diff.
	 *
	 * @param value the value
	 * @param stats the stats
	 * @return the evaluation
	 */
	protected double evaluateOverDiff(Double value, StatPair stats){
		double outVal = value - (stats.getAvg() + stats.getStd());
		if(outVal < 0)
			return 0.0;
		else return outVal;
	}
	
	/**
	 * Gets the algorithm type.
	 *
	 * @return the algorithm type
	 */
	public AlgorithmType getAlgorithmType() {
		return conf.getAlgorithmType();
	}

	/**
	 * Gets the configuration.
	 *
	 * @return the configuration
	 */
	public AlgorithmConfiguration getConfiguration() {
		return conf;
	}

	/**
	 * Gets the data series.
	 *
	 * @return the data series
	 */
	public abstract DataSeries getDataSeries();

	public ValueSeries getTrainScore() {
		return loggedScores;
	}

	public static boolean isSliding(AlgorithmType algType) {
		return algType.toString().contains("SLIDING");
	}

	public static String explainParameters(AlgorithmType algType) {
		String base = "Parameters: (threshold) string defining the DecisionFunction converting numeric to boolean scores <br>";
		switch(algType){
			case ELKI_ABOD:
			case SLIDING_ELKI_ABOD:
				return base;
			case ELKI_LOF:
			case ELKI_COF:
			case SLIDING_ELKI_COF:
				return base + "(k) the number of neighbours.";
			case ELKI_FASTABOD:
				return base + "(k) the number of neighbours.";
			case ELKI_KMEANS:
			case SLIDING_ELKI_CLUSTERING:
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
			case ELKI_ODIN:
			case SLIDING_ELKI_KNN:
				return base + "(k) the number of neighbours.";
			case SLIDING_SPS:
				return "";
			case WEKA_ISOLATIONFOREST:
			case SLIDING_WEKA_ISOLATIONFOREST:
				return "Parameters: (ntrees) number of trees in the forest, <br>"
						+ "(sample_size) instances to be sampled to train each tree.";
			case DBSCAN:
				return base + "(eps) defines the radius of neighborhood around a data point, <br>"
						+ "(pts) is the minimum number of neighbors within 'eps' radius";
			case LDCOF_DBSCAN:
				return base + "(eps) defines the radius of neighborhood around a data point, <br>"
						+ "(pts) is the minimum number of neighbors within 'eps' radius, <br>"
						+ "(gamma) the LDCOF parameter to separate small/large clusters";
			default:
				return "Parameters are shown in the table.";
		}
	}

	public static String explainAlgorithm(AlgorithmType algType) {
		switch(algType){
			case ELKI_ABOD:
				return "Angle-based algorithm. <br>"
						+ "Angles between the new data point and all the possible couples of data points of <br>"
						+ "the training set are calculated, and then their variance (ABOF) is calsulated. <br>"
						+ "The smaller the ABOF, the greater the anomality of the data point.";
			case SLIDING_ELKI_ABOD:
				return "Sliding version of ABOD.";
			case ELKI_SOS:
				return "Statistical algorithm. <br>"
						+ "It takes as input either a feature matrix or a dissimilarity matrix and outputs for each data point an outlier probability. <br>"
						+ "Intuitively, a data point is considered to be an outlier when the other data points have insufficient affinity with it.";
			case ELKI_ISOS:
				return "Fast version of SOS (Intrinsic SOS), calculating dissimilarity with the kNN of the data point.";
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
			case SLIDING_ELKI_COF:
				return "Sliding version of the COF algorithm.";
			case ELKI_FASTABOD:
				return "Fast (Quadratic) version of ABOD, considering agnles with the kNN of the data point.";
			case ELKI_KMEANS:
				return "Classic clustering algorithm, given the K number of clusters. <br>"
						+ "During training, clusters are created. Then, a new data point is scored as anomalous if it is too far <br>"
						+ "from the nearest cluster, evaluated using Euclidean Distance.";
			case SLIDING_ELKI_CLUSTERING:
				return "Sliding version of K-Means";
			case ELKI_SVM:
				return "Support Vector Machines. <br>"
						+ "Support vectors are the data points that lie closest to the decision surface (or hyperplane), <br>"
						+ "and have direct bearing on the optimum location of the decision surface. <br>"
						+ "The algorithm tries to reconstruct such hyperplane that differentiates between normal and anomalous data. <br>"
						+ "The score reprsent the distance with respect to the boundary: big negative distances represent anomalies. ";
			case HBOS:
				return "Generates histograms for each feature that describes the frequence of occurrence of specific values. <br> "
						+ "Then, a data point is scored with an high HBOS is falls in an area of the histogram with short bars.";		
			case ELKI_ODIN:	
				return "Outlier Detection using Indegree Number. <br>"
						+ "Calculates the odin according to the KNN graph. <br>"
						+ "The lower the ODIN, the higher the probability of anomaly.";
			case SLIDING_ELKI_KNN:
				return "Sliding version of KNN";
			case SLIDING_SPS:
				return "Statistical Predictor and Safety Margin Algorithm. <br> "
						+ "It predicts an acceptability interval in which the next value must fall.";
			case WEKA_ISOLATIONFOREST:
				return "Creates a forest of Isolation Trees, which are evaluated as an ensemble.";
			case SLIDING_WEKA_ISOLATIONFOREST:
				return "Sliding version of Isolation Forest";
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
			default:
				return "Algorithms' details not available.";
		}
	}
	
	private static AlgorithmType[] temporaryAlgorithms(){
		return new AlgorithmType[]{AlgorithmType.SLIDING_WEKA_ISOLATIONFOREST};
	}
	
	public static List<AlgorithmType> availableAlgorithms(){
		List<AlgorithmType> types = new LinkedList<AlgorithmType>();
		types.addAll(Arrays.asList(AlgorithmType.values()));
		types.removeAll(Arrays.asList(temporaryAlgorithms()));
		return types;
	}

	public abstract Map<String, String[]> getDefaultParameterValues();
	
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

}
