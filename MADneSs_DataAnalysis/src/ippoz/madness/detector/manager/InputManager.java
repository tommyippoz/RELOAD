/**
 * 
 */
package ippoz.madness.detector.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.knowledge.sliding.SlidingPolicyType;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.commons.support.PreferencesManager;
import ippoz.madness.detector.metric.AUC_Metric;
import ippoz.madness.detector.metric.Accuracy_Metric;
import ippoz.madness.detector.metric.Custom_Metric;
import ippoz.madness.detector.metric.FMeasure_Metric;
import ippoz.madness.detector.metric.FN_Metric;
import ippoz.madness.detector.metric.FP_Metric;
import ippoz.madness.detector.metric.FScore_Metric;
import ippoz.madness.detector.metric.FalsePositiveRate_Metric;
import ippoz.madness.detector.metric.Matthews_Coefficient;
import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.metric.MetricType;
import ippoz.madness.detector.metric.Precision_Metric;
import ippoz.madness.detector.metric.Recall_Metric;
import ippoz.madness.detector.metric.TN_Metric;
import ippoz.madness.detector.metric.TP_Metric;
import ippoz.madness.detector.reputation.BetaReputation;
import ippoz.madness.detector.reputation.ConstantReputation;
import ippoz.madness.detector.reputation.MetricReputation;
import ippoz.madness.detector.reputation.Reputation;

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

/**
 * @author Tommy
 *
 */
public class InputManager {
	
	private static final String DEFAULT_GLOBAL_PREF_FILE = "madness.preferences";
	
	private static final String DEFAULT_SCORING_PREF_FILE = "scoringPreferences.preferences";
	
	private static final String DEFAULT_ALGORITHM_PREF_FILE = "algorithmPreferences.preferences";
	
	/** The Constant LOADER_TYPE. */
	public static final String LOADERS = "LOADERS";
	
	/** The Constant CONSIDERED_LAYERS. */
	public static final String CONSIDERED_LAYERS = "CONSIDERED_LAYERS";
	
	/** The Constant INV_DOMAIN. */
	public static final String DATA_SERIES_DOMAIN = "DATA_SERIES_DOMAIN";
	
	/** The Constant OUTPUT_FORMAT. */
	public static final String OUTPUT_FORMAT = "OUTPUT_TYPE";
	
	/** The Constant OUTPUT_FOLDER. */
	public static final String INPUT_FOLDER = "INPUT_FOLDER";
	
	/** The Constant OUTPUT_FOLDER. */
	public static final String OUTPUT_FOLDER = "OUTPUT_FOLDER";
	
	/** The Constant OUTPUT_FOLDER. */
	public static final String LOADER_FOLDER = "LOADER_FOLDER";
	
	/** The Constant DATASETS_FOLDER. */
	public static final String DATASETS_FOLDER = "DATASETS_FOLDER";
	
	/** The Constant METRIC_TYPE. */
	public static final String METRIC = "METRIC"; 
	
	/** The Constant METRIC_TYPE. */
	public static final String METRIC_TYPE = "METRIC_TYPE"; 
	
	/** The Constant FILTERING_TRESHOLD. */
	public static final String FILTERING_TRESHOLD = "FILTERING_TRESHOLD"; 
	
	public static final String ANOMALY_WINDOW = "ANOMALY_WINDOW"; 
	
	/** The Constant REPUTATION_TYPE. */
	public static final String REPUTATION_TYPE = "REPUTATION";
	
	public static final String VALID_AFTER_INJECTION = "VALID_AFTER_INJECTION";
	
	/** The Constant CONF_FILE_FOLDER. */
	public static final String CONF_FILE_FOLDER = "CONF_FILE_FOLDER";
	
	/** The Constant SCORES_FILE_FOLDER. */
	public static final String SCORES_FILE_FOLDER = "SCORES_FILE_FOLDER";
	
	/** The Constant SCORES_FILE_FOLDER. */
	public static final String SCORES_FILE = "SCORES_FILE";
	
	/** The Constant SETUP_FILE_FOLDER. */
	public static final String SETUP_FILE_FOLDER = "SETUP_FILE_FOLDER";
	
