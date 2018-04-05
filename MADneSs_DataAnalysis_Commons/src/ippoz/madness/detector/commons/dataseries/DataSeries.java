/**
 * 
 */
package ippoz.madness.detector.commons.dataseries;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.knowledge.data.Observation;
import ippoz.madness.detector.commons.knowledge.snapshot.SnapshotValue;
import ippoz.madness.detector.commons.service.IndicatorStat;
import ippoz.madness.detector.commons.service.ServiceCall;
import ippoz.madness.detector.commons.service.ServiceStat;
import ippoz.madness.detector.commons.service.StatPair;
import ippoz.madness.detector.commons.support.AppLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class DataSeries implements Comparable<DataSeries> {

	private String seriesName;
	private DataCategory dataCategory;
	
	protected DataSeries(String seriesName, DataCategory dataCategory) {
		this.seriesName = seriesName;
		this.dataCategory = dataCategory;
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
		if(this instanceof ComplexDataSeries)
			return ((ComplexDataSeries)this).getFirstOperand().contains(other) || ((ComplexDataSeries)this).getSecondOperand().contains(other);
		else return compareTo(other) == 0;
	}

	@Override
	public int compareTo(DataSeries other) {
		return seriesName.equals(other.getName()) && dataCategory.equals(other.getDataCategory()) ? 0 : 1;
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
		for(Indicator ind : indicators){
			for(DataCategory dCat : dataTypes){
				simpleInd.add(new IndicatorDataSeries(ind, dCat));
			}
		}
		return simpleInd;
	}
	
	public static List<DataSeries> selectedCombinations(Indicator[] indicators, DataCategory[] dataTypes, List<List<String>> list) {
		DataSeries firstDS, secondDS;
		List<DataSeries> outList = new LinkedList<DataSeries>();
		List<IndicatorDataSeries> simpleInd = new LinkedList<IndicatorDataSeries>();
		List<DataSeries> complexInd = new LinkedList<DataSeries>();
		for(Indicator ind : indicators){
			for(DataCategory dCat : dataTypes){
				simpleInd.add(new IndicatorDataSeries(ind, dCat));
			}
		}
		for(List<String> lEntry : list){
			if(lEntry != null && lEntry.size() == 2){
				firstDS = DataSeries.fromList(simpleInd, lEntry.get(0).trim());
				secondDS = DataSeries.fromList(simpleInd, lEntry.get(1).trim());
				if(firstDS != null && secondDS != null){
					for(DataCategory dCat : dataTypes){
						complexInd.add(new DiffDataSeries(firstDS, secondDS, dCat));
						complexInd.add(new FractionDataSeries(firstDS, secondDS, dCat));
					}
				}
			}
			List<DataSeries> pList = new ArrayList<DataSeries>(lEntry.size());
			for(String entry : lEntry){
				pList.add(DataSeries.fromList(simpleInd, entry.trim()));
			}
			complexInd.add(new MultipleDataSeries(pList));
		}
		outList.addAll(simpleInd);
		outList.addAll(complexInd);
		return outList;
	}
	
	public static LinkedList<DataSeries> allCombinations(Indicator[] indicators, DataCategory[] dataTypes) {
		LinkedList<DataSeries> outList = new LinkedList<DataSeries>();
		LinkedList<DataSeries> simpleInd = new LinkedList<DataSeries>();
		LinkedList<DataSeries> complexInd = new LinkedList<DataSeries>();
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

	public static DataSeries fromList(List<IndicatorDataSeries> seriesList, String newSeriesName) {
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
	
	public abstract String toCompactString();
		
}
