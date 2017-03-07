/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.multilayer.commons.datacategory.DataCategory;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.AppUtility;
import ippoz.multilayer.detector.commons.support.PreferencesManager;
import ippoz.multilayer.detector.metric.Custom_Metric;
import ippoz.multilayer.detector.metric.FMeasure_Metric;
import ippoz.multilayer.detector.metric.FN_Metric;
import ippoz.multilayer.detector.metric.FP_Metric;
import ippoz.multilayer.detector.metric.FScore_Metric;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.metric.Precision_Metric;
import ippoz.multilayer.detector.metric.Recall_Metric;
import ippoz.multilayer.detector.metric.TN_Metric;
import ippoz.multilayer.detector.metric.TP_Metric;
import ippoz.multilayer.detector.reputation.BetaReputation;
import ippoz.multilayer.detector.reputation.ConstantReputation;
import ippoz.multilayer.detector.reputation.MetricReputation;
import ippoz.multilayer.detector.reputation.Reputation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class DetectionManager.
 * This is the manager for the detection. Coordinates all the other managers, especially the ones responsible for training and validation phases.
 *
 * @author Tommy
 */
public class DetectionManager {
	
	/** The Constant DB_USERNAME. */
	private static final String DB_USERNAME = "DB_USERNAME";
	
	/** The Constant DB_PASSWORD. */
	private static final String DB_PASSWORD = "DB_PASSWORD";
	
	/** The Constant TRAIN_RUN_PREFERENCE. */
	private static final String TRAIN_RUN_PREFERENCE = "TRAIN_RUN_IDS";
	
	/** The Constant VALIDATION_RUN_PREFERENCE. */
	private static final String VALIDATION_RUN_PREFERENCE = "VALIDATION_RUN_IDS";
	
	/** The Constant INV_DOMAIN. */
	public static final String INV_DOMAIN = "INV_DOMAIN";
	
	/** The Constant OUTPUT_FORMAT. */
	public static final String OUTPUT_FORMAT = "OUTPUT_TYPE";
	
	/** The Constant OUTPUT_FOLDER. */
	public static final String OUTPUT_FOLDER = "OUTPUT_FOLDER";
	
	/** The Constant METRIC_TYPE. */
	private static final String METRIC_TYPE = "METRIC"; 
	
	/** The Constant REPUTATION_TYPE. */
	private static final String REPUTATION_TYPE = "REPUTATION";
	
	private static final String VALID_AFTER_INJECTION = "VALID_AFTER_INJECTION";
	
	/** The Constant CONF_FILE_FOLDER. */
	public static final String CONF_FILE_FOLDER = "CONF_FILE_FOLDER";
	
	/** The Constant SCORES_FILE_FOLDER. */
	public static final String SCORES_FILE_FOLDER = "SCORES_FILE_FOLDER";
	
	/** The Constant SETUP_FILE_FOLDER. */
	public static final String SETUP_FILE_FOLDER = "SETUP_FILE_FOLDER";
	
	/** The Constant TRAIN_NEEDED_FLAG. */
	private static final String TRAIN_NEEDED_FLAG = "TRAIN_FLAG";
	
	/** The Constant DETECTION_PREFERENCES_FILE. */
	private static final String DETECTION_PREFERENCES_FILE = "DETECTION_PREFERENCES_FILE";
	
	/** The Constant DM_ANOMALY_TRESHOLD. */
	private static final String DM_ANOMALY_TRESHOLD = "ANOMALY_TRESHOLD";
	
	/** The Constant DM_SCORE_TRESHOLD. */
	private static final String DM_SCORE_TRESHOLD = "INDICATOR_SCORE_TRESHOLD";
	
	/** The Constant DM_CONVERGENCE_TIME. */
	private static final String DM_CONVERGENCE_TIME = "CONVERGENCE_TIME";
	
	/** The main preference manager. */
	private PreferencesManager prefManager;
	
