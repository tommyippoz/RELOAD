/**
 * 
 */
package ippoz.reload.manager;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.FractionDataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.dataseries.SumDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.featureselection.FeatureSelector;
import ippoz.utils.logging.AppLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class FeatureSelectorManager {
	
	private List<FeatureSelector> selectorsList;
	
	private List<DataSeries> selectedFeatures;
	
	private List<DataSeries> combinedFeatures;
	
	private List<DataSeries> finalizedFeatures;
	
	private DataCategory[] dataTypes;
	
	public FeatureSelectorManager(List<FeatureSelector> selectorsList, DataCategory[] dataTypes){
		this.selectorsList = selectorsList;
		this.dataTypes = dataTypes;
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
					AppLogger.logInfo(getClass(), "Filter '" + fs.getSelectorName() + "': " + selectedFeatures.size() + " data series are valid");
				}
			} else AppLogger.logInfo(getClass(), "No Feature Selection technique will be applied");
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write Feature Selection scores file");
		}
		return selectedFeatures;
	}
	
	private List<DataSeries> generateBaselineSeries(List<Knowledge> kList) {
		if(kList != null && kList.size() > 0){
			return DataSeries.basicCombinations(kList.get(0).getIndicators(), dataTypes);
		} else return null;
	}

	public List<DataSeries> combineSelectedFeatures(List<Knowledge> kList, String dsDomain, String setupFolder){
		combinedFeatures = new LinkedList<DataSeries>();
		if(dsDomain.equals("ALL")){
			allCombinations();
		} else if(dsDomain.equals("UNION")){
			unionCombinations();
		} else if(dsDomain.contains("PEARSON") && dsDomain.contains("(") && dsDomain.contains(")")){
			double pearsonSimple = Double.valueOf(dsDomain.substring(dsDomain.indexOf("(")+1, dsDomain.indexOf(")")));
			pearsonCombinations(kList, pearsonSimple, setupFolder);
		}
		AppLogger.logInfo(getClass(), "Combined Data Series : " + combinedFeatures.size());
		return combinedFeatures;
	}
	
	public List<DataSeries> finalizeSelection(String dsDomain){
		finalizedFeatures = new LinkedList<DataSeries>();
		if(dsDomain.equals("ALL")){
			finalizedFeatures.addAll(selectedFeatures);
			finalizedFeatures.addAll(combinedFeatures);
		} else if(dsDomain.equals("UNION")){
			finalizedFeatures.addAll(combinedFeatures);
		} else if(dsDomain.contains("PEARSON") && dsDomain.contains("(") && dsDomain.contains(")")){
			finalizedFeatures.addAll(selectedFeatures);
			finalizedFeatures.addAll(combinedFeatures);
		}
		AppLogger.logInfo(getClass(), "Finalized Data Series : " + finalizedFeatures.size());
		return combinedFeatures;
	}
	
	private void allCombinations(){
		for(int i=0;i<selectedFeatures.size();i++){
			for(int j=i+1;j<selectedFeatures.size();j++){
				if(!selectedFeatures.get(i).getName().equals(selectedFeatures.get(j).getName())){
					combinedFeatures.add(new SumDataSeries(selectedFeatures.get(i), selectedFeatures.get(j), DataCategory.PLAIN));
					combinedFeatures.add(new FractionDataSeries(selectedFeatures.get(i), selectedFeatures.get(j), DataCategory.PLAIN));
					combinedFeatures.add(new MultipleDataSeries(selectedFeatures.get(i), selectedFeatures.get(j)));
				}
			}
		}
	}
	
	public void unionCombinations() {
		List<DataSeries> simpleIndPlain = new LinkedList<DataSeries>();
		List<DataSeries> simpleIndDiff = new LinkedList<DataSeries>();
		for(DataSeries ds : selectedFeatures){
			if(ds.getDataCategory() == DataCategory.PLAIN)
				simpleIndPlain.add(ds);
			else simpleIndDiff.add(ds);
		}
		combinedFeatures.add(new MultipleDataSeries(simpleIndPlain));
		combinedFeatures.add(new MultipleDataSeries(simpleIndDiff));
	}
	
	private void pearsonCombinations(List<Knowledge> kList, double pearsonThreshold, String setupFolder) {
		PearsonCombinationManager pcManager;
		File pearsonFile = new File(setupFolder + "pearsonCombinations.csv");
		pcManager = new PearsonCombinationManager(pearsonFile, selectedFeatures, kList);
		pcManager.calculatePearsonIndexes(pearsonThreshold);
		combinedFeatures = pcManager.getPearsonCombinedSeries();
		pcManager.flush();
	}
	
	public void saveFilteredSeries(String setupFolder, String filename) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File(setupFolder + File.separatorChar + filename)));
			writer.write("data_series,type\n");
			for(DataSeries ds : finalizedFeatures){
				writer.write(ds.toString() + "\n");			
			}
			writer.close();
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write series");
		}
	}
	
}
