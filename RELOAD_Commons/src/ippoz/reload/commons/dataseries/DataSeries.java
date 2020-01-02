/**
 * 
 */
package ippoz.reload.commons.dataseries;

import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.knowledge.snapshot.SnapshotValue;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.service.IndicatorStat;
import ippoz.reload.commons.service.ServiceCall;
import ippoz.reload.commons.service.ServiceStat;
import ippoz.reload.commons.service.StatPair;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class DataSeries implements Comparable<DataSeries> {
	
	private static final int COMPACT_STRING_SIZE_LIMIT = 60;

	private String seriesName;
	
	private DataCategory dataCategory;
	
	private double dsRank;
	
	protected DataSeries(String seriesName, DataCategory dataCategory) {
		this.seriesName = seriesName;
		this.dataCategory = dataCategory;
		this.dsRank = Double.NaN;
	}

	@Override
	public String toString() {
		return seriesName + "#" + dataCategory + "#" + getLayerType();
	}

	public String getName() {
		return seriesName;
	}

	public DataCategory getDataCategory() {
		return dataCategory;
	}
	
	public LinkedList<DataSeries> listSubSeries(){
		LinkedList<DataSeries> outList = new LinkedList<DataSeries>();
		if(this instanceof ComplexDataSeries){
			outList.addAll(((ComplexDataSeries)this).getFirstOperand().listSubSeries());
			outList.addAll(((ComplexDataSeries)this).getSecondOperand().listSubSeries());
		} else outList.add(this); 
		return outList;
	}
	
	public boolean contains(DataSeries other){
		if(this instanceof MultipleDataSeries)
			return DataSeries.isIn(((MultipleDataSeries)this).getSeriesList(), other);
		else if(this instanceof ComplexDataSeries)
			return ((ComplexDataSeries)this).getFirstOperand().contains(other) || ((ComplexDataSeries)this).getSecondOperand().contains(other);
		else return compareTo(other) == 0;
	}

	@Override
	public int compareTo(DataSeries other) {
		if(seriesName.equals(other.getName()) && dataCategory.equals(other.getDataCategory()))
			return 0;
		else if(dsRank != Double.NaN && other.getRank() != Double.NaN){
			return dsRank > other.getRank() ? -1 : 1;
		} else if(dsRank != Double.NaN)
			return -1;
		else return 1;
	}
	
	public SnapshotValue getSeriesValue(Observation obs){
		try {
			switch(dataCategory){
				case PLAIN:
					return getPlainSeriesValue(obs);
				case DIFFERENCE:
					return getDiffSeriesValue(obs);
				default:
					return null;
			}
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "CastError");
		}
		return null;
	}

	public abstract LayerType getLayerType();
	
	public abstract boolean compliesWith(AlgorithmType algType);
	
	protected abstract SnapshotValue getPlainSeriesValue(Observation obs);
	
	protected abstract SnapshotValue getDiffSeriesValue(Observation obs);
	
	// Sincronizza anche se è all'inizio, nel corpo o alla fine.
	public abstract StatPair getSeriesServiceStat(Date timestamp, ServiceCall sCall, ServiceStat sStat);

	protected static StatPair getPairByTime(Date timestamp, ServiceCall sCall, IndicatorStat iStat){
		if(sCall.isAliveAt(timestamp)){
			if(sCall.getStartTime().equals(timestamp))
				return iStat.getFirstObs();
			else if(sCall.getStartTime().before(timestamp) && sCall.getEndTime().after(timestamp))
				return iStat.getAllObs();
			else if(sCall.getEndTime().equals(timestamp))
				return iStat.getLastObs();
		}
		return null;
	}
	
	public static DataSeries fromString(String stringValue, boolean show) {
		try {
			if(stringValue != null && stringValue.length() > 0){
				String layer = stringValue.substring(stringValue.lastIndexOf("#")+1);
				String partial = stringValue.substring(0, stringValue.indexOf(layer)-1);
				String dataType = partial.substring(partial.lastIndexOf("#")+1);
				String dataSeries = stringValue.substring(0, partial.lastIndexOf("#"));
				return fromStrings(dataSeries, DataCategory.valueOf(dataType), LayerType.valueOf(layer));
			}
		} catch(Exception ex){
			if(show)
				AppLogger.logError(DataSeries.class, "ParseError", "Unable to parse '" + stringValue + "' dataseries");
		}
		return null;
	}
	
	public static DataSeries fromStrings(String seriesName, DataCategory dataType, LayerType layerType) {
		List<DataSeries> sList;
		if(layerType.equals(LayerType.COMPOSITION)){
			if(seriesName.contains(")*(")){
				return new ProductDataSeries(DataSeries.fromString(seriesName.substring(1,  seriesName.indexOf(")*(")).trim(), true), DataSeries.fromString(seriesName.substring(seriesName.indexOf(")*(")+3, seriesName.length()-1).trim(), true), dataType);
			} else if(seriesName.contains(")/(")){
				return new FractionDataSeries(DataSeries.fromString(seriesName.substring(1,  seriesName.indexOf(")/(")).trim(), true), DataSeries.fromString(seriesName.substring(seriesName.indexOf(")/(")+3, seriesName.length()-1).trim(), true), dataType);
			} else if(seriesName.contains(")+(")){
				return new SumDataSeries(DataSeries.fromString(seriesName.substring(1,  seriesName.indexOf(")+(")).trim(), true), DataSeries.fromString(seriesName.substring(seriesName.indexOf(")+(")+3, seriesName.length()-1).trim(), true), dataType);
			} else if(seriesName.contains(")-(")){
				return new DiffDataSeries(DataSeries.fromString(seriesName.substring(1,  seriesName.indexOf(")-(")).trim(), true), DataSeries.fromString(seriesName.substring(seriesName.indexOf(")-(")+3, seriesName.length()-1).trim(), true), dataType);
			} else if(seriesName.contains("@")){
				sList = new ArrayList<DataSeries>(seriesName.split("@").length);
				for(String sName : seriesName.split("@")){
					sList.add(DataSeries.fromString(sName.trim(), true));
				}
				return new MultipleDataSeries(sList);
			} else return null;
		} else return new IndicatorDataSeries(new Indicator(seriesName, layerType, Double.class), dataType);
	}
	
	public static List<DataSeries> simpleCombinations(Indicator[] indicators, DataCategory[] dataTypes) {
		LinkedList<DataSeries> simpleInd = new LinkedList<DataSeries>();
		LinkedList<DataSeries> groupInd = new LinkedList<DataSeries>();
		for(Indicator ind : indicators){
			for(DataCategory dCat : dataTypes){
				simpleInd.add(new IndicatorDataSeries(ind, dCat));
			}
			groupInd.add(new IndicatorDataSeries(ind, DataCategory.PLAIN));
		}
		simpleInd.add(new MultipleDataSeries(groupInd));
		return simpleInd;
	}
	
	public static List<DataSeries> basicCombinations(Indicator[] indicators, DataCategory[] dataTypes) {
		LinkedList<DataSeries> simpleInd = new LinkedList<DataSeries>();
		for(Indicator ind : indicators){
			for(DataCategory dCat : dataTypes){
				simpleInd.add(new IndicatorDataSeries(ind, dCat));
			}
		}
		return simpleInd;
	}
	
	public static List<DataSeries> unionCombinations(Indicator[] indicators) {
		LinkedList<DataSeries> unionInd = new LinkedList<DataSeries>();
		LinkedList<DataSeries> simpleIndPlain = new LinkedList<DataSeries>();
		LinkedList<DataSeries> simpleIndDiff = new LinkedList<DataSeries>();
		for(Indicator ind : indicators){
			simpleIndPlain.add(new IndicatorDataSeries(ind, DataCategory.PLAIN));
			simpleIndDiff.add(new IndicatorDataSeries(ind, DataCategory.DIFFERENCE));
		}
		unionInd.add(new MultipleDataSeries(simpleIndPlain));
		unionInd.add(new MultipleDataSeries(simpleIndDiff));
		return unionInd;
	}
	
	
	
	public static List<DataSeries> selectedCombinations(Indicator[] indicators, DataCategory[] dataTypes, List<List<String>> list) {
		DataSeries firstDS, secondDS;
		List<DataSeries> outList = new LinkedList<DataSeries>();
		List<DataSeries> complexInd = new LinkedList<DataSeries>();
		List<DataSeries> simpleInd = simpleCombinations(indicators, dataTypes);
		for(List<String> lEntry : list){
			if(lEntry != null && lEntry.size() == 2){
				firstDS = DataSeries.fromList(simpleInd, lEntry.get(0).trim());
				secondDS = DataSeries.fromList(simpleInd, lEntry.get(1).trim());
				if(firstDS != null && secondDS != null){
					for(DataCategory dCat : dataTypes){
						complexInd.add(new DiffDataSeries(firstDS, secondDS, dCat));
						complexInd.add(new FractionDataSeries(firstDS, secondDS, dCat));
					}
					List<DataSeries> pList = new ArrayList<DataSeries>(lEntry.size());
					for(String entry : lEntry){
						pList.add(DataSeries.fromList(simpleInd, entry.trim()));
					}
					complexInd.add(new MultipleDataSeries(pList));
				}
			}
		}
		outList.addAll(simpleInd);
		outList.addAll(complexInd);
		return outList;
	}
	
	public static List<DataSeries> allCombinations(Indicator[] indicators, DataCategory[] dataTypes) {
		List<DataSeries> outList = new LinkedList<DataSeries>();
		List<DataSeries> simpleInd = new LinkedList<DataSeries>();
		List<DataSeries> complexInd = new LinkedList<DataSeries>();
		for(Indicator ind : indicators){
			for(DataCategory dCat : dataTypes){
				simpleInd.add(new IndicatorDataSeries(ind, dCat));
			}
		}
		for(int i=0;i<simpleInd.size();i++){
			for(int j=i+1;j<simpleInd.size();j++){
				if(!simpleInd.get(i).getName().equals(simpleInd.get(j).getName())){
					for(DataCategory dCat : dataTypes){
						complexInd.add(new DiffDataSeries(simpleInd.get(i), simpleInd.get(j), dCat));
						complexInd.add(new FractionDataSeries(simpleInd.get(i), simpleInd.get(j), dCat));
					}
				}
			}
		}
		outList.addAll(simpleInd);
		outList.addAll(complexInd);
		return outList;
	}

	public static DataSeries fromList(List<? extends DataSeries> seriesList, String newSeriesName) {
		for(DataSeries ds : seriesList){
			if(ds.toString().equals(newSeriesName)) {
				return ds;
			}
		}
		return null;
	}

	public int size() {
		return 1;
	}
	
	public String getCompactString(){
		String toReturn = "";
		String base = toCompactString();
		if(base != null && base.length() > COMPACT_STRING_SIZE_LIMIT){
			int step = base.length() / 15;
			for(int i=0;i<base.length();i=i+step){
				toReturn = toReturn + base.charAt(i);
			}
		} else toReturn = base;
		return toReturn;
	}
	
	protected abstract String toCompactString();

	public static List<DataSeries> fromString(String[] sStrings, boolean show) {
		DataSeries ds;
		String dsString;
		double dsRank;
		List<DataSeries> outList = new LinkedList<DataSeries>();
		for(String ss : sStrings){
			if(ss != null && ss.contains(",")){
				dsString = ss.split(",")[0];
				dsRank = calculateRank(ss);
			} else {
				dsString = ss;
				dsRank = Double.NaN;
			}	
			ds = DataSeries.fromString(dsString, show);
			if(ds != null){
				ds.setRank(dsRank);
				outList.add(ds);
			} else AppLogger.logError(DataSeries.class, "UnrecognizedDataSeries", "Unable to recognize '" + dsString + "' dataseries");
		}
		return outList;
	}

	private static double calculateRank(String ss) {
		double sum = 0;
		int count = 0;
		if(ss == null || !ss.contains(","))
			return Double.NaN;
		else {
			String[] splitted = ss.split(",");
			for(int i=1;i<splitted.length;i++){
				if(AppUtility.isNumber(splitted[i])){
					sum = sum + Double.valueOf(splitted[i]);
					count++;
				}
			}
			if(count > 0)
				return sum / count;
			else return Double.NaN;
		}
	}

	private void setRank(double dsRank) {
		this.dsRank = dsRank;
	}
	
	public double getRank(){
		return dsRank;
	}

	public String getSanitizedName() {
		return toString().replace("#PLAIN#", "(P)").replace("#DIFFERENCE#", "(D)").replace("NO_LAYER", "").replace("COMPOSITION", "");
	}
	
	public static boolean isIn(List<DataSeries> dsList, DataSeries newSeries){
		if(dsList == null || dsList.size() == 0)
			return false;
		for(DataSeries ds : dsList){
			if(ds.compareTo(newSeries) == 0)
				return true;
		}
		return false;
	}
	
	public static List<DataSeries> allCombinations(List<DataSeries> selectedFeatures){
		List<DataSeries> combinedFeatures = new LinkedList<DataSeries>();
		for(int i=0;i<selectedFeatures.size();i++){
			for(int j=i+1;j<selectedFeatures.size();j++){
				if(!selectedFeatures.get(i).getName().equals(selectedFeatures.get(j).getName())){
					combinedFeatures.add(new SumDataSeries(selectedFeatures.get(i), selectedFeatures.get(j), DataCategory.PLAIN));
					combinedFeatures.add(new FractionDataSeries(selectedFeatures.get(i), selectedFeatures.get(j), DataCategory.PLAIN));
					combinedFeatures.add(new MultipleDataSeries(selectedFeatures.get(i), selectedFeatures.get(j)));
				}
			}
		}
		return combinedFeatures;
	}
	
	public static List<DataSeries> unionCombinations(List<DataSeries> selectedFeatures) {
		List<DataSeries> combinedFeatures = new LinkedList<DataSeries>();
		if(selectedFeatures != null){
			if(selectedFeatures.size() == 1)
				combinedFeatures.add(selectedFeatures.get(0));
			else combinedFeatures.add(new MultipleDataSeries(selectedFeatures));
		}
		return combinedFeatures;
	}
	
	public static List<DataSeries> multipleUnionCombinations(List<DataSeries> selectedFeatures) {
		List<DataSeries> combinedFeatures = new LinkedList<DataSeries>();
		if(selectedFeatures != null){
			Collections.sort(selectedFeatures);
			for(int i=1;i<=selectedFeatures.size();i++){
				List<DataSeries> innerUnion = unionCombinations(selectedFeatures.subList(0, i));
				if(innerUnion != null)
					combinedFeatures.addAll(innerUnion);
			}
		}
		return combinedFeatures;
	}
	
	public static List<DataSeries> pearsonCombinations(List<Knowledge> kList, double pearsonThreshold, String setupFolder, List<DataSeries> selectedFeatures) {
		PearsonCombinationManager pcManager;
		List<DataSeries> combinedFeatures = new LinkedList<DataSeries>();
		File pearsonFile = new File(setupFolder + "pearsonCombinations.csv");
		pcManager = new PearsonCombinationManager(pearsonFile, selectedFeatures, kList);
		pcManager.calculatePearsonIndexes(pearsonThreshold);
		combinedFeatures = pcManager.getPearsonCombinedSeries();
		pcManager.flush();
		return combinedFeatures;
	}
		
}