	/** The Constant TRAIN_NEEDED_FLAG. */
	public static final String TRAIN_NEEDED_FLAG = "TRAIN_FLAG";
	
	/** The Constant FILTERING_NEEDED_FLAG. */
	public static final String FILTERING_NEEDED_FLAG = "FILTERING_FLAG";
	
	/** The Constant DETECTION_PREFERENCES_FILE. */
	public static final String DETECTION_PREFERENCES_FILE = "DETECTION_PREFERENCES_FILE";
	
	/** The Constant DM_ANOMALY_TRESHOLD. */
	private static final String DM_ANOMALY_TRESHOLD = "ANOMALY_TRESHOLD";
	
	/** The Constant DM_SCORE_TRESHOLD. */
	private static final String DM_SCORE_TRESHOLD = "INDICATOR_SCORE_TRESHOLD";
	
	/** The Constant DM_CONVERGENCE_TIME. */
	private static final String DM_CONVERGENCE_TIME = "CONVERGENCE_TIME";

	public static final String PEARSON_SIMPLE_THRESHOLD = "PEARSON_TOLERANCE";
	
	public static final String PEARSON_COMPLEX_THRESHOLD = "PEARSON_NUPLES_TOLERANCE";
	
	public static final String SLIDING_POLICY = "SLIDING_WINDOW_POLICY";
	
	public static final String SLIDING_WINDOW_SIZE = "SLIDING_WINDOW_SIZE";
	
	/** The main preference manager. */
	private PreferencesManager prefManager;
	
	/** The detection preference manager. */
	private PreferencesManager detectionManager;
	
