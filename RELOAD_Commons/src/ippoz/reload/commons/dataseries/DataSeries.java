/**
 * 
 */
package ippoz.reload.commons.dataseries;

import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class DataSeries implements Comparable<DataSeries> {
	
	private static final int COMPACT_STRING_SIZE_LIMIT = 60;

	private String seriesName;
	
	private Indicator[] indList;
	
	private double dsRank;
	
	public DataSeries(List<DataSeries> seriesList){
		this.dsRank = Double.NaN;
		List<Indicator> iList = new LinkedList<>();
		for(DataSeries ds : seriesList){
			iList.addAll(Arrays.asList(ds.getIndicators()));
		}
		iList = sanitizeIndicators(iList);
		indList = iList.toArray(new Indicator[iList.size()]);
		if(indList != null && indList.length > 0){
			seriesName = toCompactString();
		} else seriesName = "";
	}

	public DataSeries(Indicator[] indicatorList) {
		List<Indicator> ind = sanitizeIndicators(Arrays.asList(indicatorList));
		this.indList = ind.toArray(new Indicator[ind.size()]);
		this.dsRank = Double.NaN;
		if(indList != null && indList.length > 0){
			seriesName = toCompactString();
		} else seriesName = "";
	}
	
	private List<Indicator> sanitizeIndicators(List<Indicator> initialList) {
		List<Indicator> finalList = new LinkedList<>();
		if(initialList != null){
			for(Indicator ind : initialList){
				if(ind != null)
					finalList.add(ind);
			}
		}
		return finalList;
	}
	
	public DataSeries(Indicator indicator) {
		this(new Indicator[]{indicator});
	}
	
	public Indicator[] getIndicators() {
		return indList;
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public String getName(){
		return seriesName;
	}
	
	public List<DataSeries> listSubSeries(){
		List<DataSeries> outList = new LinkedList<DataSeries>();
		if(indList != null && indList.length > 1){
			for(Indicator ind : indList){
				outList.add(new DataSeries(ind));
			}
		} else outList.add(this);
		return outList;
	}
	
	public boolean contains(DataSeries other){
		if(other == null || other.size() == 0)
			return true;
		else if(size() == 0)
			return false;
		else {
			for(Indicator ind : other.getIndicators()){
				if(!containsIndicator(ind))
					return false;
			} 
			return true;
		}
	}

	private boolean containsIndicator(Indicator other) {
		if(other != null){
			for(Indicator ind : getIndicators()){
				if(ind.getName().compareTo(other.getName()) == 0)
					return true;
			} 
		}
		return false;
	}

	@Override
	public int compareTo(DataSeries other) {
		return seriesName.compareTo(other.getName());
	}
	
	public static DataSeries fromString(String seriesName) {
		if(seriesName.contains("@")){
			int indIndex = 0;
			Indicator[] iList = new Indicator[seriesName.split("@").length];
			for(String sName : seriesName.split("@")){
				iList[indIndex++] = new Indicator(sName.trim(), Double.class);
			}
			return new DataSeries(iList);
		} else if(seriesName.contains(";")){
			int indIndex = 0;
			Indicator[] iList = new Indicator[seriesName.split(";").length];
			for(String sName : seriesName.split(";")){
				iList[indIndex++] = new Indicator(sName.trim(), Double.class);
			}
			return new DataSeries(iList);
		} else return new DataSeries(new Indicator(seriesName, Double.class));
	}
	
	public static List<DataSeries> basicCombinations(Indicator[] indicators) {
		List<DataSeries> outList = new LinkedList<DataSeries>();
		if(indicators != null){
			for(Indicator ind : indicators){
				outList.add(new DataSeries(ind));
			}
		}
		return outList;
	}
	
	public static DataSeries unionCombinations(Indicator[] indicators) {
		return new DataSeries(indicators);
	}

	public int size() {
		if(indList != null)
			return indList.length;
		else return 0;
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
	
	private String toCompactString(){
		String string = "";
		if(indList != null && indList.length > 0){
			for(Indicator ind : indList){
				if(ind != null)
					string = string + ind.getName() + ";";
			}
			return string.substring(0, string.length()-1);
		} else return "";
	}

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
			ds = DataSeries.fromString(dsString);
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
		return getName();
	}
	
	public String getCompactName() {
		return getName();
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

}
