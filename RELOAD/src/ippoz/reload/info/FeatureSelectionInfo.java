/**
 * 
 */
package ippoz.reload.info;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.featureselection.FeatureSelector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class FeatureSelectionInfo {
	
	private static final String FS_SELECTORS = "FS Selection Strategies";
	
	private List<String> selectorsList;
	
	private static final String FS_SELECTED = "FS Selected Features";

	private DataSeries dataSeries;
	
	private static final String FS_NUM_SEL = "FS N Selected Features";
	
	private Integer nSelFeatures;
	
	private static final String FS_RUNS = "FS Runs";
	
	private String runs;
	
	private static final String FS_DATAPOINTS = "FS Number of Data Points";
	
	private Integer nDataPoints;
	
	private static final String FS_PREDMCC = "Predicted MCC";
	
	private double mccPred;
	
	private static final String FS_PREDF2 = "Predicted F2";
	
	private double f2Pred;
	
	private static final String FS_PREDR = "Predicted R";
	
	private double rPred;
	
	private static final String FS_PRED = "Values used to Predict";
	
	private String predString;
	
	public FeatureSelectionInfo() {
		selectorsList = null;
		dataSeries = null;
		nSelFeatures = null;
		runs = null;
		nDataPoints = null;
		mccPred = Double.NaN;
		f2Pred = Double.NaN;
		rPred = Double.NaN;
		predString = null;
	}
	
	public FeatureSelectionInfo(List<FeatureSelector> list) {
		this();
		if(list != null && list.size() > 0){
			selectorsList = new LinkedList<String>();
			for(FeatureSelector fs : list){
				if(fs != null)
					selectorsList.add(fs.getSelectorName() + "(" + fs.getSelectorThreshold() + (fs.isRankedThreshold() ? "-RANK" : "") + ")");
			}
		}
	}

	public FeatureSelectionInfo(File file) {
		HashMap<String, String> preferences;
		try {
			preferences = AppUtility.loadPreferences(file, null);
			if(preferences != null && !preferences.isEmpty()){
				if(preferences.containsKey(FS_SELECTORS) && preferences.get(FS_SELECTORS) != null && preferences.get(FS_SELECTORS).trim().length() > 0){
					selectorsList = new LinkedList<String>();
					if(preferences.get(FS_SELECTORS).contains(",")){
						String[] splitted = preferences.get(FS_SELECTORS).trim().replace("[", "").replace("]", "").split(",");
						for(String algString : splitted){
							selectorsList.add(algString.trim());
						}
					} else selectorsList.add(preferences.get(FS_SELECTORS).trim().replace("[", "").replace("]", ""));
				}
				if(preferences.containsKey(FS_SELECTED) && preferences.get(FS_SELECTED) != null && preferences.get(FS_SELECTED).trim().length() > 0){
					if(preferences.get(FS_SELECTED).contains(",")){
						String[] splitted = preferences.get(FS_SELECTED).trim().split(",");
						for(String algString : splitted){
							try {
								dataSeries = DataSeries.fromString(algString.trim());
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to decode dataseries '" + algString + "'");
							}
						}
					} else {
						try {
							dataSeries = DataSeries.fromString(preferences.get(FS_SELECTED).trim());
						} catch(Exception ex){
							AppLogger.logException(getClass(), ex, "Unable to decode dataseries '" + preferences.get(FS_SELECTED) + "'");
						}
					}
				}
				if(preferences.containsKey(FS_NUM_SEL) && preferences.get(FS_NUM_SEL) != null && AppUtility.isInteger(preferences.get(FS_NUM_SEL).trim())){
					nSelFeatures = Integer.parseInt(preferences.get(FS_NUM_SEL).trim());
				}
				if(preferences.containsKey(FS_RUNS) && preferences.get(FS_RUNS) != null){
					runs = preferences.get(FS_RUNS).trim().replace(",", ";");
				}
				if(preferences.containsKey(FS_DATAPOINTS) && preferences.get(FS_DATAPOINTS) != null && AppUtility.isInteger(preferences.get(FS_DATAPOINTS).trim())){
					nDataPoints = Integer.parseInt(preferences.get(FS_DATAPOINTS).trim());
				}
				if(preferences.containsKey(FS_PREDF2) && preferences.get(FS_PREDF2) != null && AppUtility.isNumber(preferences.get(FS_PREDF2).trim())){
					f2Pred = Double.parseDouble(preferences.get(FS_PREDF2).trim());
				}
				if(preferences.containsKey(FS_PREDMCC) && preferences.get(FS_PREDMCC) != null && AppUtility.isNumber(preferences.get(FS_PREDMCC).trim())){
					mccPred = Double.parseDouble(preferences.get(FS_PREDMCC).trim());
				}
				if(preferences.containsKey(FS_PREDR) && preferences.get(FS_PREDR) != null && AppUtility.isNumber(preferences.get(FS_PREDR).trim())){
					rPred = Double.parseDouble(preferences.get(FS_PREDR).trim());
				}
				if(preferences.containsKey(FS_PRED) && preferences.get(FS_PRED) != null){
					predString = preferences.get(FS_PRED).trim();
				}
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Error while loading feature selection info");
		}
	}

	public String toFileString(){
		return (selectorsList != null ? Arrays.toString(selectorsList.toArray()) : "").replace(",", ";").replace("[", "").replace("]", "") + "," + 
				(dataSeries != null ? dataSeries.getName() : "").replace(",", ";").replace("[", "").replace("]", "") + "," + 
				(nSelFeatures != null ? nSelFeatures : "") + "," + 
				(runs != null ? runs : "") + "," +
				(nDataPoints != null ? nDataPoints : "") + "," + rPred + "," + f2Pred + "," + mccPred + "," + 
				(predString != null ? predString.replace(",", ";") : "");
	}
	
	public static String getFileHeader(){
		return FS_SELECTORS + "," + FS_SELECTED + "," + FS_NUM_SEL  + "," + FS_RUNS + "," + 
				FS_DATAPOINTS + "," + FS_PREDR + "," + FS_PREDF2 + "," + FS_PREDMCC + "," + FS_PRED;
	}

	public void setSelectedFeatures(DataSeries selectedFeatures) {
		this.dataSeries = selectedFeatures;
		if(dataSeries != null)
			this.nSelFeatures = dataSeries.size();
	}
	
	public void printFile(File file) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("* INFO file generated at " + new Date() + " that reports on feature selection details\n"); 
			writer.write("\n* Feature selection strategies used\n" + FS_SELECTORS + " = " + (selectorsList != null ? Arrays.toString(selectorsList.toArray()) : "") + "\n");
			writer.write("\n* Selected Features\n" + FS_SELECTED + " = " + (dataSeries != null ? dataSeries.getName() : "") + "\n");
			writer.write("\n* Number of Selected Features\n" + FS_NUM_SEL + " = " + (nSelFeatures != null ? nSelFeatures : "") + "\n");
			writer.write("\n* Runs used for Feature Selection \n" + FS_RUNS + " = " + (runs != null ? runs : "") + "\n");
			writer.write("\n* Number of Data Points used for Feature Selection\n" + FS_DATAPOINTS + " = " + (nDataPoints != null ? nDataPoints : "") + "\n");
			writer.write("\n* Predicted R\n" + FS_PREDR + " = " + new DecimalFormat("#0.00").format(rPred).replace(",", ".") + "\n");
			writer.write("\n* Predicted F2\n" + FS_PREDF2 + " = " + new DecimalFormat("#0.00").format(f2Pred).replace(",", ".") + "\n");
			writer.write("\n* Predicted MCC\n" + FS_PREDMCC + " = " + new DecimalFormat("#0.00").format(mccPred).replace(",", ".") + "\n");
			writer.write("\n* Values Used to Predict\n" + FS_PRED + " = " + predString + "\n");
			writer.close();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to write feature selection INFO");
		} 		
	}

	public void setDataPoints(int size) {
		nDataPoints = size;
	}

	public void setRuns(String runs) {
		this.runs = runs;
	}

	public void setMCCPrediction(double scoreInstance) {
		this.mccPred = scoreInstance;
	}

	public double getMCCPrediction() {
		return mccPred;
	}
	
	public void setF2Prediction(double scoreInstance) {
		this.f2Pred = scoreInstance;
	}

	public double getF2Prediction() {
		return f2Pred;
	}
	
	public void setRPrediction(double scoreInstance) {
		this.rPred = scoreInstance;
	}

	public double getRPrediction() {
		return rPred;
	}
	
	public void setValuesToPredict(String str){
		predString = str;
	}
	
	public String getValuesToPredict(){
		return predString;
	}

	
}
