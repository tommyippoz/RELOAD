/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.MonitoredData;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.executable.DetectorMain;
import ippoz.reload.info.FeatureSelectionInfo;
import ippoz.reload.info.TrainInfo;
import ippoz.reload.info.ValidationInfo;
import ippoz.reload.manager.train.TrainerManager;
import ippoz.reload.metric.Metric;
import ippoz.reload.metric.result.MetricResult;
import ippoz.reload.output.DetectorOutput;
import ippoz.reload.reputation.Reputation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class DetectionManager.
 * This is the manager for the detection. Coordinates all the other managers, especially the ones responsible for training and validation phases.
 *
 * @author Tommy
 */
public class DetectionManager {
		
	/** The input manager. */
	protected InputManager iManager;
	
	/** The chosen metric. */
	protected Metric metric;
	
	/** The chosen reputation metric. */
	protected Reputation reputation;
	
	/** The algorithm types (SPS, Historical...). */
	protected LearnerType mainLearner;
	
	protected PreferencesManager loaderPref;
	
	private Loader trainLoader;
	
	private Loader evalLoader;
	
	private boolean filterFlag;
	
	/**
	 * Instantiates a new detection manager.
	 * @param loaderPref 
	 * @param filterFlag 
	 * @param sPolicy2 
	 * @param windowSize2 
	 *
	 * @param prefManager the main preference manager
	 */
	public DetectionManager(InputManager iManager, LearnerType algTypes, PreferencesManager loaderPref, Loader trainLoader, Loader evalLoader, boolean filterFlag) {
		this.iManager = iManager;
		this.mainLearner = algTypes;
		this.loaderPref = loaderPref;
		this.trainLoader = trainLoader;
		this.evalLoader = evalLoader;
		this.filterFlag = filterFlag;
		metric = iManager.getTargetMetric();
		reputation = iManager.getReputation(metric);
		if(new File(getDetectorOutputFolder()).exists()){
			for(File f : new File(getDetectorOutputFolder()).listFiles()){
				if(f.isFile())
					f.delete();
			}
		}
	}
	
	public String getDetectorOutputFolder(){
		 return iManager.getOutputFolder() + loaderPref.getCompactFilename() + File.separatorChar + mainLearner.toCompactString();
	}
	
	public Metric[] getMetrics() {
		return iManager.loadValidationMetrics();
	}

	public String getTag() {
		return getWritableTag().replace(",", " ").trim();
	}
	
	private String getWritableTag() {
		String tag = "";
		if(loaderPref != null && loaderPref.getFilename() != null)
			tag = tag + loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.'));
		tag = tag + ",";
		if(loaderPref != null && loaderPref.getPreference(Loader.VALIDATION_PARTITION) != null)
			tag = tag + loaderPref.getPreference(Loader.VALIDATION_PARTITION).replace(",", "");
		tag = tag + ",";
		if(mainLearner != null && mainLearner.toString() != null)
			tag = tag + mainLearner.toString().replace(",", "");
		return tag;
	}
	
