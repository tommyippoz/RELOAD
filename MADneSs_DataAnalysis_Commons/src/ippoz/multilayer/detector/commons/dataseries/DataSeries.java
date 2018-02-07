/**
 * 
 */
package ippoz.multilayer.detector.commons.dataseries;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.commons.layers.LayerType;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.data.Observation;
import ippoz.multilayer.detector.commons.data.SnapshotValue;
import ippoz.multilayer.detector.commons.service.IndicatorStat;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;
import ippoz.multilayer.detector.commons.service.StatPair;
import ippoz.multilayer.detector.commons.support.AppLogger;

import java.util.Date;
import java.util.LinkedList;
import java.util.Map.Entry;

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
	
	// Sincronizza anche se � all'inizio, nel corpo o alla fine.
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
				return new MultipleDataSeries(DataSeries.fromString(seriesName.substring(0,  seriesName.indexOf("@")).trim(), true), DataSeries.fromString(seriesName.substring(seriesName.indexOf("@")+1, seriesName.length()).trim(), true));
			} else return null;
		} else return new IndicatorDataSeries(new Indicator(seriesName, layerType, Double.class), dataType);
	}
	
	public static LinkedList<DataSeries> simpleCombinations(Indicator[] indicators, DataCategory[] dataTypes) {
		LinkedList<DataSeries> simpleInd = new LinkedList<DataSeries>();
		for(Indicator ind : indicators){
			for(DataCategory dCat : dataTypes){
				simpleInd.add(new IndicatorDataSeries(ind, dCat));
			}
		}
		return simpleInd;
	}
	
	public static LinkedList<DataSeries> selectedCombinations(Indicator[] indicators, DataCategory[] dataTypes, LinkedList<Entry<String, String>> couples) {
		DataSeries firstDS, secondDS;
		LinkedList<DataSeries> outList = new LinkedList<DataSeries>();
		LinkedList<IndicatorDataSeries> simpleInd = new LinkedList<IndicatorDataSeries>();
		LinkedList<DataSeries> complexInd = new LinkedList<DataSeries>();
		for(Indicator ind : indicators){
			for(DataCategory dCat : dataTypes){
				simpleInd.add(new IndicatorDataSeries(ind, dCat));
			}
		}
		for(Entry<String, String> cEntry : couples){
			firstDS = DataSeries.fromList(simpleInd, cEntry.getKey());
			secondDS = DataSeries.fromList(simpleInd, cEntry.getValue());
			if(firstDS != null && secondDS != null){
				for(DataCategory dCat : dataTypes){
					complexInd.add(new DiffDataSeries(firstDS, secondDS, dCat));
					complexInd.add(new FractionDataSeries(firstDS, secondDS, dCat));
				}
				complexInd.add(new MultipleDataSeries(firstDS, secondDS));
			}
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

	public static DataSeries fromList(LinkedList<IndicatorDataSeries> seriesList, String newSeriesName) {
		for(DataSeries ds : seriesList){
			if(ds.toString().equals(newSeriesName)) {
				return ds;
			}
		}
		return null;
	}
		
}
