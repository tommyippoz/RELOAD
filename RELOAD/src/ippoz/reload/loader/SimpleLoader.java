/**
 * 
 */
package ippoz.reload.loader;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleLoader.
 *
 * @author Tommy
 */
public abstract class SimpleLoader implements Loader {
	
	/** The Constant TRAIN_CSV_FILE. */
	public static final String TRAIN_CSV_FILE = "TRAIN_CSV_FILE";
	
	/** The Constant TRAIN_FAULTY_TAGS. */
	public static final String TRAIN_FAULTY_TAGS = "TRAIN_FAULTY_TAGS";

	/** The Constant VALIDATION_CSV_FILE. */
	public static final String VALIDATION_CSV_FILE = "VALIDATION_CSV_FILE";
	
	/** The Constant VALIDATION_FAULTY_TAGS. */
	public static final String VALIDATION_FAULTY_TAGS = "VALIDATION_FAULTY_TAGS";
	
	/** The data list. */
	protected List<MonitoredData> dataList;
	
	/** The runs. */
	private List<Integer> runs;
	
	/** The Relevant Features. */
	private List<Indicator> relevantFeatures;
	
	/** The header. */
	private List<Indicator> header;

	/**
	 * Instantiates a new simple loader.
	 *
	 * @param runs the runs
	 */
	public SimpleLoader(List<Integer> runs, List<Indicator> relevantFeatures){
		Collections.sort(runs);
		this.runs = runs;
		this.relevantFeatures = relevantFeatures;
		dataList = new LinkedList<MonitoredData>();
	}
	
	protected List<Indicator> getHeader(){
		if(header == null)
			header = loadHeader();
		return header;
	}
	
	protected void filterHeader(Integer[] skip) {
		if(getHeader() != null){
			for(int i=0;i<getHeader().size();i++){
				if(occursIn(skip, i))
					getHeader().set(i, null);
			}
		}
	}
	
	/**
	 * True if item should be skipped (occurs in the 'skip' list).
	 *
	 * @param skip the skip
	 * @param item the item
	 * @return true, if successful
	 */
	private static boolean occursIn(Integer[] skip, int item){
		for(int i=0;i<skip.length;i++){
			if(skip[i] == item)
				return true;
		}
		return false;
	}
	
	public abstract List<Indicator> loadHeader();

	/**
	 * Instantiates a new simple loader.
	 *
	 * @param runs the runs
	 */
	public SimpleLoader(List<Integer> runs){
		this(runs, null);
	}
	
	/**
	 * True if a given row of the dataset should be read.
	 *
	 * @param index the index
	 * @return true, if successful
	 */
	public synchronized boolean canRead(int index){
		if(runs != null && runs.size() > 0){
			while(runs.size() > 0 && index > runs.get(0)){
				runs.remove(0);
			}
			if(runs.size() > 0)
				return index == runs.get(0);
			else return false;
		} else return false;
	}

	public List<Indicator> getRelevantFeatures() {
		return relevantFeatures;
	}

	public void setRelevantFeatures(List<Indicator> relevantFeatures) {
		this.relevantFeatures = relevantFeatures;
	}
	
	@Override
	public String[] getFeatureNames() {
		String[] names = null;
		if(getHeader() != null){
			names = new String[header.size()];
			for(int i=0;i<header.size();i++){
				names[i] = header.get(i).getName();
			}
		}
		return names;
	}
	
	public boolean hasFeature(String featureName){
		if(featureName != null && getHeader() != null && header.size() > 0){
			for(Indicator ind : header){
				if(ind.getName().toUpperCase().equals(featureName.toUpperCase()))
					return true;
			}
			return false;
		} else return false;
	}
	
	public int getFeatureIndex(String featureName){
		int i = 0;
		if(hasFeature(featureName)){
			for(Indicator ind : getHeader()){
				if(ind.getName().toUpperCase().equals(featureName.toUpperCase()))
					return i;
				i++;
			}
		}
		return -1;
	}
	
	/**
	 * Returns a flag to indicate if the feature is numeric.
	 *
	 * @return the name
	 */
	public Boolean isFeatureNumeric(String featureName){
		String strVal = "";
		Object[] values = getSampleValuesFor(featureName);
		if(values == null || values.length == 0)
			return null;
		else {
			for(Object val : values){
				if(val != null){
					strVal = val.toString();
					if(!AppUtility.isNumber(strVal))
						return false;
				}
			}	
			return true;
		}
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#fetch()
	 */
	@Override
	public List<MonitoredData> fetch() {
		return dataList;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#getRuns()
	 */
	@Override
	public String getRuns() {
		String tag, endTag;
		if(dataList != null){
			tag = dataList.get(0).getDataTag();
			if(dataList.size() == 1){
				return "[" + tag.substring(tag.indexOf("_") + 1) + "]";
			} else {
				endTag = dataList.get(dataList.size()-1).getDataTag();				
				return "[" + tag.substring(tag.indexOf("_") + 1) + "-" + endTag.substring(endTag.indexOf("_") + 1) + "]";
			}
		}
		else return null;
	}
	
	/**
	 * Extracts faulty tags from preferences.
	 *
	 * @param prefManager the preferences manager
	 * @param tag the tag
	 * @return the string resembling faulty tags
	 */
	protected static String extractFaultyTags(PreferencesManager prefManager, String tag) {
		if(tag.equals("train") && prefManager.hasPreference(TRAIN_FAULTY_TAGS))
			return prefManager.getPreference(TRAIN_FAULTY_TAGS);
		else if(!tag.equals("train") && prefManager.hasPreference(VALIDATION_FAULTY_TAGS))
			return prefManager.getPreference(VALIDATION_FAULTY_TAGS);
		else return prefManager.getPreference("FAULTY_TAGS");
	}
	
	
	
	/**
	 * Parses the columns to be considered.
	 *
	 * @param colString the column string
	 * @return the integer[]
	 */
	protected static Integer[] parseColumns(String colString) {
		LinkedList<Integer> iList = new LinkedList<Integer>();
		if(colString != null && colString.length() > 0){
			for(String str : colString.split(",")){
				iList.add(new Integer(str.trim()));
			}
			return iList.toArray(new Integer[iList.size()]);
		} else return new Integer[]{};
	}

}
