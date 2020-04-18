/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.featureselection.FeatureSelector;
import ippoz.reload.info.FeatureSelectionInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class FeatureSelectorManager {
	
	private List<FeatureSelector> selectorsList;
	
	private List<DataSeries> selectedFeatures;
	
	private DataCategory[] dataTypes;
	
	private FeatureSelectionInfo fsInfo;
	
	public FeatureSelectorManager(List<FeatureSelector> selectorsList, DataCategory[] dataTypes){
		this.selectorsList = selectorsList;
		this.dataTypes = dataTypes;
		fsInfo = new FeatureSelectionInfo(selectorsList);
	}
	
	public List<DataSeries> selectFeatures(List<Knowledge> kList, String setupFolder, String datasetName){
		List<DataSeries> baselineSeries;
		BufferedWriter writer = null;
		try {
			baselineSeries = generateBaselineSeries(kList);
			selectedFeatures = baselineSeries;
			writer = new BufferedWriter(new FileWriter(new File(setupFolder + "featureScores_[" + datasetName + "].csv")));
			writer.write("* This file reports on the scores of each feature selection technique applied to the initial set of features\n");
			writer.write("\nfeature_selection_strategy,threshold,");
			for(DataSeries ds : baselineSeries){
				writer.write(ds.toString() + ",");
			}
			writer.write("\n");
			if(baselineSeries != null && selectorsList != null && selectorsList.size() > 0){
				for(FeatureSelector fs : selectorsList){
					fs.applyFeatureSelection(selectedFeatures, kList);
					selectedFeatures = fs.getSelectedSeries();
					writer.write(fs.getFeatureSelectorType() + "," + fs.getSelectorThreshold() + "," + fs.getScoresStringFor(baselineSeries) + "\n");
					if(selectedFeatures.size() > 0)
						AppLogger.logInfo(getClass(), "Filter '" + fs.getSelectorName() + "': " + selectedFeatures.size() + " features are valid");
					else AppLogger.logError(getClass(), "FeatureSelectionError", "Filter '" + fs.getSelectorName() + "': " + selectedFeatures.size() + " no valid features: try relaxing threshold...");
				}
			} else AppLogger.logInfo(getClass(), "No Feature Selection technique will be applied");
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write Feature Selection scores file");
		}
		fsInfo.setSelectedFeatures(selectedFeatures);
		return selectedFeatures;
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
	
}
