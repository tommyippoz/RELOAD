/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.featureselection.ChiSquaredFeatureRanker;
import ippoz.reload.featureselection.FeatureSelector;
import ippoz.reload.featureselection.InformationGainSelector;
import ippoz.reload.featureselection.J48Ranker;
import ippoz.reload.featureselection.OneRRanker;
import ippoz.reload.featureselection.PearsonFeatureSelector;
import ippoz.reload.featureselection.PrincipalComponentRanker;
import ippoz.reload.featureselection.RandomForestFeatureRanker;
import ippoz.reload.featureselection.ReliefFeatureSelector;
import ippoz.reload.info.FeatureSelectionInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Tommy
 *
 */
public class FeatureSelectorManager {
	
	private List<FeatureSelector> selectorsList;
	
	private List<DataSeries> selectedFeatures;
	
	private FeatureSelectionInfo fsInfo;
	
	private boolean predictFlag;
	
	public FeatureSelectorManager(List<FeatureSelector> selectorsList, boolean predictFlag){
		this.selectorsList = selectorsList;
		this.predictFlag = predictFlag;
		fsInfo = new FeatureSelectionInfo(selectorsList);
	}
	
	public List<DataSeries> selectFeatures(List<Knowledge> kList, String setupFolder, String datasetName){
		List<DataSeries> baselineSeries;
		BufferedWriter writer = null;
		try {
			baselineSeries = generateBaselineSeries(kList);
			selectedFeatures = new ArrayList<DataSeries>(baselineSeries);
			writer = new BufferedWriter(new FileWriter(new File(setupFolder + "featureScores_[" + datasetName + "].csv")));
			writer.write("* This file reports on the scores of each feature selection technique applied to the initial set of features\n");
			writer.write("\nfeature_selection_strategy,threshold,");
			for(DataSeries ds : baselineSeries){
				writer.write(ds.toString() + ",");
			}
			writer.write("\n");
			if(baselineSeries != null && selectorsList != null && selectorsList.size() > 0){
				if(predictFlag){
					reshapeSelectorsListForPrediction();
				}
				for(FeatureSelector fs : selectorsList){
					fs.applyFeatureSelection(selectedFeatures, kList);
					selectedFeatures = fs.getSelectedSeries();
					writer.write(fs.getFeatureSelectorType() + "," + fs.getSelectorThreshold() + "," + fs.getScoresStringFor(baselineSeries) + "\n");
					if(selectedFeatures.size() > 0)
						AppLogger.logInfo(getClass(), "Filter '" + fs.getSelectorName() + "': " + selectedFeatures.size() + " features are valid");
					else AppLogger.logError(getClass(), "FeatureSelectionError", "Filter '" + fs.getSelectorName() + "': " + selectedFeatures.size() + " no valid features: try relaxing threshold...");
				}
				if(predictFlag)
					calculatePrediction();
			} else AppLogger.logInfo(getClass(), "No Feature Selection technique will be applied");
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write Feature Selection scores file");
		}
		fsInfo.setSelectedFeatures(new DataSeries(selectedFeatures));
		return selectedFeatures;
	}
	
