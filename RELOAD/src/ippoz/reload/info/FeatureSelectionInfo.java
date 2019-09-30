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

	private List<DataSeries> seriesList;
	
	private static final String FS_NUM_SEL = "FS N Selected Features";
	
	private Integer nSelFeatures;
	
	private static final String FS_NUM_COM = "FS N Combined Features";
	
	private Integer nComFeatures;
	
	private static final String FS_NUM_FIN = "FS N Final DataSeries";
	
	private Integer nFinFeatures;
	
	private static final String FS_AGGR_STRATEGY = "FS Aggregation Strategy";
	
	private String aggrStrategy;
	
	private static final String FS_RUNS = "FS Runs";
	
	private String runs;
	
	private static final String FS_DATAPOINTS = "FS Number of Data Points";
	
	private Integer nDataPoints;
	
	public FeatureSelectionInfo() {
		selectorsList = null;
		seriesList = null;
		nSelFeatures = null;
		nComFeatures = null;
		nFinFeatures = null;
		aggrStrategy = null;
		runs = null;
		nDataPoints = null;
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
					seriesList = new LinkedList<DataSeries>();
					if(preferences.get(FS_SELECTED).contains(",")){
						String[] splitted = preferences.get(FS_SELECTED).trim().split(",");
						for(String algString : splitted){
							try {
								seriesList.add(DataSeries.fromString(algString.trim(), false));
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to decode dataseries '" + algString + "'");
							}
						}
					} else {
						try {
							seriesList.add(DataSeries.fromString(preferences.get(FS_SELECTED).trim(), false));
						} catch(Exception ex){
							AppLogger.logException(getClass(), ex, "Unable to decode dataseries '" + preferences.get(FS_SELECTED) + "'");
						}
					}
				}
				if(preferences.containsKey(FS_NUM_SEL) && preferences.get(FS_NUM_SEL) != null && AppUtility.isInteger(preferences.get(FS_NUM_SEL).trim())){
					nSelFeatures = Integer.parseInt(preferences.get(FS_NUM_SEL).trim());
				}
				if(preferences.containsKey(FS_NUM_COM) && preferences.get(FS_NUM_COM) != null && AppUtility.isInteger(preferences.get(FS_NUM_COM).trim())){
					nComFeatures = Integer.parseInt(preferences.get(FS_NUM_COM).trim());
				}
				if(preferences.containsKey(FS_NUM_FIN) && preferences.get(FS_NUM_FIN) != null && AppUtility.isInteger(preferences.get(FS_NUM_FIN).trim())){
					nFinFeatures = Integer.parseInt(preferences.get(FS_NUM_FIN).trim());
				}
				if(preferences.containsKey(FS_AGGR_STRATEGY) && preferences.get(FS_AGGR_STRATEGY) != null){
					aggrStrategy = preferences.get(FS_AGGR_STRATEGY).trim();
				}
				if(preferences.containsKey(FS_RUNS) && preferences.get(FS_RUNS) != null){
					runs = preferences.get(FS_RUNS).trim();
				}
				if(preferences.containsKey(FS_DATAPOINTS) && preferences.get(FS_DATAPOINTS) != null && AppUtility.isInteger(preferences.get(FS_DATAPOINTS).trim())){
					nDataPoints = Integer.parseInt(preferences.get(FS_DATAPOINTS).trim());
				}
			}
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Error while loading feature selection info");
		}
	}

	public String toFileString(){
		return (selectorsList != null ? Arrays.toString(selectorsList.toArray()) : "").replace(",", ";").replace("[", "").replace("]", "") + "," + 
				(seriesList != null ? Arrays.toString(seriesList.toArray()) : "").replace(",", ";").replace("[", "").replace("]", "") + "," + 
				(nSelFeatures != null ? nSelFeatures : "") + "," + 
				(aggrStrategy != null ? aggrStrategy : "") + "," + 
				(nComFeatures != null ? nComFeatures : "") + "," + 
				(nFinFeatures != null ? nFinFeatures : "") + "," + 
				(runs != null ? runs : "") + "," +
				(nDataPoints != null ? nDataPoints : "");
	}
	
	public static String getFileHeader(){
		return FS_SELECTORS + "," + FS_SELECTED + "," + FS_NUM_SEL  + "," + 
				FS_AGGR_STRATEGY + "," + FS_NUM_COM + "," + FS_NUM_FIN + "," + FS_RUNS + "," + FS_DATAPOINTS;
	}

	public void setAggregationStrategy(String aggrStrat) {
		this.aggrStrategy = aggrStrat;
	}

	public void setCombinedFeatures(int size) {
		this.nComFeatures = size;
	}

	public void setFinalizedFeatures(int size) {
		this.nFinFeatures = size;
	}

	public void setSelectedFeatures(List<DataSeries> selectedFeatures) {
		this.seriesList = selectedFeatures;
		if(seriesList != null)
			this.nSelFeatures = seriesList.size();
	}
	
	public void printFile(File file) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write("* INFO file generated at " + new Date() + " that reports on feature selection details\n"); 
			writer.write("\n* Feature selection strategies used\n" + FS_SELECTORS + " = " + (selectorsList != null ? Arrays.toString(selectorsList.toArray()) : "") + "\n");
			writer.write("\n* Selected Features\n" + FS_SELECTED + " = " + (seriesList != null ? Arrays.toString(seriesList.toArray()) : "") + "\n");
			writer.write("\n* Number of Selected Features\n" + FS_NUM_SEL + " = " + (nSelFeatures != null ? nSelFeatures : "") + "\n");
			writer.write("\n* Feature Aggregation Strategy (NONE, SIMPLE, PEARSON, UNION, ALL)\n" + FS_AGGR_STRATEGY + " = " + (aggrStrategy != null ? aggrStrategy : "") + "\n");
			writer.write("\n* Number of Combined Features\n" + FS_NUM_COM + " = " + (nComFeatures != null ? nComFeatures : "") + "\n");
			writer.write("\n* Number of DataSeries\n" + FS_NUM_FIN + " = " + (nFinFeatures != null ? nFinFeatures : "") + "\n");
			writer.write("\n* Runs used for Feature Selection \n" + FS_RUNS + " = " + (runs != null ? runs : "") + "\n");
			writer.write("\n* Number of Data Points used for Feature Selection\n" + FS_DATAPOINTS + " = " + (nDataPoints != null ? nDataPoints : "") + "\n");
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
}
