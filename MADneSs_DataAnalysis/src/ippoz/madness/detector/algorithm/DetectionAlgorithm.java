/**
 * 
 */
package ippoz.madness.detector.algorithm;

import ippoz.madness.detector.algorithm.elki.ABODELKI;
import ippoz.madness.detector.algorithm.elki.COFELKI;
import ippoz.madness.detector.algorithm.elki.FastABODELKI;
import ippoz.madness.detector.algorithm.elki.KMeansELKI;
import ippoz.madness.detector.algorithm.elki.LOFELKI;
import ippoz.madness.detector.algorithm.elki.ODINELKI;
import ippoz.madness.detector.algorithm.elki.SVMELKI;
import ippoz.madness.detector.algorithm.elki.sliding.ABODSlidingELKI;
import ippoz.madness.detector.algorithm.elki.sliding.COFSlidingELKI;
import ippoz.madness.detector.algorithm.elki.sliding.KMeansSlidingELKI;
import ippoz.madness.detector.algorithm.elki.sliding.KNNSlidingELKI;
import ippoz.madness.detector.algorithm.sliding.SPSSlidingAlgorithm;
import ippoz.madness.detector.algorithm.weka.IsolationForestSlidingWEKA;
import ippoz.madness.detector.algorithm.weka.IsolationForestWEKA;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.ComplexDataSeries;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.dataseries.MultipleDataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.KnowledgeType;
import ippoz.madness.detector.commons.service.StatPair;
import ippoz.madness.detector.commons.support.AppLogger;

/**
 * The Class DetectionAlgorithm.
 *
 * @author Tommy
 */
public abstract class DetectionAlgorithm {
	
	/** The configuration. */
	protected AlgorithmConfiguration conf;
	
	/**
	 * Instantiates a new detection algorithm.
	 *
	 * @param conf the configuration
	 */
	public DetectionAlgorithm(AlgorithmConfiguration conf){
		this.conf = conf;
	}
	
	/**
	 * Converts a double score into a 0-1 one.
	 *
	 * @param anomalyValue the anomaly value
	 * @return the double
	 */
	protected static double anomalyTrueFalse(double anomalyValue){
		if(anomalyValue > 0.0)
			return 1.0;
		else if(anomalyValue == -1.0)
			return -1.0;
		else return 0.0;	
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
			case HIST:
				return new HistoricalIndicatorChecker(dataSeries, conf);
			case CONF:
				return new ConfidenceIntervalChecker(dataSeries, conf);
			case RCC:
				return new RemoteCallChecker(conf);
			case WER:
				return new WesternElectricRulesChecker(dataSeries, conf);
			case INV:
				if(dataSeries instanceof MultipleDataSeries)
					return new InvariantChecker((MultipleDataSeries)dataSeries, conf);
				else {
					AppLogger.logError(DetectionAlgorithm.class, "DataSeriesError", "Cannot create INV checker with just simple data series '" + dataSeries.getName() + "'");
					return null;
				}
			case PEA:
				return new PearsonIndexChecker(conf);
			case HBOS:
				return new HBOSDetectionAlgorithm(dataSeries, conf);
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
			case ELKI_ODIN:
				return new ODINELKI(dataSeries, conf);
			case ELKI_SVM:
				return new SVMELKI(dataSeries, conf);
			case WEKA_ISOLATIONFOREST:
				return new IsolationForestWEKA(dataSeries, conf);
			case SLIDING_SPS:
				//return new SPSDetector(dataSeries, conf);
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
			default:
				return null;
			
		}
	}
	
	public static KnowledgeType getKnowledgeType(AlgorithmType algType) {
		switch(algType){
			case INV:
			case RCC:
				return KnowledgeType.GLOBAL;
			default:
				if(algType.name().toUpperCase().contains("SLIDING"))
					return KnowledgeType.SLIDING;
				else return KnowledgeType.SINGLE;
		}
	}
	
	public static boolean isSeriesValidFor(AlgorithmType algType, DataSeries dataSeries) {
		return (dataSeries.size() == 2 && algType == AlgorithmType.INV) || 
				(dataSeries.size() == 1 && (algType == AlgorithmType.SLIDING_SPS || algType == AlgorithmType.HIST)) ||
				(dataSeries.size() > 1 && (algType == AlgorithmType.WEKA_ISOLATIONFOREST || algType == AlgorithmType.SLIDING_WEKA_ISOLATIONFOREST)) ||
				(dataSeries.size() == 1 && algType.equals(AlgorithmType.ELKI_ODIN)) ||
				(!algType.equals(AlgorithmType.ELKI_ODIN) && algType.toString().contains("ELKI_") ||
				(algType.toString().contains("HBOS")));
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
			if(getAlgorithmType().equals(AlgorithmType.RCC))
				return false;
			else if(getAlgorithmType().equals(AlgorithmType.PEA))
				return ((PearsonIndexChecker)this).getDs1().contains(serie) || ((PearsonIndexChecker)this).getDs2().contains(serie);
			else if(getAlgorithmType().equals(AlgorithmType.INV))
				return false;
			else return false;
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
	public double snapshotAnomalyRate(Knowledge knowledge, int currentIndex){
		return anomalyTrueFalse(evaluateSnapshot(knowledge, currentIndex));//*getWeight();
	}
	
	/**
	 * Evaluates a snapshot.
	 *
	 * @param sysSnapshot the snapshot
	 * @param knowledge 
	 * @return the result of the evaluation
	 */
	protected abstract double evaluateSnapshot(Knowledge knowledge, int currentIndex);
	
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

}