	/**
	 * Check premises for the execution, such as MySQL server status.
	 *
	 * @return true, if premises are satisfied
	 */
	public boolean checkAssumptions(){
		if(loaderPref.getPreference(Loader.LOADER_TYPE) != null && 
				loaderPref.getPreference(Loader.LOADER_TYPE).equalsIgnoreCase("MYSQL") && 
					!AppUtility.isServerUp(3306)){
			AppLogger.logError(getClass(), "MySQLException", "MySQL is not running. Please activate it");
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a boolean targeting the need of training before evaluation.
	 *
	 * @return true if training is needed
	 */
	public boolean needTraining(){
		if(!iManager.getTrainingFlag())
			return false;
		else {
			if(iManager.getForceTrainingFlag()){
				return true;
			} else {
				String filename = "";
				if(mainLearner instanceof BaseLearner){
					filename = getDatasetName() + File.separatorChar + mainLearner.toString();
				} else {
					filename = getDatasetName() + File.separatorChar + mainLearner.toCompactString();
				}
				return !AlgorithmModel.trainResultExists(iManager.getScoresFile(filename));	
			}
		}
	}
	
	public boolean needFiltering() {
		return iManager.getFilteringFlag() && filterFlag;
	}
	
	public boolean needEvaluation() {
		return iManager.getEvaluationFlag();
	}

	/**
	 * Starts the train process.
	 */
	public void featureSelection(){
		List<Knowledge> kList;
		FeatureSelectorManager fsm;
		String scoresFolderName;
		Loader loader;
		try {
			if(needFiltering()) {
				scoresFolderName = iManager.getScoresFolder() + getDatasetName() + File.separatorChar;
				if(!new File(scoresFolderName).exists())
					new File(scoresFolderName).mkdirs();
				if(trainLoader == null)
					loader = iManager.buildLoader("train", loaderPref);
				else loader = trainLoader;
				if(Loader.isValid(loader)){
					kList = Knowledge.generateKnowledge(loader.fetch());
					fsm = new FeatureSelectorManager(iManager.getFeatureSelectors(), iManager.getPredictMisclassificationsFlag());
					fsm.selectFeatures(kList, scoresFolderName, loaderPref.getFilename());
					fsm.addLoaderInfo(loader);
					fsm.saveSelectedFeatures(scoresFolderName, getDatasetName() + "_filtered.csv");
				} else AppLogger.logError(getClass(), "UnvalidLoaderError", "Loader '" + (loader != null ? loader.getLoaderName() :"") + "' not specified correctly. Check if it is reachable and if train partition is specified correctly to include some anomalies to be used to calculate feature scores");
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to filter indicators");
		}
	}	
	
	public String getDatasetName(){
		return loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.'));
	}
	
	public String buildScoresFolder(){
		return iManager.getScoresFolder() + getDatasetName() + File.separatorChar + mainLearner.toCompactString();
	}
	
	/**
	 * Starts the train process.
	 */
	public void train(){
		TrainerManager tManager;
		List<Knowledge> kList;
		Loader loader;
		try {
			if(needTraining()) {
				if(trainLoader == null)
					loader = iManager.buildLoader("train", loaderPref);
				else loader = trainLoader;
				if(Loader.isValid(loader)){
					kList = generateKnowledge(loader.fetch());
					if(!new File(buildScoresFolder()).exists())
						new File(buildScoresFolder()).mkdirs();
					if(!iManager.filteringResultExists(loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.')))){
						iManager.generateDataSeries(kList, iManager.getScoresFolder() + getDatasetName() + File.separatorChar + getDatasetName() + "_filtered.csv");
					}
					tManager = new TrainerManager(iManager.getSetupFolder(), 
							iManager.getScoresFolder(), 
							loaderPref.getCompactFilename(), 
							iManager.getOutputFolder(), 
							kList, 
							iManager.loadConfigurations(mainLearner, getDatasetName(), true), 
							metric, 
							reputation, 
							mainLearner, 
							iManager.loadSelectedDataSeriesString(iManager.getScoresFolder(), getDatasetName() + File.separatorChar + getDatasetName()), 
							iManager.getKFoldCounter(),
							iManager.loadValidationMetrics(), iManager.getParallelTrainingFlag());
					tManager.addLoaderInfo(loader);
					tManager.train(buildScoresFolder());
					tManager.flush();
				} else AppLogger.logError(getClass(), "UnvalidLoaderError", "Loader '" + (loader != null ? loader.getLoaderName() : "") + "' not specified correctly. Check if it is reachable and if train partition is specified correctly to include some anomalies to be used to calculate optimal parameters values");
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to train detector");
		}
	}
	
	protected List<Knowledge> generateKnowledge(List<MonitoredData> expList) {
		List<Knowledge> kList = new ArrayList<>(expList.size());
		if(expList != null && !expList.isEmpty()){
			for(int i=0;i<expList.size();i++){
				kList.add(new Knowledge(expList.get(i)));
			}
			AppLogger.logInfo(getClass(), expList.size() + " runs loaded (K-Fold:" + iManager.getKFoldCounter() + ")");
		}
		return kList;
	}

	public void flush() {
		iManager = null;
	}

	/*public DetectorOutput evaluate(DetectorOutput optOut) {
		String scoresFileString = buildOutFilePrequel() + File.separatorChar + buildOutFilePrequel() + "_" + mainLearner.toString().substring(1, mainLearner.toString().length()-1);
		if(iManager.countAvailableModels(scoresFileString) > 0){
			if(optOut == null || optOut.getVoter() == null){
				return evaluateAll();
			} else {
				return optimizedEvaluation(optOut.getVoter());
			}
		} else {
			AppLogger.logError(getClass(), "NoVotersFound", "Unable to gather voters as result of train phase.");
			return null;
		}
	}*/
	
	public DetectorOutput evaluate() {
		MetricResult score = null;
		EvaluatorManager eManager;
		Metric[] metList = iManager.loadValidationMetrics();
		boolean printOutput = iManager.getOutputVisibility();
		Loader l = evalLoader;
		if(evalLoader == null)
			l = iManager.buildLoader("validation", loaderPref);
		if(Loader.isValid(l)){
			String scoresFileString = buildScoresFolder();
			if(iManager.countAvailableModels(scoresFileString) > 0){
				eManager = new EvaluatorManager(getDetectorOutputFolder(), scoresFileString, generateKnowledge(l.fetch()), metList, printOutput);
				if(eManager.detectAnomalies()){
					score = eManager.getMetricsValues().get(metric.getName());
					AppLogger.logInfo(getClass(), "Detection Executed. Obtained score is " + score.toString());
				}
				ValidationInfo vInfo = new ValidationInfo();
				vInfo.setLoaderName(l.getCompactName());
				vInfo.setDataPoints(l.getDataPoints());
				vInfo.setRuns(l.getRuns());
				vInfo.setValidationTime(eManager.getEvalTime());
				vInfo.setFaultRatio(eManager.getInjectionsRatio());
				vInfo.setModels(eManager.getModels());
				vInfo.setBestScore(score);
				vInfo.setMetricsString(eManager.getMetricsString());
				vInfo.setSeriesString(iManager.getSelectedSeries(iManager.getScoresFolder(), getDatasetName() + File.separatorChar + getDatasetName()));
				vInfo.setParamsString(eManager.getModel().getMainConfString());
				vInfo.printFile(new File(getDetectorOutputFolder() + File.separatorChar + "validationInfo.info"));
				return new DetectorOutput(iManager, mainLearner, score, 
						InputManager.loadAlgorithmModel(buildScoresFolder()),
						eManager.getDetailedEvaluations(),
						iManager.getSelectedSeries(iManager.getScoresFolder(), getDatasetName() + File.separatorChar + getDatasetName()), 
						iManager.extractSelectedFeatures(iManager.getScoresFolder(), getDatasetName() + File.separatorChar + getDatasetName(), loaderPref.getFilename()),		
						getWritableTag(),
						eManager.getInjectionsRatio(), 
						iManager.loadFeatureSelectionInfo(iManager.getScoresFolder() + getDatasetName() + File.separatorChar + "featureSelectionInfo.info"),
						iManager.loadTrainInfo(buildScoresFolder() + File.separatorChar + "trainInfo.info"),
						vInfo);
			
			} else {
				AppLogger.logError(getClass(), "NoVotersFound", "Unable to gather models as result of train phase.");
			}
		} else AppLogger.logError(getClass(), "UnvalidLoaderError", "Loader '" + (l != null ? l.getLoaderName() : "") + "' not specified correctly. Check if it is reachable and if validation partition is specified correctly to include some anomalies to be used to calculate metric scores");
		return null;
	}

	public String getModelsPath() {
		return getDatasetName() + File.separatorChar + getDatasetName() + "_" + mainLearner.toString().substring(1, mainLearner.toString().length()-1);
	}
	
	public void report(){
		File drFile = new File(DetectorMain.DEFAULT_REPORT_FILE);
		FeatureSelectionInfo fsInfo;
		TrainInfo tInfo;
		ValidationInfo vInfo;
		BufferedWriter writer;
		try {
			fsInfo = iManager.loadFeatureSelectionInfo(iManager.getScoresFolder() + getDatasetName() + File.separatorChar + "featureSelectionInfo.info");
			tInfo = iManager.loadTrainInfo(buildScoresFolder() + File.separatorChar + "trainInfo.info");
			vInfo = iManager.loadValidationInfo(getDetectorOutputFolder() + File.separatorChar + "validationInfo.info");
			if(!drFile.exists()){
				writer = new BufferedWriter(new FileWriter(drFile, false));
				writer.write("* Report for RELOAD activity on " + new Date(System.currentTimeMillis()) + "\n");
				writer.write(FeatureSelectionInfo.getFileHeader() + ",");
				writer.write(TrainInfo.getFileHeader() + ",");
				writer.write(ValidationInfo.getFileHeader());
				for(Metric met : iManager.loadValidationMetrics()){
					writer.write("," + met.getName());
				}
				writer.write("\n");
			} else {
				writer = new BufferedWriter(new FileWriter(drFile, true));
			}
			writer.write(fsInfo.toFileString() + ",");
			writer.write(tInfo.toFileString() + ",");
			writer.write(vInfo.toFileString() + ",");
			writer.write(vInfo.getMetricsValuesString() + "\n");
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(DetectorMain.class, ex, "Unable to report");
		}
	}
		
}