	/** The detection preference manager. */
	private PreferencesManager detectionManager;
	
	/** The timings manager. */
	private TimingsManager pManager;
	
	/** The chosen metric. */
	private Metric metric;
	
	/** The chosen reputation metric. */
	private Reputation reputation;
	
	/** The used data types (plain, diff...). */
	private DataCategory[] dataTypes;
	
	/** The algorithm types (SPS, Historical...). */
	private AlgorithmType[] algTypes;
	
	/**
	 * Instantiates a new detection manager.
	 *
	 * @param prefManager the main preference manager
	 */
	public DetectionManager(PreferencesManager prefManager){
		this.prefManager = prefManager;
		pManager = new TimingsManager();
		detectionManager = new PreferencesManager(prefManager.getPreference(DETECTION_PREFERENCES_FILE));
		metric = getMetric();
		reputation = getReputation(metric);
		dataTypes = getDataTypes();
		algTypes = getAlgTypes();
		pManager.addTiming(TimingsManager.SCORING_METRIC, metric.getMetricName());
		pManager.addTiming(TimingsManager.REPUTATION_METRIC, reputation.getReputationTag());
		pManager.addTiming(TimingsManager.EXECUTION_TIME, new Date().toString());
	}
	
	/**
	 * Check premises for the execution, such as MySQL server status.
	 *
	 * @return true, if premises are satisfied
	 */
	public boolean checkPremises(){
		if(!AppUtility.isServerUp(3306)){
			AppLogger.logError(getClass(), "MySQLException", "MySQL is not running. Please activate it");
			return false;
		}
		return true;
	}
	
