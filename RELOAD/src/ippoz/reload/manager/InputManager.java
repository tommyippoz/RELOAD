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
import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.IndicatorDataSeries;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.knowledge.sliding.SlidingPolicy;
import ippoz.reload.commons.knowledge.sliding.SlidingPolicyType;
import ippoz.reload.commons.layers.LayerType;
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
import ippoz.reload.loader.MySQLLoader;
import ippoz.reload.metric.Metric;
import ippoz.reload.metric.MetricType;
import ippoz.reload.reputation.Reputation;
import ippoz.reload.voter.AlgorithmVoter;
import ippoz.reload.voter.ScoresVoter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

	public static final String OPTIMIZATION_NEEDED_FLAG = "OPTIMIZATION_FLAG";
	
	public static final String EVALUATION_NEEDED_FLAG = "EVALUATION_FLAG";
	
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
	
	public boolean updatePreference(String tag, String newValue, boolean updateFile){
		if(tag != null && prefManager.hasPreference(tag)){
			prefManager.updatePreference(tag, newValue, updateFile);
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
						metricList.add(Metric.fromString(readed.trim(), prefManager.getPreference(METRIC_TYPE).trim(), 
								prefManager.hasPreference(VALID_AFTER_INJECTION) ? Boolean.getBoolean(prefManager.getPreference(VALID_AFTER_INJECTION)) : true));
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
		return getTargetMetric().getMetricType();
	}

	/**
	 * Returns the metric.
	 *
	 * @return the metric
	 */
	public Metric getTargetMetric() {
		return Metric.fromString(prefManager.getPreference(METRIC), prefManager.getPreference(METRIC_TYPE).trim(), 
				prefManager.hasPreference(VALID_AFTER_INJECTION) ? Boolean.getBoolean(prefManager.getPreference(VALID_AFTER_INJECTION)) : true);
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
	
	public List<ScoresVoter> getScoresVoters(String datasetName) {
		File voterFile = new File(getInputFolder() + getDetectionPreferencesFile());
		List<ScoresVoter> voterList = new LinkedList<ScoresVoter>();
		BufferedReader reader;
		String readed;
		try {
			if(voterFile.exists()){
				reader = new BufferedReader(new FileReader(voterFile));
				// Eats the header
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null && readed.trim().length() > 0 && !readed.trim().startsWith("*")){
						break;
					}
				}
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && !readed.trim().startsWith("*") && readed.contains(",")){
							ScoresVoter voter = ScoresVoter.generateVoter(readed.split(",")[0].trim(), readed.split(",")[1].trim(), this, datasetName);
							if(voter != null)
								voterList.add(voter);
						}
					}
				}
				reader.close();
			} 
			if(!voterFile.exists() || (voterList != null && voterList.size() == 0)){
				AppLogger.logError(getClass(), "MissingPreferenceError", "File " + 
						voterFile.getPath() + " not found. Using default value of 'BEST 1 - 1'");
				voterList.add(ScoresVoter.generateVoter("BEST  1", "1"));
				generateDefaultScoringPreferences();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read data types");
		}
		return voterList;
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
	
	public List<BasicConfiguration> loadConfiguration(LearnerType at, String datasetName, Integer windowSize, SlidingPolicy sPolicy) {
		return loadConfigurations(at, datasetName, windowSize, sPolicy, true);
	}

	/**
	 * Loads the possible configurations for all the algorithms.
	 * @param sPolicy 
	 * @param windowSize 
	 *
	 * @return the map of the configurations
	 */
	public List<BasicConfiguration> loadConfigurations(LearnerType alg, String datasetName, Integer windowSize, SlidingPolicy sPolicy, boolean createMissing) {
		List<BasicConfiguration> confList = readConfigurationsFile(alg, datasetName, windowSize, sPolicy);
		if(createMissing && alg != null && alg instanceof BaseLearner){
			AppLogger.logInfo(getClass(), "Algorithm '" + alg + "' does not have an associated configuration file. Default will be created");
			generateConfigurationsFile(alg, DetectionAlgorithm.buildAlgorithm(alg, null, BasicConfiguration.buildConfiguration(alg)).getDefaultParameterValues());
			confList = readConfigurationsFile(alg, datasetName, windowSize, sPolicy);
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

	private List<BasicConfiguration> readConfigurationsFile(LearnerType mainLearner, String datasetName, Integer windowSize, SlidingPolicy sPolicy) {
		File confFolder = new File(getConfigurationFolder());
		List<BasicConfiguration> confList = new LinkedList<>();
		LearnerType fileLearner;
		String[] header = null;
		String readed;
		MetaConfiguration mConf = null;
		if(mainLearner instanceof MetaLearner){
			mConf = new MetaConfiguration(mainLearner);
			mConf.addItem(BasicConfiguration.K_FOLD, getKFoldCounter());
			mConf.addItem(BasicConfiguration.METRIC, getTargetMetric().getMetricName());
			mConf.addItem(BasicConfiguration.REPUTATION, getReputation(getTargetMetric()).toString());
			mConf.addItem(BasicConfiguration.DATASET_NAME, datasetName);
			
		}
		if(confFolder != null && confFolder.exists() && confFolder.isDirectory()){
			for(File confFile : confFolder.listFiles()){
				if(confFile.exists() && confFile.getName().endsWith(".conf")){
					try {
						fileLearner = LearnerType.fromString(confFile.getName().substring(0, confFile.getName().indexOf(".")));
						if(fileLearner != null && (mainLearner.compareTo(fileLearner) == 0 
								|| (mConf != null && ((MetaLearner)mainLearner).hasBaseLearner(fileLearner)))) {
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
										if(DetectionAlgorithm.isSliding(fileLearner)){
											alConf.addItem(BasicConfiguration.SLIDING_WINDOW_SIZE, windowSize);
											alConf.addItem(BasicConfiguration.SLIDING_POLICY, sPolicy.toString());
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
	
	public boolean getOptimizationFlag() {
		if(prefManager.hasPreference(OPTIMIZATION_NEEDED_FLAG))
			return !prefManager.getPreference(OPTIMIZATION_NEEDED_FLAG).equals("0");
		else {
			AppLogger.logError(getClass(), "MissingPreferenceError", "Preference " + 
					OPTIMIZATION_NEEDED_FLAG + " not found. Using default value of '1'");
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
						"LOADER_FOLDER = input" + File.separatorChar + "loaders\n");
				writer.write("\n* Loaders folder.\n" + 
						"LOADERS = iscx\n");
				writer.write("\n* Datasets folder.\n" +
						"DATASETS_FOLDER = \n");
				writer.write("\n* RELOAD Execution.\n\n");
				writer.write("\n* Perform Feature Selection (0 = NO, 1 = YES).\n" + 
						"FEATURE_SELECTION_FLAG = 1\n");
				writer.write("\n* Perform Training (0 = NO, 1 = YES).\n" + 
						"TRAIN_FLAG = 1\n");
				writer.write("\n* Perform Optimization (0 = NO, 1 = YES).\n" + 
						"OPTIMIZATION_FLAG = 1\n");
				writer.write("\n* Perform Evaluation (0 = NO, 1 = YES).\n" + 
						"EVALUATION_FLAG = 1\n");
				writer.write("\n* K for the K-Fold Evaluation (Default is 2).\n" + 
						"KFOLD_COUNTER = 2\n");
				writer.write("\n* The scoring metric. Accepted values are FP, FN, TP, TN, PRECISION, RECALL, FSCORE(b), FMEASURE, FPR, FNR, MATTHEWS.\n" + 
						"METRIC = FMEASURE\n");
				writer.write("\n* The metric type (absolute/relative). Applies only to FN, FP, TN, TP.\n" + 
						"METRIC_TYPE = relative\n");
				writer.write("\n* Expected duration of injected faults (observations).\n" + 
						"ANOMALY_WINDOW = 0\n");
				writer.write("\n* Sliding window policy.\n" + 
						"SLIDING_WINDOW_POLICY = FIFO\n");
				writer.write("\n* Flag which indicates if we expect more than one fault for each run\n" + 
						"VALID_AFTER_INJECTION = true\n");
				writer.write("\n* Reputation Score. Accepted values are 'double value', BETA, FP, FN, TP, TN, PRECISION, RECALL, FSCORE(b), FMEASURE, FPR, FNR, MATTHEWS\n" + 
						"REPUTATION = 1.0\n");
				writer.write("\n* Strategy to aggregate indicators. Suggested is PEARSON(n), where 'n' is the minimum value of correlation that is accepted\n" + 
						"INDICATOR_SELECTION = UNION\n");
				writer.write("\n* Strategy to slide windows. Accepted Values are FIFO, \n" + 
						"SLIDING_POLICY = FIFO\n");
				writer.write("\n* Size of the sliding window buffer\n" + 
						"SLIDING_WINDOW_SIZE = 20\n");
				writer.write("\n* Type of output produced by RELOAD. Accepted values are ui, basic, IMAGE, TEXT\n" + 
						"OUTPUT_TYPE = ui\n");
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
				writer.write("\nINFORMATION_GAIN,3.0,true\n");
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
				writer.write("\n Thresholds\n");
				writer.write("\n SScore(3)\n");
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
		if(loaderType != null && loaderType.toUpperCase().contains("MYSQL"))
			return new MySQLLoader(null, lPref, loaderTag, getConsideredLayers(), null);
		else if(loaderType != null && loaderType.toUpperCase().contains("CSV"))
			return new CSVLoader(lPref, loaderTag, anomalyWindow, getDatasetsFolder(), runIdsString);
		else if(loaderType != null && loaderType.toUpperCase().contains("ARFF"))
			return new ARFFLoader(lPref, loaderTag, anomalyWindow, getDatasetsFolder(), runIdsString);
		else {
			AppLogger.logError(getClass(), "LoaderError", "Unable to parse loader '" + loaderType + "'");
			return null;
		} 
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

	public PreferencesManager generateDefaultLoaderPreferences(String loaderName, LoaderType loaderType) {
		File file = createDefaultLoader(loaderName, loaderType);
		return new PreferencesManager(file);
	}

	private File createDefaultLoader(String loaderName, LoaderType loaderType) {
		BufferedWriter writer = null;
		File lFile = new File(getLoaderFolder() + loaderName);
		try {
			if(!lFile.exists()){
				writer = new BufferedWriter(new FileWriter(lFile));
				
				writer.write("* Default loader file for '" + loaderName + "'. Comments with '*'.\n");
				
				writer.write("\n\n* Loader type (CSV, ARFF).\n" + 
						"LOADER_TYPE = " + loaderType + "\n");
				writer.write("\n* * Investigated Data Layers (if any, NO_LAYER otherwise).\n" + 
						"CONSIDERED_LAYERS = NO_LAYER\n");
				
				writer.write("\n* Data Partitioning.\n\n");
				
				writer.write("\n* File Used for Training\n" +
						FileLoader.TRAIN_FILE + " = \n");
				writer.write("\n* Train Runs.\n" + 
						FileLoader.TRAIN_PARTITION + " = 1 - 10\n");
				writer.write("\n* Train Faulty Tags.\n" + 
						FileLoader.TRAIN_FAULTY_TAGS + " = attack\n");
				writer.write("\n* Train Runs.\n" + 
						FileLoader.TRAIN_SKIP_ROWS + " = \n");
				writer.write("\n* File Used for Validation\n" +
						FileLoader.VALIDATION_FILE + " = \n");
				writer.write("\n* Validation Runs.\n" + 
						FileLoader.VALIDATION_PARTITION + " = 1 - 10\n");
				writer.write("\n* Train Faulty Tags.\n" + 
						FileLoader.VALIDATION_FAULTY_TAGS + " = attack\n");
				writer.write("\n* Train Runs.\n" + 
						FileLoader.VALIDATION_SKIP_ROWS + " = \n");
				
				writer.write("\n* Parsing Dataset.\n\n");
				
				writer.write("\n* Features to Skip\n" + 
						FileLoader.SKIP_COLUMNS + " = 0\n");
				writer.write("\n* Column Containing the 'Label' Feature\n" + 
						FileLoader.LABEL_COLUMN + " = 1\n");
				writer.write("\n* Size of Each Experiment.\n" + 
						FileLoader.BATCH_COLUMN + " = 100\n");	
				
				writer.close();
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to create loader '" + loaderName + "'");
		}
		return lFile;
	}
	
	public static String[] getIndicatorSelectionPolicies(){
		return new String[]{"NONE", "ALL", "UNION", "MULTIPLE_UNION", "PEARSON", "SIMPLE"};
	}

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
	
	/**
	 * Loads train scores.
	 * This is the outcome of some previous training phases.
	 *
	 * @return the list of AlgorithmVoters resulting from the read scores
	 */
	public static List<AlgorithmModel> loadAlgorithmModelsFor(String scoresFileString, ScoresVoter voter) {
		File asFile = new File(scoresFileString);
		BufferedReader reader;
		BasicConfiguration conf;
		String[] splitted;
		List<AlgorithmModel> modelList = new LinkedList<AlgorithmModel>();
		String readed;
		String seriesString;
		try {
			if(asFile.exists()){
				reader = new BufferedReader(new FileReader(asFile));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null){
						readed = readed.trim();
						if(readed.length() > 0 && readed.indexOf("§") != -1){
							splitted = AppUtility.splitAndPurify(readed, "§");
							if(splitted.length > 4 && voter.checkAnomalyTreshold(Double.valueOf(splitted[3]))){
								AlgorithmModel am = AlgorithmModel.fromString(readed);
								if(am != null && voter.checkModel(am, modelList))
									modelList.add(am);
							} 
						}
					}
				}
				reader.close();
			} else AppLogger.logError(InputManager.class, "FileNotFound", "Unable to find '" + scoresFileString + "'");
		} catch(Exception ex){
			AppLogger.logException(InputManager.class, ex, "Unable to read scores");
		}
		return modelList;
	}
	
	public AlgorithmModel loadAlgorithmModel(String scoresFileString) {
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
	public List<AlgorithmModel> loadAlgorithmModels(String scoresFileString) {
		String scoresFile = getScoresFile(scoresFileString);
		return AlgorithmModel.fromFile(scoresFile);
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
									String name = header[i].split("#")[0].trim();
									String dataCat = header[i].split("#")[1].trim();
									featureMap.put(new IndicatorDataSeries(new Indicator(name, LayerType.NO_LAYER, Double.class), DataCategory.valueOf(dataCat)), new HashMap<>());
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
	
	public List<DataSeries> generateDataSeries(Map<KnowledgeType, List<Knowledge>> kMap, DataCategory[] cats, String filename) {
		List<DataSeries> ds = createDataSeries(kMap, cats);
		saveFilteredSeries(ds, filename);
		return ds;
	}
	
	public List<DataSeries> generateDataSeries(Map<KnowledgeType, List<Knowledge>> kMap, String filename) {
		List<DataSeries> ds = createDataSeries(kMap);
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
	
	public List<DataSeries> createDataSeries(Map<KnowledgeType, List<Knowledge>> kMap) {
		return DataSeries.basicCombinations(Knowledge.getIndicators(kMap), getDataTypes());
	}
	
	public List<DataSeries> createDataSeries(Map<KnowledgeType, List<Knowledge>> kMap, DataCategory[] cats) {
		return DataSeries.basicCombinations(Knowledge.getIndicators(kMap), cats);
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

	public PreferencesManager getMetaLearningPreferences(AlgorithmVoter av, String amString, String csvFilename, String loaderName) {
		File mll = createMetaLearningLoader(deriveExcludedModels(av, amString), csvFilename, loaderName);
		return new PreferencesManager(mll);
	}
	
	private String deriveExcludedModels(AlgorithmVoter av, String amString) {
		String toSkip = "";
		List<AlgorithmModel> okModels = new LinkedList<>(); 
		for(AlgorithmModel am : loadAlgorithmModels(amString)){
			if(av.checkModel(am, okModels))
				okModels.add(am);
			else toSkip = toSkip + am.getAlgorithmType() + "(" + am.getDataSeries().getCompactName() + "), ";
		}
		if(toSkip.length() > 1)
			return toSkip.substring(0, toSkip.length()-2);
		else return "";
	}

	private File createMetaLearningLoader(String toSkip, String csvFilename, String loaderName) {
		BufferedWriter writer = null;
		File lFolder = new File(getLoaderFolder() + "meta" + File.separatorChar);
		File lFile = new File(lFolder.getPath() + File.separatorChar + "meta_" + loaderName + ".loader");
		try {
			if(!lFolder.exists())
				lFolder.mkdirs();
			writer = new BufferedWriter(new FileWriter(lFile));
				
			writer.write("* Loader file for meta-learning. Comments with '*'.\n");
				
			writer.write("\n\n* Loader type (CSV, ARFF).\n" + 
					"LOADER_TYPE = CSV\n");
			writer.write("\n* * Investigated Data Layers (if any, NO_LAYER otherwise).\n" + 
					"CONSIDERED_LAYERS = NO_LAYER\n");
			
			writer.write("\n* Data Partitioning.\n\n");
			
			writer.write("\n* File Used for Training\n" +
					FileLoader.TRAIN_FILE + " = " + csvFilename + "\n");
			writer.write("\n* Train Runs.\n" + 
					FileLoader.TRAIN_PARTITION + " = 0 - 50\n");
			writer.write("\n* Train Faulty Tags.\n" + 
					FileLoader.TRAIN_FAULTY_TAGS + " = Attack\n");
			writer.write("\n* Train Runs.\n" + 
					FileLoader.TRAIN_SKIP_ROWS + " = \n");
			writer.write("\n* File Used for Validation\n" +
					FileLoader.VALIDATION_FILE + " = " + csvFilename + "\n");
			writer.write("\n* Validation Runs.\n" + 
					FileLoader.VALIDATION_PARTITION + " = 0 - 100\n");
			writer.write("\n* Train Faulty Tags.\n" + 
					FileLoader.VALIDATION_FAULTY_TAGS + " = Attack\n");
			writer.write("\n* Train Runs.\n" + 
					FileLoader.VALIDATION_SKIP_ROWS + " = \n");
			
			writer.write("\n* Parsing Dataset.\n\n");
			
			writer.write("\n* Features to Skip\n" + 
					FileLoader.SKIP_COLUMNS + " = " + toSkip.replace("@", "-") + "\n");
			writer.write("\n* Column Containing the 'Label' Feature\n" + 
					FileLoader.LABEL_COLUMN + " = label\n");
			writer.write("\n* Size of Each Experiment.\n" + 
					FileLoader.BATCH_COLUMN + " = 100\n");	
			
			writer.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to create loader for meta-learning");
		}
		return lFile;
	}

	public List<AlgorithmVoter> getMetaLearners(String datasetName) {
		List<AlgorithmVoter> ml = new LinkedList<>();
		List<ScoresVoter> svs = getScoresVoters(datasetName);
		if(svs != null && svs.size() > 0){
			for(ScoresVoter sv : svs){
				if(sv.isMetaLearner())
					ml.add((AlgorithmVoter)sv);
			}
		} 
		return ml;
	}

	public String getMetaFolder() {
		return "meta" + File.separatorChar;
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
		return Reputation.fromString(prefManager.getPreference(REPUTATION_TYPE), met, prefManager.getPreference(VALID_AFTER_INJECTION).equals("no") ? false : true);
	}	
	
}