	private void reshapeSelectorsListForPrediction() {
		boolean hasChi = false;
		boolean hasRelief = false;
		boolean hasPearson = false;
		boolean hasInfo = false;
		boolean hasPCA = false;
		boolean hasRF = false;
		boolean hasJ48 = false;
		boolean hasOneR = false;
		for(FeatureSelector fs : selectorsList){
			if(fs instanceof ChiSquaredFeatureRanker){
				hasChi = true;
			}
			if(fs instanceof ReliefFeatureSelector){
				hasRelief = true;
			}
			if(fs instanceof PearsonFeatureSelector){
				hasPearson = true;
			}
			if(fs instanceof InformationGainSelector){
				hasInfo = true;
			}
			if(fs instanceof PrincipalComponentRanker){
				hasPCA = true;
			}
			if(fs instanceof RandomForestFeatureRanker){
				hasRF = true;
			}
			if(fs instanceof J48Ranker){
				hasJ48 = true;
			}
			if(fs instanceof OneRRanker){
				hasOneR = true;
			}
		}
		if(!hasChi){
			selectorsList.add(0, new ChiSquaredFeatureRanker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'ChiSquared' was added to predict misclassifications");
		}
		if(!hasRelief){
			selectorsList.add(0, new ReliefFeatureSelector(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'ReliefF' was added to predict misclassifications");
		}
		if(!hasPearson){
			selectorsList.add(0, new PearsonFeatureSelector(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'Pearson' was added to predict misclassifications");
		}
		if(!hasInfo){
			selectorsList.add(0, new InformationGainSelector(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'InformationGain' was added to predict misclassifications");
		}
		if(!hasPCA){
			selectorsList.add(0, new PrincipalComponentRanker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'PCA' was added to predict misclassifications");
		}
		if(!hasRF){
			selectorsList.add(0, new RandomForestFeatureRanker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'RandomForest' was added to predict misclassifications");
		}
		if(!hasJ48){
			selectorsList.add(0, new J48Ranker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'J48' was added to predict misclassifications");
		}
		if(!hasOneR){
			selectorsList.add(0, new OneRRanker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'OneR' was added to predict misclassifications");
		}
	}

	private static int[] FS_THR = {1, 3, 5, 10};
	
	private void calculatePrediction() {
		Map<String, Double> map = new TreeMap<>();
		for(FeatureSelector fs : selectorsList){
			if(fs instanceof ChiSquaredFeatureRanker){
				for(int n : FS_THR){
					map.put("CS_" + n, fs.getRankedAverageScore(n));
				}
			}
			if(fs instanceof ReliefFeatureSelector){
				for(int n : FS_THR){
					map.put("REL_" + n, fs.getRankedAverageScore(n));
				}
			}
			if(fs instanceof PearsonFeatureSelector){
				for(int n : FS_THR){
					map.put("P_" + n, fs.getRankedAverageScore(n));
				}
			}
			if(fs instanceof InformationGainSelector){
				for(int n : FS_THR){
					map.put("IG_" + n, fs.getRankedAverageScore(n));
				}
			}
			if(fs instanceof PrincipalComponentRanker){
				for(int n : FS_THR){
					map.put("PCA_" + n, fs.getRankedAverageScore(n));
				}
			}
			if(fs instanceof RandomForestFeatureRanker){
				for(int n : FS_THR){
					map.put("RF_" + n, fs.getRankedAverageScore(n));
				}
			}
			if(fs instanceof J48Ranker){
				for(int n : FS_THR){
					map.put("J48_" + n, fs.getRankedAverageScore(n));
				}
			}
			if(fs instanceof OneRRanker){
				for(int n : FS_THR){
					map.put("OR_" + n, fs.getRankedAverageScore(n));
				}
			}
		}
		try {
			fsInfo.setValuesToPredict(Arrays.asList(map.values()).toString());
			MisclassificationPrediction mp = new MisclassificationPrediction("R");
			fsInfo.setRPrediction(mp.scoreInstance(map));
			AppLogger.logInfo(getClass(), "Predicted R is " + fsInfo.getRPrediction());
			mp = new MisclassificationPrediction("F2");
			fsInfo.setF2Prediction(mp.scoreInstance(map));
			AppLogger.logInfo(getClass(), "Predicted F2 is " + fsInfo.getF2Prediction());
			mp = new MisclassificationPrediction("MCC");
			fsInfo.setMCCPrediction(mp.scoreInstance(map));
			AppLogger.logInfo(getClass(), "Predicted MCC is " + fsInfo.getMCCPrediction());
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Error wile predicting misclassifications");
		}
	}

	private List<DataSeries> generateBaselineSeries(List<Knowledge> kList) {
		if(kList != null && kList.size() > 0){
			return DataSeries.basicCombinations(kList.get(0).getIndicators());
		} else return null;
	}
	
	public void saveSelectedFeatures(String setupFolder, String filename) {
		BufferedWriter writer;
		try {
			fsInfo.printFile(new File(setupFolder + File.separatorChar + "featureSelectionInfo.info"));
			writer = new BufferedWriter(new FileWriter(new File(setupFolder + File.separatorChar + filename)));
			writer.write("data_series,");
			for(FeatureSelector fs : selectorsList){
				writer.write(fs.getSelectorName() + ",");
			}
			writer.write("\n");
			for(DataSeries ds : selectedFeatures){
				writer.write(ds.toString() + ",");
				for(FeatureSelector fs : selectorsList){
					writer.write((fs.getScoreFor(ds) != null ? fs.getScoreFor(ds) : "NaN") + ",");
				}
				writer.write("\n");
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write series");
		}
	}

	public void addLoaderInfo(Loader loader) {
		fsInfo.setRuns(loader.getRuns());
		fsInfo.setDataPoints(loader.getDataPoints());
	}
	
	private class MisclassificationPrediction {
		
		private RandomForest rf;
		
		private String targetMetric;
		
		private final String baseFile = "/MP_WEKA_";
		
		private static final int classIndex = 32;
		
		public MisclassificationPrediction(String targetMetric) throws Exception {
			this.targetMetric = targetMetric;
			rf = buildMetaClassifier();
			switch(targetMetric){
				case "P":
				case "R":
				case "F1":
				case "F2":
				case "MCC":
				case "ACC":
					rf.buildClassifier(getData(baseFile + targetMetric + ".arff"));
					break;
				default:
					AppLogger.logError(getClass(), "MetricError", "Unable to recognize '" + targetMetric + "' metric to predict");
			}
		}
		
		private Instances getData(String fileString) throws FileNotFoundException, IOException {
			File file = new File(fileString);
			Instances inst = null;
			if(file.exists()){
				inst = new Instances(new FileReader(file));
				inst.setClassIndex(classIndex);
			} else {
				InputStream is = getClass().getResourceAsStream(fileString);
				if(is != null){
					inst = new Instances(new InputStreamReader(is));
					inst.setClassIndex(classIndex);
				}
			}
			return inst;
		}

		private RandomForest buildMetaClassifier(){
			RandomForest rf = new RandomForest();
			rf.setNumIterations(100);
			rf.setBatchSize("10");
			rf.setMaxDepth(0);
			rf.setNumIterations(1000);
			rf.setNumFeatures(0);
			rf.setNumDecimalPlaces(2);
			return rf;
		}
		
		public double scoreInstance(Map<String, Double> map) throws Exception{
			Instance inst = buildInstance(map);
			return rf.classifyInstance(inst);
		}
		
		private Instance buildInstance(Map<String, Double> map){
			String st = "";
			Instances iList;
			try {
				st = "@relation pred_" + targetMetric + "\n\n";
				for(String tag : map.keySet()){
					st = st + "@attribute " + tag + " numeric\n";
				}
				st = st + "@attribute class numeric";
				st = st + "\n\n@data\n";
				for(String tag : map.keySet()){
					st = st + map.get(tag) + ",";
				}
				st = st + "0.0";
				iList = new Instances(new StringReader(st));
				iList.setClassIndex(classIndex);
				if(iList != null && iList.size() > 0)
					return iList.instance(0);
				else return null;
			} catch (IOException ex) {
				AppLogger.logException(getClass(), ex, "Error while converting snapshot to WEKA instance");
				return null;
			}
		}
		
	}
	
}