	/**
	 * Loads the validation metrics, used to score the final result.
	 *
	 * @return the list of metrics
	 */
	private Metric[] loadValidationMetrics() {
		File dataTypeFile = new File(prefManager.getPreference(DetectionManager.SETUP_FILE_FOLDER) + "validationMetrics.preferences");
		LinkedList<Metric> metricList = new LinkedList<Metric>();
		BufferedReader reader;
		String readed;
		try {
			if(dataTypeFile.exists()){
				reader = new BufferedReader(new FileReader(dataTypeFile));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*")){
							metricList.add(getMetric(readed.trim()));
						}
					}
				}
				reader.close();
			} 
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		}
		return metricList.toArray(new Metric[metricList.size()]);
	}

	/**
	 * Starts the train process.
	 */
	public void train(){
		TrainerManager tManager;
		try {
			if(needTest()) {
				tManager = new TrainerManager(prefManager, pManager, new LoaderManager(readRunIds(TRAIN_RUN_PREFERENCE), "train", pManager, prefManager.getPreference(DB_USERNAME), prefManager.getPreference(DB_PASSWORD)).fetch(), loadConfigurations(), metric, reputation, dataTypes, algTypes);
				tManager.train();
				tManager.flush();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to train detector");
		}
	}
	
	/**
	 * Starts the evaluation process.
	 */
	public void evaluate(){
		EvaluatorManager eManager;
		boolean printOutput = prefManager.getPreference(OUTPUT_FORMAT) != null && !prefManager.getPreference(OUTPUT_FORMAT).equals("null");
		Metric[] metList = loadValidationMetrics();
		HashMap<String, Integer> nVoters = new HashMap<String, Integer>();
		LinkedList<ExperimentData> expList = new LoaderManager(readRunIds(VALIDATION_RUN_PREFERENCE), "validation", pManager, prefManager.getPreference(DB_USERNAME), prefManager.getPreference(DB_PASSWORD)).fetch();
		HashMap<String, HashMap<String, LinkedList<HashMap<Metric, Double>>>> evaluations = new HashMap<String, HashMap<String,LinkedList<HashMap<Metric,Double>>>>();
		try {
			pManager.setupExpTimings(new File(prefManager.getPreference(OUTPUT_FOLDER) + "/evaluationTimings.csv"));
			for(String voterTreshold : parseVoterTresholds()){
				evaluations.put(voterTreshold.trim(), new HashMap<String, LinkedList<HashMap<Metric,Double>>>());
				for(String anomalyTreshold : parseAnomalyTresholds()){
					eManager = new EvaluatorManager(prefManager, pManager, expList, metList, anomalyTreshold.trim(), Double.parseDouble(detectionManager.getPreference(DM_CONVERGENCE_TIME)), voterTreshold.trim(), printOutput);
					if(eManager.detectAnomalies()) {
						evaluations.get(voterTreshold.trim()).put(anomalyTreshold.trim(), eManager.getMetricsEvaluations());
						eManager.printTimings(prefManager.getPreference(OUTPUT_FOLDER) + "/evaluationTimings.csv");
					}
					nVoters.put(voterTreshold.trim(), eManager.getCheckersNumber());
					eManager.flush();
				}
			}
			summarizeEvaluations(evaluations, metList, parseAnomalyTresholds(), nVoters);
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to evaluate detector");
		}
	}
	
	private void summarizeEvaluations(HashMap<String, HashMap<String, LinkedList<HashMap<Metric, Double>>>> evaluations, Metric[] metList, String[] anomalyTresholds, HashMap<String, Integer> nVoters) {
		BufferedWriter writer;
		BufferedWriter compactWriter;
		try {
			compactWriter = new BufferedWriter(new FileWriter(new File(prefManager.getPreference(DetectionManager.OUTPUT_FOLDER) + "/tableSummary.csv")));
			writer = new BufferedWriter(new FileWriter(new File(prefManager.getPreference(DetectionManager.OUTPUT_FOLDER) + "/summary.csv")));
			compactWriter.write("selection_strategy,checkers,");
			for(String anomalyTreshold : anomalyTresholds){
				compactWriter.write(anomalyTreshold + ",");
			}
			compactWriter.write("\n");
			writer.write("voter,anomaly,");
			for(Metric met : metList){
				writer.write(met.getMetricName() + ",");
			}
			writer.write("\n");
			for(String voterTreshold : evaluations.keySet()){
				compactWriter.write(voterTreshold + "," + nVoters.get(voterTreshold.trim()) + ",");
				for(String anomalyTreshold : anomalyTresholds){
					writer.write(voterTreshold + "," + anomalyTreshold.trim() + ",");
					for(Metric met : metList){
						if(met.equals(metric))
							compactWriter.write(getAverageMetricValue(evaluations.get(voterTreshold).get(anomalyTreshold.trim()), met) + ",");
						writer.write(getAverageMetricValue(evaluations.get(voterTreshold).get(anomalyTreshold.trim()), met) + ",");
					}
					writer.write("\n");
				}
				compactWriter.write("\n");
			}
			compactWriter.close();
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write summary files");
		}
	}

	private String getAverageMetricValue(LinkedList<HashMap<Metric, Double>> list, Metric met) {
		LinkedList<Double> dataList = new LinkedList<Double>();
		if(list != null){
			for(HashMap<Metric, Double> map : list){
				dataList.add(map.get(met));
			}
			return String.valueOf(AppUtility.calcAvg(dataList));
		} else return String.valueOf(Double.NaN);
	}

	private String[] parseAnomalyTresholds() {
		String prefString = detectionManager.getPreference(DM_ANOMALY_TRESHOLD);
		if(prefString.contains(","))
			return prefString.split(",");
		else return new String[]{prefString};
	}

	private String[] parseVoterTresholds(){
		String prefString = detectionManager.getPreference(DM_SCORE_TRESHOLD);
		if(prefString.contains(","))
			return prefString.split(",");
		else return new String[]{prefString};
	}
	
	/**
	 * Gets the data types.
	 *
	 * @return the data types
	 */
	private DataCategory[] getDataTypes() {
		File dataTypeFile = new File(prefManager.getPreference(DetectionManager.SETUP_FILE_FOLDER) + "dataSeries.preferences");
		LinkedList<DataCategory> dataList = new LinkedList<DataCategory>();
		BufferedReader reader;
		String readed;
		try {
			if(dataTypeFile.exists()){
				reader = new BufferedReader(new FileReader(dataTypeFile));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*")){
							dataList.add(DataCategory.valueOf(readed.trim()));
						}
					}
				}
				reader.close();
			} 
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		}
		return dataList.toArray(new DataCategory[dataList.size()]);
	}
	
	/**
	 * Gets the algorithm types.
	 *
	 * @return the algorithm types
	 */
	private AlgorithmType[] getAlgTypes() {
		File algTypeFile = new File(prefManager.getPreference(DetectionManager.SETUP_FILE_FOLDER) + "detectionAlgorithms.preferences");
		LinkedList<AlgorithmType> algTypeList = new LinkedList<AlgorithmType>();
		BufferedReader reader;
		String readed;
		try {
			if(algTypeFile.exists()){
				reader = new BufferedReader(new FileReader(algTypeFile));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*")){
							algTypeList.add(AlgorithmType.valueOf(readed.trim()));
						}
					}
				}
				reader.close();
			} 
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		}
		return algTypeList.toArray(new AlgorithmType[algTypeList.size()]);
	}

	/**
	 * Loads the possible configurations for all the algorithms.
	 *
	 * @return the map of the configurations
	 */
	/*private HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>> loadConfigurations() {
		File confFile = new File(prefManager.getPreference(DetectionManager.CONF_FILE_FOLDER) + "conf.csv");
		HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>> confList = new HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>>();
		AlgorithmConfiguration alConf;
		BufferedReader reader;
		String[] header;
		String readed;
		int i;
		try {
			if(confFile.exists()){
				reader = new BufferedReader(new FileReader(confFile));
				readed = reader.readLine();
				if(readed != null){
					header = readed.split(",");
					while(reader.ready()){
						readed = reader.readLine();
						if(readed != null){
							readed = readed.trim();
							if(readed.length() > 0){
								if(readed.split(",")[0].toUpperCase().equals("TYPE")){
									header = readed.split(",");
								} else {
									i = 0;
									alConf = AlgorithmConfiguration.getConfiguration(AlgorithmType.valueOf(readed.split(",")[0].toUpperCase()), null);
									for(String element : readed.split(",")){
										if(i > 0)
											alConf.addItem(header[i], element);
										i++;
									}
									if(confList.get(AlgorithmType.valueOf(readed.split(",")[0].toUpperCase())) == null)
										confList.put(AlgorithmType.valueOf(readed.split(",")[0].toUpperCase()), new LinkedList<AlgorithmConfiguration>());
									confList.get(AlgorithmType.valueOf(readed.split(",")[0].toUpperCase())).add(alConf);
								}
							}
						}
					}
				}
				reader.close();
			} 
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read configurations");
		}
		return confList;
	}*/
	
	/**
	 * Loads the possible configurations for all the algorithms.
	 *
	 * @return the map of the configurations
	 */
	private HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>> loadConfigurations() {
		File confFolder = new File(prefManager.getPreference(DetectionManager.CONF_FILE_FOLDER));
		HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>> confList = new HashMap<AlgorithmType, LinkedList<AlgorithmConfiguration>>();
		AlgorithmConfiguration alConf;
		AlgorithmType algType;
		BufferedReader reader;
		String[] header;
		String readed;
		int i;
		try {
			for(File confFile : confFolder.listFiles()){
				if(confFile.exists() && confFile.getName().endsWith(".conf")){
					algType = AlgorithmType.valueOf(confFile.getName().substring(0,  confFile.getName().indexOf(".")));
					reader = new BufferedReader(new FileReader(confFile));
					readed = reader.readLine();
					if(readed != null){
						header = readed.split(",");
						while(reader.ready()){
							readed = reader.readLine();
							if(readed != null){
								readed = readed.trim();
								if(readed.length() > 0){
									i = 0;
									alConf = AlgorithmConfiguration.getConfiguration(algType, null);
									for(String element : readed.split(",")){
										alConf.addItem(header[i++], element);
									}
									if(confList.get(algType) == null)
										confList.put(algType, new LinkedList<AlgorithmConfiguration>());
									confList.get(algType).add(alConf);
								}
							}
						}
						AppLogger.logInfo(getClass(), "Found " + confList.get(algType).size() + " configuration for " + algType + " algorithm");
					}
					reader.close();
				} 
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read configurations");
		}
		return confList;
	}
	
	/**
	 * Returns run IDs parsing a specific tag.
	 *
	 * @param runTag the run tag
	 * @return the list of IDs
	 */
	private LinkedList<String> readRunIds(String runTag){
		String from, to;
		String idPref = prefManager.getPreference(runTag).trim();
		LinkedList<String> idList = new LinkedList<String>();
		if(idPref != null && idPref.length() > 0){
			for(String id : idPref.split(",")){
				if(id.contains("-")){
					from = id.split("-")[0].trim();
					to = id.split("-")[1].trim();
					for(int i=Integer.parseInt(from);i<=Integer.parseInt(to);i++){
						idList.add(String.valueOf(i));
					}
				} else idList.add(id.trim());
			}
		}
		return idList;
	}
	
	/**
	 * Gets the reputation.
	 *
	 * @param metric the chosen metric
	 * @return the obtained reputation
	 */
	private Reputation getReputation(Metric metric) {
		String reputationType = prefManager.getPreference(REPUTATION_TYPE);
		boolean validAfter = Boolean.getBoolean(prefManager.getPreference(VALID_AFTER_INJECTION));
		switch(reputationType.toUpperCase()){
			case "BETA":
				return new BetaReputation(reputationType, validAfter);
			case "METRIC":
				return new MetricReputation(reputationType, metric);
			default:
				if(AppUtility.isNumber(reputationType))
					return new ConstantReputation(reputationType, Double.parseDouble(reputationType));
				else return null;
		}
	}
	
	/**
	 * Gets the metric.
	 *
	 * @param metricType the metric tag
	 * @return the obtained metric
	 */
	private Metric getMetric(String metricType){
		String param = null;
		boolean validAfter = Boolean.getBoolean(prefManager.getPreference(VALID_AFTER_INJECTION));
		if(metricType.contains("(")){
			param = metricType.substring(metricType.indexOf("(")+1, metricType.indexOf(")"));
			metricType = metricType.substring(0, metricType.indexOf("("));
		}
		switch(metricType.toUpperCase()){
			case "TP":
			case "TRUEPOSITIVE":
				return new TP_Metric(false, validAfter);
			case "TN":
			case "TRUENEGATIVE":
				return new TN_Metric(false, validAfter);
			case "FN":
			case "FALSENEGATIVE":
				return new FN_Metric(false, validAfter);
			case "FP":
			case "FALSEPOSITIVE":
				return new FP_Metric(false, validAfter);
			case "PRECISION":
				return new Precision_Metric(validAfter);
			case "RECALL":
				return new Recall_Metric(validAfter);
			case "F-MEASURE":
			case "FMEASURE":
				return new FMeasure_Metric(validAfter);
			case "F-SCORE":
			case "FSCORE":
				return new FScore_Metric(Double.valueOf(param), validAfter);
			case "CUSTOM":
				return new Custom_Metric(validAfter);
			default:
				return null;
		}
	}

	/**
	 * Returns the metric.
	 *
	 * @return the metric
	 */
	private Metric getMetric() {
		return getMetric(prefManager.getPreference(METRIC_TYPE));
	}
	
	/**
	 * Returns a boolean targeting the need of training before evaluation.
	 *
	 * @return true if training is needed
	 */
	public boolean needTest(){
		return !prefManager.getPreference(TRAIN_NEEDED_FLAG).equals("0");
	}
	
}
