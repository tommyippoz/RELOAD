/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.configuration.MetaConfiguration;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.loader.ARFFLoader;
import ippoz.reload.commons.loader.CSVLoader;
import ippoz.reload.commons.loader.FileLoader;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.loader.LoaderType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.featureselection.FeatureSelector;
import ippoz.reload.featureselection.FeatureSelectorType;
import ippoz.reload.info.FeatureSelectionInfo;
import ippoz.reload.info.TrainInfo;
import ippoz.reload.info.ValidationInfo;
import ippoz.reload.metric.Metric;
import ippoz.reload.metric.MetricType;
import ippoz.reload.reputation.Reputation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class InputManager {
	
	private static final String DEFAULT_GLOBAL_PREF_FILE = "reload.preferences";
	
	private static final String DEFAULT_SCORING_FS_FILE = "featureSelection.preferences";
	
	private static final String DEFAULT_SCORING_PREF_FILE = "scoringPreferences.preferences";
	
	private static final String DEFAULT_ALGORITHM_PREF_FILE = "algorithmPreferences.preferences";
	
	/** The Constant LOADER_TYPE. */
	public static final String LOADERS = "LOADERS";
	
	/** The Constant CONSIDERED_LAYERS. */
	public static final String CONSIDERED_LAYERS = "CONSIDERED_LAYERS";
	
	/** The Constant INV_DOMAIN. */
	public static final String INDICATOR_SELECTION = "INDICATOR_SELECTION";
	
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
	
	/** The Constant KFOLD_COUNTER. */
	public static final String KFOLD_COUNTER = "KFOLD_COUNTER"; 
	
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
	public static final String FILTERING_NEEDED_FLAG = "FEATURE_SELECTION_FLAG";
	
	/** The Constant DETECTION_PREFERENCES_FILE. */
	public static final String DETECTION_PREFERENCES_FILE = "DETECTION_PREFERENCES_FILE";

	public static final String PEARSON_SIMPLE_THRESHOLD = "PEARSON_TOLERANCE";
	
	public static final String PEARSON_COMPLEX_THRESHOLD = "PEARSON_NUPLES_TOLERANCE";
	
	public static final String SLIDING_POLICY = "SLIDING_WINDOW_POLICY";
	
	public static final String SLIDING_WINDOW_SIZE = "SLIDING_WINDOW_SIZE";
	
	public static final String EVALUATION_NEEDED_FLAG = "EVALUATION_FLAG";
	
	public static final String FORCE_TRAINING_BASELEARNERS = "FORCE_TRAINING_BASELEARNERS";
	
	public static final String PARALLEL_TRAINING = "PARALLEL_TRAINING";
	
	public static final String FORCE_TRAINING = "FORCE_TRAINING";

	public static final String PREDICT_MISCLASSIFICATIONS = "PREDICT_MISCLASSIFICATIONS";
	
	/** The main preference manager. */
	private PreferencesManager prefManager;
	
	public InputManager(PreferencesManager prefManager) {
		this.prefManager = prefManager;
		try {
			if(prefManager == null || !prefManager.isValidFile())
				this.prefManager = generateDefaultRELOADPreferences();
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Error while reading preferences");
		}
	}
	
	public void reload() {
		try {
			if(prefManager == null || !prefManager.isValidFile())
				this.prefManager = generateDefaultRELOADPreferences();
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Error while reading preferences");
		}
	}
	
	public boolean updatePreference(String tag, String newValue, boolean createNew,  boolean updateFile){
		if(tag != null && (createNew || prefManager.hasPreference(tag))){
			prefManager.updatePreference(tag, newValue, createNew, updateFile);
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
		if(new File(loaderFile).exists())
			return new PreferencesManager(loaderFile);
		else if(new File(getLoaderFolder() + loaderFile).exists())
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
						metricList.add(Metric.fromString(readed.trim(), prefManager.getPreference(METRIC_TYPE).trim()));
					}
				}
			}
			reader.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		}
		try {
			Metric m = getTargetMetric();
			boolean found = false;
			for(Metric met : metricList){
				if(m.compareTo(met) == 0){
					found = true;
					break;
				}
			}
			if(!found)
				metricList.add(m);
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to process Target Metric");
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
			//AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + DATASETS_FOLDER + " not found. Using default value of ''");
			return ""; //checkFolder(new File("").getAbsoluteFile().getPath(), true);
		}
	}
	
	public String getLoaderFolder() {
		if(prefManager.hasPreference(LOADER_FOLDER))
			return checkFolder(prefManager.getPreference(LOADER_FOLDER));
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
	
	public MetricType getMetricType() {
		if(getTargetMetric() != null)
			return getTargetMetric().getMetricType();
		else return MetricType.FMEASURE;
	}

	/**
	 * Returns the metric.
	 *
	 * @return the metric
	 */
	public Metric getTargetMetric() {
		return Metric.fromString(prefManager.getPreference(METRIC), prefManager.getPreference(METRIC_TYPE).trim());
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
	
	public String getDetectionPreferencesFile() {
		try {
			if(prefManager.hasPreference(DETECTION_PREFERENCES_FILE) && new File(getInputFolder() + prefManager.getPreference(DETECTION_PREFERENCES_FILE)).exists())
				return prefManager.getPreference(DETECTION_PREFERENCES_FILE);
			else {
				AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
						DETECTION_PREFERENCES_FILE + " not found. Using default value of '" + DEFAULT_SCORING_PREF_FILE + "'");
				if(!new File(DEFAULT_SCORING_PREF_FILE).exists())
					generateDefaultScoringPreferences();
				return DEFAULT_SCORING_PREF_FILE;
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "");
		}
		return "";
	}
	
	public List<BasicConfiguration> loadConfiguration(LearnerType at, String datasetName) {
		return loadConfigurations(at, datasetName, true);
	}

	/**
	 * Loads the possible configurations for all the algorithms.
	 * @param sPolicy 
	 * @param windowSize 
	 *
	 * @return the map of the configurations
	 */
	public List<BasicConfiguration> loadConfigurations(LearnerType alg, String datasetName, boolean createMissing) {
		List<BasicConfiguration> confList = readConfigurationsFile(alg, datasetName);
		if((confList == null || confList.size() == 0) && createMissing && alg != null && alg instanceof BaseLearner){
			AppLogger.logInfo(getClass(), "Algorithm '" + alg + "' does not have an associated configuration file. Default will be created");
			generateConfigurationsFile(alg, DetectionAlgorithm.buildAlgorithm(alg, null, BasicConfiguration.buildConfiguration(alg)).getDefaultParameterValues());
			confList = readConfigurationsFile(alg, datasetName);
		}
		return confList;
	}
	
	private void generateCombinations(Map<String, String[]> confMap, int keyIndex, List<String> keyList, List<String> combinations){
		int nPrevious = 1, nAfter = 1;
		for(int i=0; i<keyList.size(); i++){
			if(i < keyIndex)
				nPrevious = nPrevious*confMap.get(keyList.get(i)).length;
			else if(i > keyIndex)
				nAfter = nAfter*confMap.get(keyList.get(i)).length;
		}
		int nOthers= nAfter*nPrevious;
		if(combinations == null)
			combinations = new LinkedList<String>();
		if(combinations.size() < confMap.get(keyList.get(keyIndex)).length*nOthers){
			for(int i=combinations.size(); i<confMap.get(keyList.get(keyIndex)).length*nOthers; i++){
				combinations.add("");
			}
		}
		int index = 0;
		for(int prev=0; prev<nPrevious; prev++){
			for(int itemIndex=0; itemIndex<confMap.get(keyList.get(keyIndex)).length; itemIndex++){
				for(int aft=0; aft<nAfter; aft++){
					String partial = combinations.remove(index);
					partial = partial + confMap.get(keyList.get(keyIndex))[itemIndex] + ", ";
					combinations.add(index, partial);
					index++;
				}
			}
		}
		keyIndex++;
		if(keyIndex < keyList.size())
			generateCombinations(confMap, keyIndex, keyList, combinations);
	}
	
	private void generateConfigurationsFile(LearnerType alg, Map<String, String[]> confMap) {
		File confFile;
		BufferedWriter writer = null;
		List<String> keyList = null;
		List<String> combinations = new LinkedList<String>();
		try {
			// Generating Combinations
			keyList = new ArrayList<String>(confMap.keySet());
			if(confMap != null && confMap.size() > 0)
				generateCombinations(confMap, 0, keyList, combinations);
			// Writing Combinations
			confFile = new File(getConfigurationFolder() + alg.toString() + ".conf");
			writer = new BufferedWriter(new FileWriter(confFile));
			writer.write("* Default Configuration file for algorithm '" + alg.toString() + "'\n\n");
			for(String tag : keyList){
				writer.write(tag + ",");
			}
			writer.write("\n");
			for(String combination : combinations){
				writer.write(combination + "\n");
			}
			AppLogger.logInfo(getClass(), "Default Configuration file '" + confFile.getName() + "' generated.");
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to generate configuration");
		} finally {
			try {
				writer.close();
			} catch (IOException ex) {
				AppLogger.logException(getClass(), ex, "Unable to generate configuration");
			}
		}
	}

	private List<BasicConfiguration> readConfigurationsFile(LearnerType mainLearner, String datasetName) {
		File confFolder = new File(getConfigurationFolder());
		List<BasicConfiguration> confList = new LinkedList<>();
		LearnerType fileLearner;
		String[] header = null;
		String readed;
		MetaConfiguration mConf = null;
		if(mainLearner instanceof MetaLearner){
			mConf = new MetaConfiguration(mainLearner);
			mConf.addItem(BasicConfiguration.SCORES_FOLDER, getScoresFolder());
			mConf.addItem(BasicConfiguration.FORCE_META_TRAINING, String.valueOf(getForceBaseLearnersFlag()));
			mConf.addItem(BasicConfiguration.K_FOLD, getKFoldCounter());
			mConf.addItem(BasicConfiguration.METRIC, getTargetMetric().getName());
			mConf.addItem(BasicConfiguration.REPUTATION, getReputation(getTargetMetric()).toString());
			mConf.addItem(BasicConfiguration.DATASET_NAME, datasetName);
			
		}
		if(confFolder != null && confFolder.exists() && confFolder.isDirectory()){
			for(File confFile : confFolder.listFiles()){
				if(confFile.exists() && confFile.getName().endsWith(".conf")){
					try {
						fileLearner = LearnerType.fromString(confFile.getName().substring(0, confFile.getName().indexOf(".")));
						if(fileLearner != null && (mainLearner.compareTo(fileLearner) == 0 
								|| (mConf != null && ((MetaLearner)mainLearner).hasLearner(fileLearner)))) {
							BufferedReader reader = new BufferedReader(new FileReader(confFile));
							// Eats the header
							while(reader.ready()){
								readed = reader.readLine();
								if(readed != null && readed.trim().length() > 0 && !readed.trim().startsWith("*")){
									header = readed.split(",");
									break;
								}
							}
							List<BasicConfiguration> partialList = new LinkedList<>();
							while(reader.ready()){
								readed = reader.readLine();
								if(readed != null){
									readed = readed.trim();
									if(readed.length() > 0 && !readed.startsWith("*")){
										int i = 0;
										BasicConfiguration alConf = BasicConfiguration.buildConfiguration(fileLearner);
										for(String element : readed.split(",")){
											alConf.addItem(header[i++], element);
										}
										partialList.add(alConf);
									}
								}
							}
							reader.close();
							AppLogger.logInfo(getClass(), "Found " + partialList.size() + " configuration for " + fileLearner + " algorithm");
							if(mConf == null){
								confList = partialList;
								break;
							} else {
								mConf.addConfiguration(fileLearner, partialList);
							}
						}
					} catch(Exception ex){
						AppLogger.logWarning(getClass(), "ConfigurationError", "File " + confFile.getPath() + " cannot be associated to any known algorithm");
					}
				} 
			}
			if(mConf != null){
				confList.add(mConf);
			}
		} else AppLogger.logError(getClass(), "FolderNotFoundError", "Folder '" + confFolder + "' not valid");
		return confList;
	}
	
	public void updateConfiguration(LearnerType algType, List<BasicConfiguration> confList) {
		BufferedWriter writer = null;
		File outFile = new File(getConfigurationFolder() + algType.toString() + ".conf");
		try {
			if(confList != null && confList.size() > 0) {
				writer = new BufferedWriter(new FileWriter(outFile));
				writer.write("* Configuration file for algorithm '" + algType.toString() + "'\n\n");
				for(String tag : confList.get(0).listLabels()){
					writer.write(tag + ",");
				}
				writer.write("\n");
				for(BasicConfiguration conf : confList){
					for(String tag : confList.get(0).listLabels()){
						writer.write(conf.getItem(tag) + ",");
					}
					writer.write("\n");
				}
				AppLogger.logInfo(getClass(), "Configuration file '" + outFile.getName() + "' updated.");
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read configurations");
		} finally {
			try {
				writer.close();
			} catch (IOException ex) {
				AppLogger.logException(getClass(), ex, "Unable to read configurations");
			}
		}
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
	
	public boolean getPredictMisclassificationsFlag() {
		if(prefManager.hasPreference(PREDICT_MISCLASSIFICATIONS))
			return !prefManager.getPreference(PREDICT_MISCLASSIFICATIONS).equals("0");
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					PREDICT_MISCLASSIFICATIONS + " not found. Using default value of 'yes'");
			return true;
		}
	}
	
	public boolean getEvaluationFlag() {
		if(prefManager.hasPreference(EVALUATION_NEEDED_FLAG))
			return !prefManager.getPreference(EVALUATION_NEEDED_FLAG).equals("0");
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					EVALUATION_NEEDED_FLAG + " not found. Using default value of '1'");
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
			return !prefManager.getPreference(OUTPUT_FORMAT).equals("basic");
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					OUTPUT_FORMAT + " not found. Using default value of 'not visible'");
			return false;
		}
	}
	
	public int getKFoldCounter() {
		if(prefManager.hasPreference(KFOLD_COUNTER) && AppUtility.isNumber(prefManager.getPreference(KFOLD_COUNTER)))
			return Integer.parseInt(prefManager.getPreference(KFOLD_COUNTER));
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					KFOLD_COUNTER + " not found. Using default value of '1'");
			return 1;
		}
	}
	
	public String getScoresFile(String prequel){
		if(prefManager.hasPreference(SCORES_FILE_FOLDER) && prefManager.hasPreference(SCORES_FILE))
			return getScoresFolder() + (prequel != null ? prequel + File.separatorChar : "") + prefManager.getPreference(SCORES_FILE);
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
		if(prefManager.hasPreference(INDICATOR_SELECTION))
			return prefManager.getPreference(INDICATOR_SELECTION);
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					INDICATOR_SELECTION + " not found. Using default value of 'PEARSON(0.9)'");
			return "PEARSON(0.9)";
		}
	}
	
	public String getDataSeriesBaseDomain() {
		String toRet = getDataSeriesDomain();
		if(toRet.contains("("))
			toRet = toRet.substring(0, toRet.indexOf("("));
		return toRet;
	}
	
	public static PreferencesManager generateDefaultRELOADPreferences() throws IOException {
		File prefFile = null;
		BufferedWriter writer = null;
		try {
			prefFile = new File(DEFAULT_GLOBAL_PREF_FILE);
			if(!prefFile.exists()){
				writer = new BufferedWriter(new FileWriter(prefFile));
				writer.write("* Default preferences file for 'RELOAD'. Comments with '*'.\n");
				writer.write("\n\n* Data Source - Loaders.\n" + 
						LOADER_FOLDER + " = input" + File.separatorChar + "loaders\n");
				writer.write("\n* Loaders folder.\n" + 
						LOADERS + " = \n");
				writer.write("\n* Datasets folder.\n" +
						DATASETS_FOLDER + " = \n");
				writer.write("\n* RELOAD Execution.\n\n");
				writer.write("\n* Perform Feature Selection (0 = NO, 1 = YES).\n" + 
						FILTERING_NEEDED_FLAG + " = 1\n");
				writer.write("\n* Perform Training (0 = NO, 1 = YES).\n" + 
						TRAIN_NEEDED_FLAG + " = 1\n");
				writer.write("\n* Perform Evaluation (0 = NO, 1 = YES).\n" + 
						EVALUATION_NEEDED_FLAG + " = 1\n");
				writer.write("\n* K for the K-Fold Evaluation (Default is 2).\n" + 
						KFOLD_COUNTER + " = 1\n");
				writer.write("\n* The scoring metric. Accepted values are FP, FN, TP, TN, PRECISION, RECALL, FSCORE(b), FMEASURE, FPR, FNR, MATTHEWS.\n" + 
						METRIC + " = FMEASURE\n");
				writer.write("\n* The metric type (absolute/relative). Applies only to FN, FP, TN, TP.\n" + 
						METRIC_TYPE + " = relative\n");
				writer.write("\n* Expected duration of injected faults (observations).\n" + 
						ANOMALY_WINDOW + " = 0\n");
				writer.write("\n* Flag which indicates if we expect more than one fault for each run\n" + 
						VALID_AFTER_INJECTION + " = true\n");
				writer.write("\n* Reputation Score. Accepted values are 'double value', BETA, FP, FN, TP, TN, PRECISION, RECALL, FSCORE(b), FMEASURE, FPR, FNR, MATTHEWS\n" + 
						REPUTATION_TYPE + " = 1.0\n");
				writer.write("\n* Strategy to aggregate indicators. Suggested is PEARSON(n), where 'n' is the minimum value of correlation that is accepted\n" + 
						INDICATOR_SELECTION + " = UNION\n");
				writer.write("\n* Strategy to slide windows. Accepted Values are FIFO, \n" + 
						SLIDING_POLICY + " = FIFO\n");
				writer.write("\n* Size of the sliding window buffer\n" + 
						SLIDING_WINDOW_SIZE + " = 20\n");
				writer.write("\n* Type of output produced by RELOAD. Accepted values are ui, basic, IMAGE, TEXT\n" + 
						OUTPUT_FORMAT + " = ui\n");
				writer.write("\n* Path Setup.\n\n");
				writer.write("\n* Input folder\n" + 
						INPUT_FOLDER + " = input\n");
				writer.write("\n* Output folder\n" + 
						OUTPUT_FOLDER + " = output\n");
				writer.write("\n* Configuration folder\n" + 
						CONF_FILE_FOLDER + " = configurations\n");
				writer.write("\n* Setup folder\n" + 
						SETUP_FILE_FOLDER + " = setup\n");
				writer.write("\n* Setup folder\n" + 
						SCORES_FILE_FOLDER + " = intermediate\n");
				writer.write("\n* Scores file\n" + 
						SCORES_FILE + " = scores.csv");
				writer.write("\n\n* Other Preference Files.\n");
				writer.write("\n* Detection Preferences\n" + 
						DETECTION_PREFERENCES_FILE + " = scoringPreferences.preferences\n");	
				writer.write("\n* Meta-Training Preferences for base-learners\n" + 
						FORCE_TRAINING_BASELEARNERS + " = 0\n");	
				writer.write("\n* Training Preferences for base-learners\n" + 
						FORCE_TRAINING + " = 0\n");	
				writer.write("\n* Predict Misclassifications\n" + 
						PREDICT_MISCLASSIFICATIONS + " = 0\n");	
			}
			new File("input").mkdir();
			new File("input" + File.separatorChar + "setup").mkdir();
			new File("input" + File.separatorChar + "loaders").mkdir();
			new File("input" + File.separatorChar + "configurations").mkdir();
		} catch(IOException ex){
			AppLogger.logException(InputManager.class, ex, "Error while generating RELOAD global preferences");
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
				writer.write("* Default scoring preferences file for 'RELOAD'. Comments with '*'.\n");
				writer.write("\nchecker_selection,voting_strategy");
				writer.write("\nBEST 1, 1");
				writer.write("\nFILTERED 10, HALF");
			}
		} catch(IOException ex){
			AppLogger.logException(InputManager.class, ex, "Error while generating RELOAD scoring preferences");
			throw ex;
		} finally {
			if(writer != null)
				writer.close();
		}
		return new PreferencesManager(getInputFolder() + DEFAULT_SCORING_PREF_FILE);
	}
	
	private PreferencesManager generateDefaultFeatureSelectionPreferences() throws IOException {
		File prefFile = null;
		BufferedWriter writer = null;
		try {
			prefFile = new File(checkFolder(getSetupFolder()) + DEFAULT_SCORING_FS_FILE);
			if(!prefFile.exists()){
				writer = new BufferedWriter(new FileWriter(prefFile));
				writer.write("* Default feature selection preferences for 'RELOAD'. Comments with '*'.\n");
				writer.write("* This file reports on the feature selection techniques to be applied. \n");
				writer.write("\nfeature_selection_strategy,threshold,ranked_flag\n");
				writer.write("\n" + FeatureSelectorType.INFORMATION_GAIN + ",3.0,true\n");
			}
		} catch(IOException ex){
			AppLogger.logException(InputManager.class, ex, "Error while generating RELOAD scoring preferences");
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
				writer.write("* Default algorithm preferences file for 'RELOAD'. Comments with '*'.\n");
				writer.write("* Uncomment the algorithms you want to use. Filtering uses ELKI_KMEANS by default.\n");
				for(AlgorithmType at : AlgorithmType.values()){
					if(at.equals(AlgorithmType.ELKI_KMEANS))
						writer.write("\n " + at + "\n");
					else writer.write("\n* " + at + "\n");
				}
			}
		} catch(IOException ex){
			AppLogger.logException(InputManager.class, ex, "Error while generating RELOAD algorithm preferences");
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
				writer.write("\n NoPrediction\n");
			}
		} catch(IOException ex){
			AppLogger.logException(InputManager.class, ex, "Error while generating RELOAD algorithm preferences");
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

	public boolean filteringResultExists(String datasetName) {
		return new File(getScoresFolder() + datasetName + File.separatorChar + datasetName + "_filtered.csv").exists();
	}

	public void removeDataset(String option) {
		String a = option.split("@")[1].trim();
		String b = a.split(" ")[0];
		removeFromFile(new File(getSetupFolder() + "loaderPreferences.preferences"), b.trim(), true);
	}	
	
	public void removeAlgorithm(LearnerType learnerType) {
		if(learnerType != null)
			removeAlgorithmFromFile(new File(getSetupFolder() + "algorithmPreferences.preferences"), learnerType, false);
	}
	
	private void removeAlgorithmFromFile(File file, LearnerType learnerType, boolean b) {
		// TODO Auto-generated method stub
		BufferedReader reader = null;
		BufferedWriter writer = null;
		String readed;
		boolean found = false;
		List<String> fileLines = new LinkedList<String>();
		try {
			if(file.exists()){
				reader = new BufferedReader(new FileReader(file));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						fileLines.add(readed);
						if(readed.length() > 0 && !readed.trim().startsWith("*")){
							try {
								LearnerType rowLearner = LearnerType.fromString(readed.trim());
								if(rowLearner != null && rowLearner.compareTo(learnerType) == 0){
									readed = fileLines.remove(fileLines.size()-1);
									readed = "* " + readed;
									fileLines.add(readed);
									found = true;
								}
							} catch(Exception ex){
								AppLogger.logError(getClass(), "UnrecognizedAlgorithm", "Unable to parse algorithm '" + readed + "'");
							}
						}
					}
				}
				reader.close();
				if(found){
					writer = new BufferedWriter(new FileWriter(file));
					for(String st : fileLines){
						writer.write(st + "\n");
					}
					writer.close();
				}
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		} 	
	}

	private void removeFromFile(File file, String toRemove, boolean partialMatching){
		BufferedReader reader = null;
		BufferedWriter writer = null;
		String readed;
		boolean found = false;
		List<String> fileLines = new LinkedList<String>();
		try {
			if(file.exists()){
				reader = new BufferedReader(new FileReader(file));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						fileLines.add(readed);
						if(readed.length() > 0 && !readed.trim().startsWith("*")){
							if(readed.equalsIgnoreCase(toRemove) || readed.replace(" ", "").equalsIgnoreCase(toRemove.replace(" ", "")) || (partialMatching && readed.endsWith(toRemove))){
								readed = fileLines.remove(fileLines.size()-1);
								readed = "* " + readed;
								fileLines.add(readed);
								found = true;
							}
						}
					}
				}
				reader.close();
				if(found){
					writer = new BufferedWriter(new FileWriter(file));
					for(String st : fileLines){
						writer.write(st + "\n");
					}
					writer.close();
				}
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		} 		
	}
	
	public void addDataset(String option) {
		addToFile(new File(getSetupFolder() + "loaderPreferences.preferences"), option);
	}	
	
	public void addAlgorithm(String option) {
		addToFile(new File(getSetupFolder() + "algorithmPreferences.preferences"), option);
	}

	private void addToFile(File file, String toAdd){
		BufferedReader reader = null;
		BufferedWriter writer = null;
		String readed;
		boolean found = false;
		List<String> fileLines = new LinkedList<String>();
		try {
			if(file.exists()){
				reader = new BufferedReader(new FileReader(file));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						fileLines.add(readed);
						if(readed.length() > 0){
							if(readed.trim().startsWith("*")){
								readed = readed.substring(1).trim();
								if(readed.equalsIgnoreCase(toAdd) || readed.equalsIgnoreCase(toAdd.replace("/", "\\")) || readed.equalsIgnoreCase(toAdd.replace("\\", "/"))){
									readed = fileLines.remove(fileLines.size()-1);
									readed = readed.substring(1).trim();
									fileLines.add(readed);
									found = true;
								}
							} else if(readed.equalsIgnoreCase(toAdd) || readed.equalsIgnoreCase(toAdd.replace("/", "\\")) || readed.equalsIgnoreCase(toAdd.replace("\\", "/")) || readed.equalsIgnoreCase(toAdd.replace(" ", ""))){
								found = true;
							}
						}
					}
				}
				reader.close();
				if(!found){
					fileLines.add(toAdd);
				}
				writer = new BufferedWriter(new FileWriter(file));
				for(String st : fileLines){
					writer.write(st + "\n");
				}
				writer.close();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		} 		
	}
	
	public List<PreferencesManager> readLoaders() {
		List<PreferencesManager> lList = new LinkedList<PreferencesManager>();
		File loadersFile = new File(getSetupFolder() + "loaderPreferences.preferences");
		PreferencesManager pManager;
		BufferedReader reader;
		String readed;
		try {
			if(loadersFile.exists()){
				reader = new BufferedReader(new FileReader(loadersFile));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*")){
							readed = readed.trim();
							if(readed.endsWith(".loader")){
								pManager = getLoaderPreferences(readed);
								if(pManager != null)
									lList.add(pManager);
							} else if(new File(getLoaderFolder() + readed).exists() && new File(getLoaderFolder() + readed).isDirectory()){
								readed = getLoaderFolder() + readed; 
								if(!readed.endsWith("" + File.separatorChar))
									readed = readed + File.separatorChar;
								for(File f : new File(readed).listFiles()){
									if(f.getName().endsWith(".loader")){
										lList.add(new PreferencesManager(readed + f.getName()));
									}
								}
							}
						}
					}
				}
				reader.close();
			} else {
				AppLogger.logError(getClass(), "MissingPreferenceError", "File " + 
						loadersFile.getPath() + " not found. Will be generated. Using default value of ''");
				BufferedWriter writer = new BufferedWriter(new FileWriter(loadersFile));
				writer.write("*Default loaders file for RELOAD \n");
				writer.write("*Add relative paths to loaders starting from loaders folder, or through GUI \n\n");
				writer.close();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read loaders list");
		}
		return lList;	
	}
	
	public Loader buildSingleLoader(PreferencesManager lPref, String loaderTag, int anomalyWindow, String runIdsString){
		String loaderType = lPref.getPreference(Loader.LOADER_TYPE);
		if(loaderType != null && loaderType.toUpperCase().contains("CSV"))
			return new CSVLoader(lPref, loaderTag, anomalyWindow, getDatasetsFolder(), runIdsString);
		else if(loaderType != null && loaderType.toUpperCase().contains("ARFF"))
			return new ARFFLoader(lPref, loaderTag, anomalyWindow, getDatasetsFolder(), runIdsString);
		else {
			AppLogger.logError(getClass(), "LoaderError", "Unable to parse loader '" + loaderType + "'");
			return null;
		} 
	}
	
	public Loader buildSingleLoader(PreferencesManager lPref, String loaderTag){
		String loaderType = lPref.getPreference(Loader.LOADER_TYPE);
		String runIdsString = lPref.getPreference(loaderTag.equals("validation") ? Loader.VALIDATION_PARTITION : Loader.TRAIN_PARTITION);
		if(loaderType != null && loaderType.toUpperCase().contains("CSV"))
			return new CSVLoader(lPref, loaderTag, getAnomalyWindow(), getDatasetsFolder(), runIdsString);
		else if(loaderType != null && loaderType.toUpperCase().contains("ARFF"))
			return new ARFFLoader(lPref, loaderTag, getAnomalyWindow(), getDatasetsFolder(), runIdsString);
		else {
			AppLogger.logError(getClass(), "LoaderError", "Unable to parse loader '" + loaderType + "'");
			return null;
		} 
	}
	
	public boolean isValid(PreferencesManager lPref){
		Loader lt = buildSingleLoader(lPref, "train");
		Loader lv = buildSingleLoader(lPref, "validation");
		return Loader.isValid(lt) && Loader.isValid(lv);	
	}
	
	/**
	 * Returns run IDs parsing a specific tag.
	 *
	 * @param runTag the run tag
	 * @return the list of IDs
	 */
	public List<Integer> readRunIds(String idPref){
		String from, to;
		LinkedList<Integer> idList = new LinkedList<Integer>();
		if(idPref != null && idPref.length() > 0){
			for(String id : idPref.split(",")){
				if(id.contains("-")){
					from = id.split("-")[0].trim();
					to = id.split("-")[1].trim();
					for(int i=Integer.parseInt(from);i<=Integer.parseInt(to);i++){
						idList.add(i);
					}
				} else idList.add(Integer.parseInt(id.trim()));
			}
		}
		return idList;
	}

	public PreferencesManager getLoaderPreferencesByName(String loaderString) {
		for(PreferencesManager pFile : readLoaders()){
			if(pFile.getFilename().endsWith(loaderString.trim())){
				return pFile;
			}
		}
		return null;
	}

	public PreferencesManager generateDefaultLoaderPreferences(String loaderName, LoaderType loaderType, String filename) {
		File file = createDefaultLoader(loaderName, loaderType, filename);
		return new PreferencesManager(file);
	}

	private File createDefaultLoader(String loaderName, LoaderType loaderType, String filename) {
		BufferedWriter writer = null;
		File lFile = new File(getLoaderFolder() + loaderName + ".loader");
		try {
				writer = new BufferedWriter(new FileWriter(lFile));
				
				writer.write("* Default loader file for '" + loaderName + "'. Comments with '*'.\n");
				
				writer.write("\n\n* Loader type (CSV, ARFF).\n" + 
						Loader.LOADER_TYPE + " = " + loaderType + "\n");
				
				writer.write("\n* Data Partitioning.\n\n");
				
				writer.write("\n* File Used for Training\n" +
						FileLoader.TRAIN_FILE + " = " + (filename != null && filename.trim().length() > 0 ? filename.trim() : "") + "\n");
				writer.write("\n* Train Runs.\n" + 
						FileLoader.TRAIN_PARTITION + " = 0 - 999\n");
				
				writer.write("\n* File Used for Validation\n" +
						FileLoader.VALIDATION_FILE + " = " + (filename != null && filename.trim().length() > 0 ? filename.trim() : "") + "\n");
				writer.write("\n* Validation Runs.\n" + 
						FileLoader.VALIDATION_PARTITION + " = 1000 - 1999\n");
				
				writer.write("\n* Faulty Tags.\n" + 
						FileLoader.FAULTY_TAGS + " = attack\n");
				writer.write("\n* Tags to Skip.\n" + 
						FileLoader.SKIP_ROWS + " = \n");
				
				writer.write("\n* Parsing Dataset.\n\n");
				
				writer.write("\n* Features to Skip\n" + 
						FileLoader.SKIP_COLUMNS + " = \n");
				writer.write("\n* Column Containing the 'Label' Feature\n" + 
						FileLoader.LABEL_COLUMN + " = 1\n");
				writer.write("\n* Size of Each Experiment.\n" + 
						FileLoader.BATCH_COLUMN + " = \n");	
				writer.write("\n* Size of Each Experiment.\n" + 
						FileLoader.EXPERIMENT_ROWS + " = \n");	
				
				writer.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to create loader '" + loaderName + "'");
		}
		return lFile;
	}
	
	/*public static String[] getIndicatorSelectionPolicies(){
		return new String[]{"NONE", "ALL", "UNION", "MULTIPLE_UNION", "PEARSON", "SIMPLE"};
	}*/

	public List<FeatureSelector> getFeatureSelectors() {
		List<FeatureSelector> fsList = new LinkedList<FeatureSelector>();
		File featuresFile = new File(getSetupFolder() + "featureSelection.preferences");
		FeatureSelector toAdd;
		BufferedReader reader;
		String[] splitted;
		String readed;
		try {
			if(!featuresFile.exists()){
				AppLogger.logInfo(getClass(), "Feature Selection preferences do not exist. Default file will be generated.");
				generateDefaultFeatureSelectionPreferences();
			}
			reader = new BufferedReader(new FileReader(featuresFile));
			while((readed = reader.readLine()).trim().length() == 0 || readed.startsWith("*"));
			while(reader.ready()){
				readed = reader.readLine();
				if(readed != null){
					readed = readed.trim();
					if(readed.length() > 0 && !readed.trim().startsWith("*")){
						readed = readed.trim();
						if(readed.contains(",")){
							splitted = readed.split(",");
							try {
								boolean rankThresholdFlag = splitted.length > 2 && splitted[2].trim().toUpperCase().equals("TRUE");
								toAdd = FeatureSelector.createSelector(FeatureSelectorType.valueOf(splitted[0].trim()), Double.valueOf(splitted[1].trim()), rankThresholdFlag);
								if(toAdd != null)
									fsList.add(toAdd);
							} catch(Exception ex){
								AppLogger.logError(getClass(), "FeatureSelectionStrategyError", "Unable to load '" + splitted[0] + "' strategy");
							}
						}
					}
				}
			}
			reader.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read loaders list");
		}
		return fsList;
	}

	public void updateFeatureSelectionPolicies(List<FeatureSelector> fsList) {
		BufferedWriter writer = null;
		try {
			if(fsList != null){
				writer = new BufferedWriter(new FileWriter(new File(getSetupFolder() + "featureSelection.preferences")));
				writer.write("* This file reports on the feature selection techniques to be applied\n");
				writer.write("\nfeature_selection_strategy,threshold,ranked_flag\n");
				for(FeatureSelector fs : fsList){
					writer.write(fs.getFeatureSelectorType() + "," + fs.getSelectorThreshold() + "," + fs.isRankedThreshold() + "\n");
				}
				writer.close();
			} else AppLogger.logInfo(getClass(), "Unable to update Feature Selection preferences");
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write Feature Selection scores file");
		}
	}
	
	/**
	 * Loads train scores.
	 * This is the outcome of some previous training phases.
	 *
	 * @return the list of AlgorithmVoters resulting from the read scores
	 */
	public int countAvailableModels(String scoresFileString) {
		List<AlgorithmModel> vList = loadAlgorithmModels(scoresFileString);
		if(vList != null)
			return vList.size();
		else return 0;
	}
	
	public static AlgorithmModel loadAlgorithmModel(String scoresFileString) {
		List<AlgorithmModel> list = loadAlgorithmModels(scoresFileString);
		if(list != null && list.size() > 0)
			return list.get(0);
		else return null;
	}
	
	/**
	 * Loads train scores.
	 * This is the outcome of some previous training phases.
	 *
	 * @return the list of AlgorithmVoters resulting from the read scores
	 */
	public static List<AlgorithmModel> loadAlgorithmModels(String scoresFileString) {
		if(new File(scoresFileString).isDirectory())
			return AlgorithmModel.fromFile(scoresFileString + File.separatorChar + "scores.csv");
		else return AlgorithmModel.fromFile(scoresFileString);
	}

	public String[] loadSelectedDataSeriesString(String baseFolder, String filename) {
		LinkedList<String> sSeries = new LinkedList<String>();
		String readed;
		BufferedReader reader = null;
		//File dsF = new File(getScoresFolder() + filePrequel + File.separatorChar + filePrequel + "_filtered.csv");
		File dsF = new File(baseFolder + filename + "_filtered.csv");
		try {
			reader = new BufferedReader(new FileReader(dsF));
			while((readed = reader.readLine()).trim().length() == 0 || readed.startsWith("*"));
			while(reader.ready()){
				readed = reader.readLine();
				if(readed != null){
					readed = readed.trim();
					if(readed.length() > 0 && !readed.trim().startsWith("*")){
						sSeries.add(readed.trim());
					}
				}
			}
			reader.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read Selected data Series");
		} 
		return sSeries.toArray(new String[sSeries.size()]);
	}
	
	public List<DataSeries> getSelectedSeries(String baseFolder, String filename) {
		String[] strings = loadSelectedDataSeriesString(baseFolder, filename);
		if(strings != null && strings.length > 0){
			return DataSeries.fromString(strings, false);
		} else return new LinkedList<DataSeries>();
	}
	
	public Map<DataSeries, Map<FeatureSelectorType, Double>> extractSelectedFeatures(String baseFolder, String filename, String datasetName) {
		BufferedReader reader;
		String readed;
		String[] header = null;
		String croppedDN = datasetName.substring(0, datasetName.indexOf("."));
		Map<DataSeries, Map<FeatureSelectorType, Double>> featureMap = new HashMap<>();
		List<DataSeries> sList = getSelectedSeries(baseFolder, filename);
		File file = new File(baseFolder + croppedDN + File.separatorChar + "featureScores_[" + datasetName + "].csv");
		try {
			if(file.exists()){
				reader = new BufferedReader(new FileReader(file));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*")){
							if(header == null){
								header = readed.split(",");
								for(int i=2;i<header.length;i++){
									String name = header[i].trim();
									featureMap.put(new DataSeries(new Indicator(name, Double.class)), new HashMap<>());
								}
							} else {
								String[] splitted = readed.split(",");
								if(splitted[0] != null && splitted[0].length() > 0){
									for(int i=2;i<splitted.length;i++){
										for(DataSeries ds : featureMap.keySet()){
											if(ds.toString().equals(header[i].trim())){
												featureMap.get(ds).put(FeatureSelectorType.valueOf(splitted[0]), AppUtility.isNumber(splitted[i]) ? Double.valueOf(splitted[i]) : Double.NaN);
												break;
											}
										}
									}
								}
							}
						}
					}
				}
				reader.close();
			} else {
				for(DataSeries ds : sList){
					featureMap.put(ds, new HashMap<>());
				}
			}
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to read Feature Selection scores file");
		}
		return featureMap;
	}
	
	public List<DataSeries> generateDataSeries(List<Knowledge> kList, String filename) {
		List<DataSeries> ds = createDataSeries(kList);
		saveFilteredSeries(ds, filename);
		return ds;
	}
	
	private void saveFilteredSeries(List<DataSeries> seriesList, String filename) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(filename)));
			writer.write("data_series,type\n");
			for(DataSeries ds : seriesList){
				writer.write(ds.toString() + "\n");			
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write series");
		}
	}
	
	public List<DataSeries> createDataSeries(List<Knowledge> kList) {
		return DataSeries.basicCombinations(Knowledge.getIndicators(kList));
	}

	public FeatureSelectionInfo loadFeatureSelectionInfo(String outFilePrequel) {
		return new FeatureSelectionInfo(new File(outFilePrequel));
	}

	public TrainInfo loadTrainInfo(String outFilePrequel) {
		return new TrainInfo(new File(outFilePrequel));
	}
	
	public ValidationInfo loadValidationInfo(String outFilePrequel) {
		return new ValidationInfo(new File(outFilePrequel));
	}

	public BasicConfiguration getMetaConfigurationFor(String datasetName, String mlName) {
		String readed;
		String[] header = null;
		BasicConfiguration acOut = null;
		BufferedReader reader = null;
		File mcFile = new File(getMetaFolder() + "meta_" + datasetName.trim() + "_" + mlName.trim() + "_scores.csv");
		try {
			if(mcFile.exists()){
				reader = new BufferedReader(new FileReader(mcFile));
				// Eats the header
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null && readed.trim().length() > 0 && !readed.trim().startsWith("*")){
						header = readed.split(",");
						break;
					}
				}
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();		
						if(readed.length() > 0 && !readed.startsWith("*")){
							int i = 0;
							acOut = BasicConfiguration.buildConfiguration(LearnerType.fromString(mlName.trim()), readed.split("§")[6]);
							for(String element : readed.split("§")){
								acOut.addItem(header[i++].trim(), element.trim());
							}
							break;
						}
					}
				}
				reader.close();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read Meta-Configuration");
		} 
		return acOut;
	}

	private String getMetaFolder() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasMetaLearning() {
		File voterFile = new File(getInputFolder() + getDetectionPreferencesFile());
		BufferedReader reader;
		String readed;
		try {
			if(voterFile.exists()){
				reader = new BufferedReader(new FileReader(voterFile));
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*") && readed.contains(",")){
							try {
								AlgorithmType.valueOf(readed.split(",")[1].trim());
								return true;
							} catch(Exception ex){ }
						}
					}
				}
				reader.close();
			} 
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read voting preferences");
		}
		return false;
	}

	public Reputation getReputation(Metric met) {
		return Reputation.fromString(prefManager.getPreference(REPUTATION_TYPE), met);
	}

	public boolean getForceBaseLearnersFlag() {
		if(prefManager.hasPreference(FORCE_TRAINING_BASELEARNERS))
			return !prefManager.getPreference(FORCE_TRAINING_BASELEARNERS).equals("0");
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					FORCE_TRAINING_BASELEARNERS + " not found. Using default value of 'no'");
			return false;
		}
	}

	public boolean getForceTrainingFlag() {
		if(prefManager.hasPreference(FORCE_TRAINING))
			return !prefManager.getPreference(FORCE_TRAINING).equals("0");
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					FORCE_TRAINING + " not found. Using default value of 'no'");
			return false;
		}
	}
	
	public boolean getParallelTrainingFlag() {
		if(prefManager.hasPreference(PARALLEL_TRAINING))
			return !prefManager.getPreference(PARALLEL_TRAINING).equals("0");
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					PARALLEL_TRAINING + " not found. Using default value of 'no'");
			return false;
		}
	}

	public static void copyScores(String fromFolder, String toFolder) {
		File from = new File(fromFolder);
		if(fromFolder != null && from.isDirectory()){
			File to = new File(toFolder);
			if(!to.exists())
				to.mkdirs();
			for(File sourceFile : from.listFiles()){
				copyFileUsingStream(sourceFile, new File(toFolder + File.separatorChar + sourceFile.getName()));
			}
		}
	}	
	
	private static void copyFileUsingStream(File source, File dest){
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	        is.close();
	        os.close();
	    } catch (IOException e) {
			AppLogger.logException(InputManager.class, e, "Unable to copy from " + source);
		} 
	}
	
	public Loader buildLoader(String loaderTag, PreferencesManager loaderPref){
		if(loaderPref != null){
			return buildLoader(loaderTag, loaderPref, getAnomalyWindow());
		} else return null;
	}
	
	private Loader buildLoader(String loaderTag, PreferencesManager loaderPref, int anomalyWindow){
		String runsString = loaderPref.getPreference(loaderTag.equals("validation") ? Loader.VALIDATION_PARTITION : Loader.TRAIN_PARTITION);
		if(runsString != null && runsString.length() > 0){
			return buildSingleLoader(loaderPref, loaderTag, anomalyWindow, runsString);
		} else AppLogger.logError(getClass(), "LoaderError", "Unable to find run preference");
		return null;
	}

	
	
}
