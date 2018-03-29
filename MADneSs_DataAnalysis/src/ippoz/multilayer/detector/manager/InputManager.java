/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.AppUtility;
import ippoz.multilayer.detector.commons.support.PreferencesManager;
import ippoz.multilayer.detector.metric.Custom_Metric;
import ippoz.multilayer.detector.metric.FMeasure_Metric;
import ippoz.multilayer.detector.metric.FN_Metric;
import ippoz.multilayer.detector.metric.FP_Metric;
import ippoz.multilayer.detector.metric.FScore_Metric;
import ippoz.multilayer.detector.metric.FalsePositiveRate_Metric;
import ippoz.multilayer.detector.metric.Matthews_Coefficient;
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
import java.io.File;
import java.io.FileReader;
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
	
	/** The Constant LOADER_TYPE. */
	private static final String LOADER_TYPE = "LOADER_TYPE";
	
	/** The Constant FILTERING_RUN_PREFERENCE. */
	private static final String FILTERING_RUN_PREFERENCE = "GOLDEN_RUN_IDS";
	
	/** The Constant TRAIN_RUN_PREFERENCE. */
	private static final String TRAIN_RUN_PREFERENCE = "TRAIN_RUN_IDS";
	
	/** The Constant VALIDATION_RUN_PREFERENCE. */
	private static final String VALIDATION_RUN_PREFERENCE = "VALIDATION_RUN_IDS";
	
	/** The Constant CONSIDERED_LAYERS. */
	private static final String CONSIDERED_LAYERS = "CONSIDERED_LAYERS";
	
	/** The Constant INV_DOMAIN. */
	public static final String DATA_SERIES_DOMAIN = "DATA_SERIES_DOMAIN";
	
	/** The Constant OUTPUT_FORMAT. */
	public static final String OUTPUT_FORMAT = "OUTPUT_TYPE";
	
	/** The Constant OUTPUT_FOLDER. */
	public static final String OUTPUT_FOLDER = "OUTPUT_FOLDER";
	
	/** The Constant METRIC_TYPE. */
	private static final String METRIC = "METRIC"; 
	
	/** The Constant METRIC_TYPE. */
	private static final String METRIC_TYPE = "METRIC_TYPE"; 
	
	/** The Constant FILTERING_TRESHOLD. */
	private static final String FILTERING_TRESHOLD = "FILTERING_TRESHOLD"; 
	
	private static final String ANOMALY_WINDOW = "ANOMALY_WINDOW"; 
	
	/** The Constant REPUTATION_TYPE. */
	private static final String REPUTATION_TYPE = "REPUTATION";
	
	private static final String VALID_AFTER_INJECTION = "VALID_AFTER_INJECTION";
	
	/** The Constant CONF_FILE_FOLDER. */
	public static final String CONF_FILE_FOLDER = "CONF_FILE_FOLDER";
	
	/** The Constant SCORES_FILE_FOLDER. */
	public static final String SCORES_FILE_FOLDER = "SCORES_FILE_FOLDER";
	
	/** The Constant SCORES_FILE_FOLDER. */
	public static final String SCORES_FILE = "SCORES_FILE";
	
	/** The Constant SETUP_FILE_FOLDER. */
	public static final String SETUP_FILE_FOLDER = "SETUP_FILE_FOLDER";
	
	/** The Constant TRAIN_NEEDED_FLAG. */
	private static final String TRAIN_NEEDED_FLAG = "TRAIN_FLAG";
	
	/** The Constant FILTERING_NEEDED_FLAG. */
	private static final String FILTERING_NEEDED_FLAG = "FILTERING_FLAG";
	
	/** The Constant DETECTION_PREFERENCES_FILE. */
	private static final String DETECTION_PREFERENCES_FILE = "DETECTION_PREFERENCES_FILE";
	
	/** The Constant DM_ANOMALY_TRESHOLD. */
	private static final String DM_ANOMALY_TRESHOLD = "ANOMALY_TRESHOLD";
	
	/** The Constant DM_SCORE_TRESHOLD. */
	private static final String DM_SCORE_TRESHOLD = "INDICATOR_SCORE_TRESHOLD";
	
	/** The Constant DM_CONVERGENCE_TIME. */
	private static final String DM_CONVERGENCE_TIME = "CONVERGENCE_TIME";

	private static final String LOADER_PREF_FILE = "LOADER_PREF_FILE";
	
	/** The main preference manager. */
	private PreferencesManager prefManager;
	
	/** The detection preference manager. */
	private PreferencesManager detectionManager;
	
	public InputManager(PreferencesManager prefManager) {
		this.prefManager = prefManager;
		detectionManager = new PreferencesManager(prefManager.getPreference(DETECTION_PREFERENCES_FILE));
	}
	
	public String getLoader() {
		return prefManager.getPreference(LOADER_TYPE);
	}
	
	public String getRunIDs(String loaderTag){
		return prefManager.getPreference(loaderTag.equals("validation") ? VALIDATION_RUN_PREFERENCE : (loaderTag.equals("filter") ? FILTERING_RUN_PREFERENCE : TRAIN_RUN_PREFERENCE));
	}

	public int getAnomalyWindow() {
		if(prefManager.hasPreference(ANOMALY_WINDOW) && AppUtility.isNumber(prefManager.getPreference(ANOMALY_WINDOW)))
			return Integer.parseInt(prefManager.getPreference(ANOMALY_WINDOW));
		else return 1;
	}

	public PreferencesManager getLoaderPreferences() {
		if(prefManager.hasPreference(LOADER_PREF_FILE))
			return new PreferencesManager(prefManager.getPreference(LOADER_PREF_FILE));
		else return null;
	}

	public String getConsideredLayers() {
		if(prefManager.hasPreference(CONSIDERED_LAYERS))
			return prefManager.getPreference(CONSIDERED_LAYERS);
		else return null;
	}
	
	private String checkFolder(String toCheck){
		File dir;
		if(!toCheck.endsWith(File.separator))
			toCheck = toCheck + File.separator;
		dir = new File(toCheck);
		if(dir.exists())
			return toCheck;
		else return null;
	}
	
	/**
	 * Loads the validation metrics, used to score the final result.
	 *
	 * @return the list of metrics
	 */
	public Metric[] loadValidationMetrics() {
		File dataTypeFile = new File(checkFolder(prefManager.getPreference(SETUP_FILE_FOLDER)) + "validationMetrics.preferences");
		List<Metric> metricList = new LinkedList<>();
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

	public double getFilteringTreshold() {
		if(prefManager.hasPreference(FILTERING_TRESHOLD))
			return Double.parseDouble(prefManager.getPreference(FILTERING_TRESHOLD));
		else return 0.0;
	}

	public String getScoresFolder() {
		if(prefManager.hasPreference(SCORES_FILE_FOLDER))
			return checkFolder(prefManager.getPreference(SCORES_FILE_FOLDER));
		else return null;
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
				else return null;
		}
	}
	
	/**
	 * Gets the metric.
	 *
	 * @param metricType the metric tag
	 * @return the obtained metric
	 */
	public Metric getMetric(String metricType){
		String param = null;
		boolean absolute = prefManager.getPreference(METRIC_TYPE).equals("absolute") ? true : false;
		boolean validAfter = Boolean.getBoolean(prefManager.getPreference(VALID_AFTER_INJECTION));
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
	public Metric getMetricName() {
		return getMetric(prefManager.getPreference(METRIC));
	}

	public String getConfigurationFolder() {
		if(prefManager.hasPreference(CONF_FILE_FOLDER))
			return checkFolder(prefManager.getPreference(CONF_FILE_FOLDER));
		else return null;
	}

	public String getSetupFolder() {
		if(prefManager.hasPreference(SETUP_FILE_FOLDER))
			return checkFolder(prefManager.getPreference(SETUP_FILE_FOLDER));
		else return null;
	}
	
	public String getAnomalyTreshold(){
		if(detectionManager.hasPreference(DM_ANOMALY_TRESHOLD))
			return detectionManager.getPreference(DM_ANOMALY_TRESHOLD);
		else return null;
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
		else return null;
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
	public List<AlgorithmType> getAlgTypes() {
		File algTypeFile = new File(getSetupFolder() + "detectionAlgorithms.preferences");
		List<AlgorithmType> algTypeList = new LinkedList<AlgorithmType>();
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
		return algTypeList;
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
		else return true;
	}
	
	public boolean getFilteringFlag() {
		if(prefManager.hasPreference(FILTERING_NEEDED_FLAG))
			return !prefManager.getPreference(FILTERING_NEEDED_FLAG).equals("0");
		else return true;
	}

	public String getOutputFolder() {
		if(prefManager.hasPreference(OUTPUT_FOLDER))
			return checkFolder(prefManager.getPreference(OUTPUT_FOLDER));
		else return null;
	}

	public boolean getOutputVisibility() {
		if(prefManager.hasPreference(OUTPUT_FORMAT))
			return !prefManager.getPreference(OUTPUT_FORMAT).equals("null");
		return false;
	}

	public double getConvergenceTime() {
		if(detectionManager.hasPreference(DM_CONVERGENCE_TIME) && AppUtility.isNumber(detectionManager.getPreference(DM_CONVERGENCE_TIME)))
			return Double.parseDouble(detectionManager.getPreference(DM_CONVERGENCE_TIME));
		else return 0.0;
	}
	
	public String getScoresFile(){
		if(prefManager.hasPreference(SCORES_FILE_FOLDER) && prefManager.hasPreference(SCORES_FILE))
			return checkFolder(prefManager.getPreference(SCORES_FILE_FOLDER)) + prefManager.getPreference(SCORES_FILE);
		else return null;
	}
	
	public String getOutputFormat(){
		if(prefManager.hasPreference(OUTPUT_FORMAT))
			return prefManager.getPreference(OUTPUT_FORMAT);
		else return null;
	}

	public String getDataSeriesDomain() {
		if(prefManager.hasPreference(DATA_SERIES_DOMAIN))
			return prefManager.getPreference(DATA_SERIES_DOMAIN);
		return null;
	}
}
