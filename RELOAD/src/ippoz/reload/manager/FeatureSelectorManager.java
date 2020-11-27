/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.commons.datacategory.DataCategory;
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
import java.util.List;

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
		if(!hasChi){
			selectorsList.add(0, new ReliefFeatureSelector(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'ReliefF' was added to predict misclassifications");
		}
		if(!hasChi){
			selectorsList.add(0, new PearsonFeatureSelector(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'Pearson' was added to predict misclassifications");
		}
		if(!hasInfo){
			selectorsList.add(0, new InformationGainSelector(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'InformationGain' was added to predict misclassifications");
		}
		if(!hasChi){
			selectorsList.add(0, new PrincipalComponentRanker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'PCA' was added to predict misclassifications");
		}
		if(!hasChi){
			selectorsList.add(0, new RandomForestFeatureRanker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'RandomForest' was added to predict misclassifications");
		}
		if(!hasJ48){
			selectorsList.add(0, new J48Ranker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'J48' was added to predict misclassifications");
		}
		if(!hasChi){
			selectorsList.add(0, new OneRRanker(1000, true));
			AppLogger.logInfo(getClass(), "Feature Selector 'OneR' was added to predict misclassifications");
		}
	}

	private void calculatePrediction() {
		double bestChiSQ = 0.0, bestRelief = 0.0, bestPearson = 0.0, bestInfoGain = 0.0, 
				bestPCA = 0.0, bestRF = 0.0, bestJ48 = 0.0, bestOneR = 0.0;
		for(FeatureSelector fs : selectorsList){
			if(fs instanceof ChiSquaredFeatureRanker){
				bestChiSQ = fs.getHighestScore();
			}
			if(fs instanceof ReliefFeatureSelector){
				bestRelief = fs.getHighestScore();
			}
			if(fs instanceof PearsonFeatureSelector){
				bestPearson = fs.getHighestScore();
			}
			if(fs instanceof InformationGainSelector){
				bestInfoGain = fs.getHighestScore();
			}
			if(fs instanceof PrincipalComponentRanker){
				bestPCA = fs.getHighestScore();
			}
			if(fs instanceof RandomForestFeatureRanker){
				bestRF = fs.getHighestScore();
			}
			if(fs instanceof J48Ranker){
				bestJ48 = fs.getHighestScore();
			}
			if(fs instanceof OneRRanker){
				bestOneR = fs.getHighestScore();
			}
		}
		try {
			fsInfo.setValuesToPredict(bestChiSQ + "," + bestRelief + "," + bestPearson + "," + 
					bestInfoGain + "," + bestPCA + "," + bestRF + "," + bestJ48 + "," + bestOneR);
			MisclassificationPrediction mp = new MisclassificationPrediction(0);
			fsInfo.setRPrediction(mp.scoreInstance(bestChiSQ, bestRelief, bestPearson, bestInfoGain, 
					bestPCA, bestRF, bestJ48, bestOneR));
			AppLogger.logInfo(getClass(), "Predicted R is " + fsInfo.getRPrediction());
			mp = new MisclassificationPrediction(1);
			fsInfo.setF2Prediction(mp.scoreInstance(bestChiSQ, bestRelief, bestPearson, bestInfoGain, 
					bestPCA, bestRF, bestJ48, bestOneR));
			AppLogger.logInfo(getClass(), "Predicted F2 is " + fsInfo.getF2Prediction());
			mp = new MisclassificationPrediction(2);
			fsInfo.setMCCPrediction(mp.scoreInstance(bestChiSQ, bestRelief, bestPearson, bestInfoGain, 
					bestPCA, bestRF, bestJ48, bestOneR));
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
		
		private final String F2File = "/DSN_WEKA_F2.arff";
		
		private final String MCCFile = "/DSN_WEKA_MCC.arff";
		
		private final String RFile = "/DSN_WEKA_R.arff";
		
		public MisclassificationPrediction(int targetMetric) throws Exception{
			rf = buildMetaClassifier();
			if(targetMetric == 0)
				rf.buildClassifier(getData(RFile));
			else if(targetMetric == 1)
				rf.buildClassifier(getData(F2File));
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
		
		public double scoreInstance(double chisq, double relief, double pearson, double infogain, double pca, double ranf, double j48, double oner) throws Exception{
			Instance inst = buildInstance(chisq, relief, pearson, infogain, pca, ranf, j48, oner);
			return rf.classifyInstance(inst);
		}
		
		private Instance buildInstance(double chisq, double relief, double pearson, double infogain, double pca, double rf, double j48, double oner){
			String st = "";
			Instances iList;
			try {
				st = "@relation predMCC_Full\n\n"
						+ "@attribute F1_1 numeric\n"
						+ "@attribute F2_1 numeric\n"
						+ "@attribute F3_1 numeric\n"
						+ "@attribute F4_1 numeric\n"
						+ "@attribute F5_1 numeric\n"
						+ "@attribute F6_1 numeric\n"
						+ "@attribute F7_1 numeric\n"
						+ "@attribute F8_1 numeric\n"
						+ "@attribute class numeric";
				st = st + "\n\n@data\n";
				st = st + chisq + ",";
				st = st + relief + ",";
				st = st + pearson + ",";
				st = st + infogain + ",";
				st = st + pca + ",";
				st = st + rf + ",";
				st = st + j48 + ",";
				st = st + oner + ",0.0";
				iList = new Instances(new StringReader(st));
				iList.setClassIndex(8);
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
