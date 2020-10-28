/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.IndicatorDataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.externalutils.WEKAUtils;
import ippoz.reload.featureselection.ChiSquaredFeatureRanker;
import ippoz.reload.featureselection.FeatureSelector;
import ippoz.reload.featureselection.InformationGainSelector;
import ippoz.reload.featureselection.OneRRanker;
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
import java.util.LinkedList;
import java.util.List;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Instance;

/**
 * @author Tommy
 *
 */
public class FeatureSelectorManager {
	
	private List<FeatureSelector> selectorsList;
	
	private List<DataSeries> selectedFeatures;
	
	private DataCategory[] dataTypes;
	
	private FeatureSelectionInfo fsInfo;
	
	private boolean predictFlag;
	
	public FeatureSelectorManager(List<FeatureSelector> selectorsList, DataCategory[] dataTypes, boolean predictFlag){
		this.selectorsList = selectorsList;
		this.dataTypes = dataTypes;
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
		fsInfo.setSelectedFeatures(selectedFeatures);
		return selectedFeatures;
	}
	
	
	
	private void reshapeSelectorsListForPrediction() {
		boolean hasChi = false;
		boolean hasInfo = false;
		boolean hasOneR = false;
		for(FeatureSelector fs : selectorsList){
			if(fs instanceof ChiSquaredFeatureRanker)
				hasChi = true;
			if(fs instanceof InformationGainSelector)
				hasInfo = true;
			if(fs instanceof OneRRanker)
				hasOneR = true;
		}
		if(!hasInfo){
			selectorsList.add(0, new InformationGainSelector(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'InformationGain' was added to predict misclassifications");
		}
		if(!hasChi){
			selectorsList.add(0, new ChiSquaredFeatureRanker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'ChiSquared' was added to predict misclassifications");
		}
		if(!hasOneR){
			selectorsList.add(0, new OneRRanker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'OneR' was added to predict misclassifications");
		}
	}

	private void calculatePrediction() {
		double bestChiSQ = 0.0, bestInfoGain = 0.0, bestOneR = 0.0;
		for(FeatureSelector fs : selectorsList){
			if(fs instanceof ChiSquaredFeatureRanker){
				bestChiSQ = fs.getHighestScore();
			}
			if(fs instanceof InformationGainSelector){
				bestInfoGain = fs.getHighestScore();
			}
			if(fs instanceof OneRRanker){
				bestOneR = fs.getHighestScore();
			}
		}
		try {
			MisclassificationPrediction mp = new MisclassificationPrediction(true);
			fsInfo.setF1Prediction(mp.scoreInstance(bestChiSQ, bestInfoGain, bestOneR));
			AppLogger.logInfo(getClass(), "Predicted F1 is " + fsInfo.getF1Prediction());
			mp = new MisclassificationPrediction(false);
			fsInfo.setMCCPrediction(mp.scoreInstance(bestChiSQ, bestInfoGain, bestOneR));
			AppLogger.logInfo(getClass(), "Predicted MCC is " + fsInfo.getMCCPrediction());
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Error wile predicting misclassifications");
		}
	}

	private List<DataSeries> generateBaselineSeries(List<Knowledge> kList) {
		if(kList != null && kList.size() > 0){
			return DataSeries.basicCombinations(kList.get(0).getIndicators(), dataTypes);
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
		
		private final String F1File = "resources" + File.separatorChar + "AllDataToModel_F1_3Att.arff";
		
		private final String MCCFile = "resources" + File.separatorChar + "AllDataToModel_MCC_3Att.arff";
		
		public MisclassificationPrediction(boolean targetF1) throws Exception{
			rf = buildMetaClassifier();
			if(targetF1)
				rf.buildClassifier(getData(F1File));
			else rf.buildClassifier(getData(MCCFile));
		}
		
		private Instances getData(String fileString) throws FileNotFoundException, IOException {
			File file = new File(fileString);
			Instances inst = null;
			if(file.exists()){
				inst = new Instances(new FileReader(file));
				inst.setClassIndex(3);
			} else {
				InputStream is = getClass().getResourceAsStream(fileString);
				if(is != null){
					inst = new Instances(new InputStreamReader(is));
					inst.setClassIndex(3);
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
		
		private Instance buildInstance(double chisq, double infoGain, double oner){
			String st = "";
			Instances iList;
			try {
				st = "@relation predMCC_Full\n\n@attribute F1_1 numeric\n@attribute F4_1 numeric\n@attribute F7_1 numeric\n@attribute class numeric";
				st = st + "\n\n@data\n";
				st = st + chisq + ",";
				st = st + infoGain + ",";
				st = st + oner + ",0.0";
				iList = new Instances(new StringReader(st));
				iList.setClassIndex(3);
				if(iList != null && iList.size() > 0)
					return iList.instance(0);
				else return null;
			} catch (IOException ex) {
				AppLogger.logException(getClass(), ex, "Error while converting snapshot to WEKA instance");
				return null;
			}
		}
		
		public double scoreInstance(double chisq, double infoGain, double oner) throws Exception{
			return rf.classifyInstance(buildInstance(chisq, infoGain, oner));
		}
		
	}
	
}