	public InputManager(PreferencesManager prefManager) {
		this.prefManager = prefManager;
		try {
			if(prefManager == null || !prefManager.isValidFile())
				this.prefManager = generateDefaultMADneSsPreferences();
			if(prefManager.hasPreference(DETECTION_PREFERENCES_FILE) && new File(prefManager.getPreference(DETECTION_PREFERENCES_FILE)).exists())
				detectionManager = new PreferencesManager(prefManager.getPreference(DETECTION_PREFERENCES_FILE));
			else {
				detectionManager = generateDefaultScoringPreferences();
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Error while reading preferences");
		}
	}
	
	public boolean updatePreference(String tag, String newValue){
		if(tag != null && prefManager.hasPreference(tag)){
			prefManager.updatePreference(tag, newValue);
			return true;
		} else return false;
	}

	public String getLoaders() {
		return prefManager.getPreference(LOADERS);
	}

	public int getAnomalyWindow() {
		if(prefManager.hasPreference(ANOMALY_WINDOW) && AppUtility.isNumber(prefManager.getPreference(ANOMALY_WINDOW)))
			return Integer.parseInt(prefManager.getPreference(ANOMALY_WINDOW));
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					ANOMALY_WINDOW + " not found. Using default value of 1");
			return 1;
		}
	}

	public PreferencesManager getLoaderPreferences(String loaderFile) {
		if(new File(getLoaderFolder() + loaderFile).exists())
			return new PreferencesManager(getLoaderFolder() + loaderFile);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Loader '" + 
					loaderFile + "' not valid.");
			return null;
		}
	}

	public String getConsideredLayers() {
		if(prefManager.hasPreference(CONSIDERED_LAYERS))
			return prefManager.getPreference(CONSIDERED_LAYERS);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					CONSIDERED_LAYERS + " not found. Using 'NO_LAYER' default value");
			return "NO_LAYER";
		}
	}
	
	private String checkFolder(String toCheck){
		return checkFolder(toCheck, false);
	}
	
	private String checkFolder(String toCheck, boolean create){
		File dir;
		if(!toCheck.endsWith(File.separator))
			toCheck = toCheck + File.separator;
		dir = new File(toCheck);
		if(dir.exists())
			return toCheck;
		else {
			if(create){
				dir.mkdirs();
				return toCheck;
			} else {
				AppLogger.logError(getClass(), "MissingPreferenceError", "Folder " + toCheck + " does not exist");
				return null;
			}
		}
	}
	
	/**
	 * Loads the validation metrics, used to score the final result.
	 *
	 * @return the list of metrics
	 */
	public Metric[] loadValidationMetrics() {
		File dataTypeFile = new File(getSetupFolder() + "validationMetrics.preferences");
		List<Metric> metricList = new LinkedList<>();
		BufferedReader reader;
		String readed;
		try {
			if(!dataTypeFile.exists())
				generateValidationMetricsFile(dataTypeFile);
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
			metricList.add(getMetric("AUC"));
			reader.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		}
		return metricList.toArray(new Metric[metricList.size()]);
	}

	public double getFilteringTreshold() {
		if(prefManager.hasPreference(FILTERING_TRESHOLD))
			return Double.parseDouble(prefManager.getPreference(FILTERING_TRESHOLD));
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					FILTERING_TRESHOLD + " not found. Using default value of 0.0");
			return 0.0;
		} 
	}

	public String getInputFolder() {
		if(prefManager.hasPreference(INPUT_FOLDER))
			return checkFolder(prefManager.getPreference(INPUT_FOLDER), true);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					INPUT_FOLDER + " not found. Using default value of 'input'");
			return checkFolder("input", true);
		}
	}
	
	public String getDatasetsFolder() {
		if(prefManager.hasPreference(DATASETS_FOLDER))
			return checkFolder(prefManager.getPreference(DATASETS_FOLDER));
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					DATASETS_FOLDER + " not found. Using default value of ' datasets'");
			return checkFolder("datasets", true);
		}
	}
	
	public String getLoaderFolder() {
		if(prefManager.hasPreference(LOADER_FOLDER))
			return checkFolder(getInputFolder() + prefManager.getPreference(LOADER_FOLDER));
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					LOADER_FOLDER + " not found. Using default value of '" + getInputFolder() + "loaders'");
			return checkFolder(getInputFolder() + "loaders");
		}
	}
	
	public String getScoresFolder() {
		if(prefManager.hasPreference(SCORES_FILE_FOLDER))
			return checkFolder(prefManager.getPreference(SCORES_FILE_FOLDER), true);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					SCORES_FILE_FOLDER + " not found. Using default value of 'intermediate'");
			return checkFolder("intermediate", true);
		}
	}
	
	/**
	 * Gets the reputation.
	 *
	 * @param metric the chosen metric
	 * @return the obtained reputation
	 */
	public Reputation getReputation(Metric metric) {
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
				else {
					AppLogger.logError(getClass(), "MissingPreferenceError", "Reputation cannot be defined");
					return null;
				}
		}
	}
	
	public MetricType getMetricType() {
		return getMetric(prefManager.getPreference(METRIC)).getMetricType();
	}
	
	/**
	 * Gets the metric.
	 *
	 * @param metricType the metric tag
	 * @return the obtained metric
	 */
	public Metric getMetric(String metricType){
		String param = null;
		String mType = prefManager.getPreference(METRIC_TYPE);
		boolean absolute = mType != null && mType.equals("absolute") ? true : false;
		boolean validAfter = prefManager.hasPreference(VALID_AFTER_INJECTION) ? Boolean.getBoolean(prefManager.getPreference(VALID_AFTER_INJECTION)) : true;
		if(metricType.contains("(")){
			param = metricType.substring(metricType.indexOf('(')+1, metricType.indexOf(')'));
			metricType = metricType.substring(0, metricType.indexOf('('));
		}
		switch(metricType.toUpperCase()){
			case "TP":
			case "TRUEPOSITIVE":
				return new TP_Metric(absolute, validAfter);
			case "TN":
			case "TRUENEGATIVE":
				return new TN_Metric(absolute, validAfter);
			case "FN":
			case "FALSENEGATIVE":
				return new FN_Metric(absolute, validAfter);
			case "FP":
			case "FALSEPOSITIVE":
				return new FP_Metric(absolute, validAfter);
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
			case "FPR":
				return new FalsePositiveRate_Metric(validAfter);
			case "MATTHEWS":
				return new Matthews_Coefficient(validAfter);
			case "AUC":
				return new AUC_Metric(validAfter);
			case "ACCURACY":
				return new Accuracy_Metric(validAfter);
			case "CUSTOM":
				return new Custom_Metric(validAfter);	
			default:
				AppLogger.logError(getClass(), "MissingPreferenceError", "Metric cannot be defined");
				return null;
		}
	}

	/**
	 * Returns the metric.
	 *
	 * @return the metric
	 */
	public Metric getMetricName() {
		return getMetric(prefManager.getPreference(METRIC));
	}

	public String getConfigurationFolder() {
		if(prefManager.hasPreference(CONF_FILE_FOLDER))
			return checkFolder(getInputFolder() + prefManager.getPreference(CONF_FILE_FOLDER));
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					CONF_FILE_FOLDER + " not found. Using default value of '" + getInputFolder() + "conf'");
			return checkFolder(getInputFolder() + "conf");
		}
	}

	public String getSetupFolder() {
		if(prefManager.hasPreference(SETUP_FILE_FOLDER))
			return checkFolder(getInputFolder() + prefManager.getPreference(SETUP_FILE_FOLDER), true);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					SETUP_FILE_FOLDER + " not found. Using default value of '" + getInputFolder() + "setup'");
			return checkFolder(getInputFolder() + "setup");
		}
	}
	
	public String getAnomalyTreshold(){
		if(detectionManager.hasPreference(DM_ANOMALY_TRESHOLD))
			return detectionManager.getPreference(DM_ANOMALY_TRESHOLD);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					DM_ANOMALY_TRESHOLD + " not found. Using default value of 'HALF'");
			return "HALF";
		}
	}
	
	public String[] parseAnomalyTresholds() {
		String prefString = getAnomalyTreshold();
		if(prefString != null) {
			if(prefString.contains(","))
				return prefString.split(",");
			else return new String[]{prefString};
		} else return null;
	}
	
	public String getVoterTreshold(){
		if(detectionManager.hasPreference(DM_SCORE_TRESHOLD))
			return detectionManager.getPreference(DM_SCORE_TRESHOLD);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					DM_SCORE_TRESHOLD + " not found. Using default value of 'FILTERED 10'");
			return "FILTERED 10";
		}
	}

	public String[] parseVoterTresholds(){
		String prefString = getVoterTreshold();
		if(prefString != null){
			if(prefString.contains(","))
				return prefString.split(",");
			else return new String[]{prefString};
		} else return null;
	}
	
	/**
	 * Gets the data types.
	 *
	 * @return the data types
	 */
	public DataCategory[] getDataTypes() {
		File dataTypeFile = new File(getSetupFolder() + "dataSeries.preferences");
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
			} else {
				AppLogger.logError(getClass(), "MissingPreferenceError", "File " + 
						dataTypeFile.getPath() + " not found. Using default value of 'PLAIN, DIFFERENCE'");
				return new DataCategory[]{DataCategory.PLAIN, DataCategory.DIFFERENCE};
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		}
		return dataList.toArray(new DataCategory[dataList.size()]);
	}
	
	public Map<AlgorithmType, List<AlgorithmConfiguration>> loadConfiguration(AlgorithmType at) {
		List<AlgorithmType> list = new LinkedList<AlgorithmType>();
		list.add(at);
		return loadConfigurations(list);
	}

	/**
	 * Loads the possible configurations for all the algorithms.
	 *
	 * @return the map of the configurations
	 */
	public Map<AlgorithmType, List<AlgorithmConfiguration>> loadConfigurations(List<AlgorithmType> algTypes) {
		File confFolder = new File(getConfigurationFolder());
		Map<AlgorithmType, List<AlgorithmConfiguration>> confList = new HashMap<AlgorithmType, List<AlgorithmConfiguration>>();
		AlgorithmConfiguration alConf;
		AlgorithmType algType;
		BufferedReader reader = null;
		String[] header;
		String readed;
		int i;
		try {
			for(File confFile : confFolder.listFiles()){
				if(confFile.exists() && confFile.getName().endsWith(".conf")){
					try {
						algType = AlgorithmType.valueOf(confFile.getName().substring(0,  confFile.getName().indexOf(".")));
						if(algType != null && algTypes.contains(algType)) {
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
						}
					} catch(Exception ex){
						AppLogger.logError(getClass(), "ConfigurationError", "File " + confFile.getPath() + " cannot be associated to any known algorithm");
					}
				} 
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read configurations");
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				AppLogger.logException(getClass(), ex, "Unable to read configurations");
			}
		}
		return confList;
	}

	public boolean getTrainingFlag() {
		if(prefManager.hasPreference(TRAIN_NEEDED_FLAG))
			return !prefManager.getPreference(TRAIN_NEEDED_FLAG).equals("0");
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					TRAIN_NEEDED_FLAG + " not found. Using default value of '1'");
			return true;
		}
	}
	
	public boolean getFilteringFlag() {
		if(prefManager.hasPreference(FILTERING_NEEDED_FLAG))
			return !prefManager.getPreference(FILTERING_NEEDED_FLAG).equals("0");
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					FILTERING_NEEDED_FLAG + " not found. Using default value of '1'");
			return true;
		}
	}

	public String getOutputFolder() {
		if(prefManager.hasPreference(OUTPUT_FOLDER))
			return checkFolder(prefManager.getPreference(OUTPUT_FOLDER), true);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					OUTPUT_FOLDER + " not found. Using default value of 'output'");
			return checkFolder("output", true);
		}
	}

	public boolean getOutputVisibility() {
		if(prefManager.hasPreference(OUTPUT_FORMAT))
			return !prefManager.getPreference(OUTPUT_FORMAT).equals("null");
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					OUTPUT_FORMAT + " not found. Using default value of 'not visible'");
			return false;
		}
	}

	public double getConvergenceTime() {
		if(detectionManager.hasPreference(DM_CONVERGENCE_TIME) && AppUtility.isNumber(detectionManager.getPreference(DM_CONVERGENCE_TIME)))
			return Double.parseDouble(detectionManager.getPreference(DM_CONVERGENCE_TIME));
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					DM_CONVERGENCE_TIME + " not found. Using default value of '0.0'");
			return 0.0;
		}
	}
	
	public String getScoresFile(String prequel){
		if(prefManager.hasPreference(SCORES_FILE_FOLDER) && prefManager.hasPreference(SCORES_FILE))
			return getScoresFolder() + (prequel != null ? prequel + "_" : "") + prefManager.getPreference(SCORES_FILE);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					SCORES_FILE + " not found. Using default value of '" + getScoresFolder() + "scores.csv'");
			return getScoresFolder() + (prequel != null ? prequel + "_" : "") + "scores.csv";
		}
	}
	
	public String getOutputFormat(){
		if(prefManager.hasPreference(OUTPUT_FORMAT))
			return prefManager.getPreference(OUTPUT_FORMAT);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					OUTPUT_FORMAT + " not found. Using default value of 'null'");
			return "null";
		}
	}

	public String getDataSeriesDomain() {
		if(prefManager.hasPreference(DATA_SERIES_DOMAIN))
			return prefManager.getPreference(DATA_SERIES_DOMAIN);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					DATA_SERIES_DOMAIN + " not found. Using default value of 'PEARSON(0.90)'");
			return "PEARSON(0.90)";
		}
	}
	
	private static PreferencesManager generateDefaultMADneSsPreferences() throws IOException {
		File prefFile = null;
		BufferedWriter writer = null;
		try {
			prefFile = new File(DEFAULT_GLOBAL_PREF_FILE);
			if(!prefFile.exists()){
				writer = new BufferedWriter(new FileWriter(prefFile));
				writer.write("* Default preferences file for 'MADneSs'. Comments with '*'.\n");
				writer.write("\n\n* Data Source - Loaders.\n");
				writer.write("\n* Loader type (MYSQL, CSVALL).\n" + 
						"LOADER_FOLDER = loaders\n");
				writer.write("\n* Loaders folder.\n" + 
						"LOADERS = iscx\n");
				writer.write("\n* Datasets folder.\n" +
						"DATASETS_FOLDER = datasets\n");
				writer.write("\n* MADneSs Execution.\n\n");
				writer.write("\n* Perform Filtering of Indicators (0 = NO, 1 = YES).\n" + 
						"FILTERING_FLAG = 1\n");
				writer.write("\n* Perform Training (0 = NO, 1 = YES).\n" + 
						"TRAIN_FLAG = 1\n");
				writer.write("\n* The scoring metric. Accepted values are FP, FN, TP, TN, PRECISION, RECALL, FSCORE(b), FMEASURE, FPR, FNR, MATTHEWS.\n" + 
						"METRIC = FSCORE(2)\n");
				writer.write("\n* The metric type (absolute/relative). Applies only to FN, FP, TN, TP.\n" + 
						"METRIC_TYPE = absolute\n");
				writer.write("\n* Expected duration of injected faults (observations).\n" + 
						"ANOMALY_WINDOW = 1\n");
				writer.write("\n* Threshold for filtering i.e., minimum value of FPR accepted.\n" + 
						"FILTERING_TRESHOLD = 0.5\n");
				writer.write("\n* Flag which indicates if we expect more than one fault for each run\n" + 
						"VALID_AFTER_INJECTION = true\n");
				writer.write("\n* Reputation Score. Accepted values are 'double value', BETA, FP, FN, TP, TN, PRECISION, RECALL, FSCORE(b), FMEASURE, FPR, FNR, MATTHEWS\n" + 
						"REPUTATION = 1.0\n");
				writer.write("\n* Strategy to aggregate indicators. Suggested is PEARSON(n), where 'n' is the minimum value of correlation that is accepted\n" + 
						"DATA_SERIES_DOMAIN = PEARSON(0.90)\n");
				writer.write("\n* Strategy to slide windows. Accepted Values are FIFO, \n" + 
						"SLIDING_POLICY = FIFO\n");
				writer.write("\n* Size of the sliding window buffer\n" + 
						"SLIDING_WINDOW_SIZE = 20\n");
				writer.write("\n* Type of output produced by MADneSs. Accepted values are null, IMAGE, TEXT\n" + 
						"OUTPUT_TYPE = null\n");
				writer.write("\n* Path Setup.\n\n");
				writer.write("\n* Input folder\n" + 
						"INPUT_FOLDER = input\n");
				writer.write("\n* Output folder\n" + 
						"OUTPUT_FOLDER = output\n");
				writer.write("\n* Configuration folder\n" + 
						"CONF_FILE_FOLDER = configurations\n");
				writer.write("\n* Setup folder\n" + 
						"SETUP_FILE_FOLDER = setup\n");
				writer.write("\n* Setup folder\n" + 
						"SCORES_FILE_FOLDER = intermediate\n");
				writer.write("\n* Scores file\n" + 
						"SCORES_FILE = scores.csv");
				writer.write("\n\n* Other Preference Files.\n");
				writer.write("\n* Detection Preferences\n" + 
						"DETECTION_PREFERENCES_FILE = scoringPreferences.preferences\n");						
			}
		} catch(IOException ex){
			AppLogger.logException(InputManager.class, ex, "Error while generating MADneSs global preferences");
			throw ex;
		} finally {
			if(writer != null)
				writer.close();
		}
		return new PreferencesManager(DEFAULT_GLOBAL_PREF_FILE);
	}
	
	private PreferencesManager generateDefaultScoringPreferences() throws IOException {
		File prefFile = null;
		BufferedWriter writer = null;
		try {
			prefFile = new File(checkFolder(getInputFolder(), true) + DEFAULT_SCORING_PREF_FILE);
			if(!prefFile.exists()){
				writer = new BufferedWriter(new FileWriter(prefFile));
				writer.write("* Default scoring preferences file for 'MADneSs'. Comments with '*'.\n");
				writer.write("\n* Time needed for convergence (excluded by metric evaluations).\n" + 
						"CONVERGENCE_TIME = 10.0\n");
				writer.write("\n* Set of possible anomaly thresholds. Accepted values are ALL, HALF, THIRD, 'n'.\n" + 
						"ANOMALY_TRESHOLD = ALL, HALF, 1\n");
				writer.write("\n* Strategy to select anomaly checkers. Accepted values are 'min metric score', BEST 'n', FILTERED 'n'.\n" + 
						"INDICATOR_SCORE_TRESHOLD = BEST 10, BEST 3, 0.9, FILTERED 10, BEST 30\n");
			}
		} catch(IOException ex){
			AppLogger.logException(InputManager.class, ex, "Error while generating MADneSs scoring preferences");
			throw ex;
		} finally {
			if(writer != null)
				writer.close();
		}
		return new PreferencesManager(getInputFolder() + DEFAULT_SCORING_PREF_FILE);
	}
	
	public void generateDefaultAlgorithmPreferences() throws IOException {
		File prefFile = null;
		BufferedWriter writer = null;
		try {
			prefFile = new File(checkFolder(getSetupFolder(), true) + DEFAULT_ALGORITHM_PREF_FILE);
			if(!prefFile.exists()){
				writer = new BufferedWriter(new FileWriter(prefFile));
				writer.write("* Default algorithm preferences file for 'MADneSs'. Comments with '*'.\n");
				writer.write("* Uncomment the algorithms you want to use. Filtering uses ELKI_KMEANS by default.\n");
				for(AlgorithmType at : AlgorithmType.values()){
					if(at.equals(AlgorithmType.ELKI_KMEANS))
						writer.write("\n " + at + "\n");
					else writer.write("\n* " + at + "\n");
				}
			}
		} catch(IOException ex){
			AppLogger.logException(InputManager.class, ex, "Error while generating MADneSs algorithm preferences");
			throw ex;
		} finally {
			if(writer != null)
				writer.close();
		}
	}
	
	private void generateValidationMetricsFile(File dataTypeFile) throws IOException {
		BufferedWriter writer = null;
		try {
			if(!dataTypeFile.exists()){
				writer = new BufferedWriter(new FileWriter(dataTypeFile));
				writer.write("* Metrics involved in the output of the detection phase. Comments with '*'.\n");
				writer.write("\n TP\n");
				writer.write("\n TN\n");
				writer.write("\n FP\n");
				writer.write("\n FN\n");
				writer.write("\n FPR\n");
				writer.write("\n Precision\n");
				writer.write("\n Recall\n");
				writer.write("\n FMeasure\n");
				writer.write("\n FScore(2)\n");
				writer.write("\n MATTHEWS\n");
			}
		} catch(IOException ex){
			AppLogger.logException(InputManager.class, ex, "Error while generating MADneSs algorithm preferences");
			throw ex;
		} finally {
			if(writer != null)
				writer.close();
		}
	}

	public double getSimplePearsonThreshold() {
		if(prefManager.hasPreference(PEARSON_SIMPLE_THRESHOLD) && AppUtility.isNumber(prefManager.getPreference(PEARSON_SIMPLE_THRESHOLD)))
			return Double.valueOf(prefManager.getPreference(PEARSON_SIMPLE_THRESHOLD));
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					PEARSON_SIMPLE_THRESHOLD + " not found. Using default value of '0.9'");
			return 0.9;
		}
	}
	
	public double getComplexPearsonThreshold() {
		if(prefManager.hasPreference(PEARSON_COMPLEX_THRESHOLD) && AppUtility.isNumber(prefManager.getPreference(PEARSON_COMPLEX_THRESHOLD)))
			return Double.valueOf(prefManager.getPreference(PEARSON_COMPLEX_THRESHOLD));
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					PEARSON_COMPLEX_THRESHOLD + " not found. Using default value of '0.95'");
			return 0.95;
		}
	}

	public String getSlidingPolicies() {
		try {
			if(prefManager.hasPreference(SLIDING_POLICY))
				return prefManager.getPreference(SLIDING_POLICY).toUpperCase();
			else {
				AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
						SLIDING_POLICY + " not found. Using default value of 'FIFO'");
				return SlidingPolicyType.FIFO.toString();
			}
		} catch(Exception ex){
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					SLIDING_POLICY + " cannot be parsed correctly. Using default value of 'FIFO'");
			return SlidingPolicyType.FIFO.toString();
		}
	}

	public String getSlidingWindowSizes() {
		if(prefManager.hasPreference(SLIDING_WINDOW_SIZE))
			return prefManager.getPreference(SLIDING_WINDOW_SIZE);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					SLIDING_WINDOW_SIZE + " not found. Using default value of '20'");
			return "20";
		}
	}

	public String getDetectionPreferencesFile() {
		return detectionManager.getFilename();
	}

	public boolean filteringResultExists(String datasetName) {
		return new File(getScoresFolder() + datasetName + "_filtered.csv").exists();
	}	
	
}
