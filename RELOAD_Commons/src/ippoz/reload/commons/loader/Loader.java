/**
 * 
 */
package ippoz.reload.commons.loader;

import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.data.MonitoredData;
import ippoz.reload.commons.support.PreferencesManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Interface Loader. This allows loading Knowledge from external data sources, either 
 * datasets, streams and databases.
 *
 * @author Tommy
 */
public abstract class Loader {
	
	/** The Constant TRAIN_RUN_PREFERENCE. */
	public static final String TRAIN_PARTITION = "TRAIN_RUN_IDS";
	
	/** The Constant VALIDATION_RUN_PREFERENCE. */
	public static final String VALIDATION_PARTITION = "VALIDATION_RUN_IDS";

	/** The Constant LOADER_TYPE. */
	public static final String LOADER_TYPE = "LOADER_TYPE";
	
	/** The Constant TRAIN_CSV_FILE. */
	public static final String TRAIN_FILE = "TRAIN_FILE";
	
	/** The Constant TRAIN_FAULTY_TAGS. */
	public static final String TRAIN_FAULTY_TAGS = "TRAIN_FAULTY_TAGS";

	/** The Constant VALIDATION_CSV_FILE. */
	public static final String VALIDATION_FILE = "VALIDATION_FILE";
	
	/** The Constant VALIDATION_FAULTY_TAGS. */
	public static final String VALIDATION_FAULTY_TAGS = "VALIDATION_FAULTY_TAGS";
	
	public static final int SAMPLE_VALUES_COUNT = 200;

	/** The data list. */
	protected List<MonitoredData> dataList;
	
	/** The runs. */
	private List<LoaderBatch> batches;
	
	/** The Relevant Features. */
	private List<Indicator> relevantFeatures;
	
	/** The header. */
	private Map<String, Boolean> header;
	
	protected DatasetInfo datasetInfo;
	
	/**
	 * Instantiates a new simple loader.
	 *
	 * @param batches the runs
	 */
	public Loader(){
		dataList = new LinkedList<MonitoredData>();
	}

	/**
	 * Instantiates a new simple loader.
	 *
	 * @param runs the runs
	 */
	public Loader(List<LoaderBatch> runs, List<Indicator> relevantFeatures){
		Collections.sort(runs);
		this.batches = runs;
		this.relevantFeatures = relevantFeatures;
		dataList = new LinkedList<MonitoredData>();
	}
	
	public int getBatchesNumber() {
		if(batches != null)
			return batches.size();
		else return 0;
	}
	
	protected void setBatches(List<LoaderBatch> runs) {
		this.batches = runs;
	}
	
	protected void updateBatches() {
		int n = getRowNumber();
		List<Integer> toRemove = new LinkedList<Integer>();
		if(n > 0 && getBatchesNumber() > 0){
			for(int i=0;i<getBatchesNumber();i++){
				if(batches.get(i).getFrom() >= n)
					toRemove.add(i);
				else if(batches.get(i).getTo() >= n)
					batches.get(i).setTo(n-1);
			}
			if(toRemove.size() > 0){
				List<LoaderBatch> newList = new ArrayList<>(getBatchesNumber() - toRemove.size());
				for(int i=0;i<getBatchesNumber();i++){
					if(!toRemove.contains(i))
						newList.add(batches.get(i));
				}
				setBatches(newList);
			}
		}
	}
	
	protected Map<String, Boolean> getHeader(){
		if(header == null)
			header = loadHeader();
		return header;
	}
	
	protected int getRunsNumber(){
		if(batches != null)
			return batches.size();
		else return 0;
	}
	
	protected void filterHeader(String[] toSkip, String labelString) {
		Map<String, Boolean> head = getHeader();
		if(head != null){
			for(String ind : head.keySet()){
				if(ind != null && occursIn(toSkip, ind))
					head.replace(ind, false);
			}
			if(labelString != null && head.containsKey(labelString.trim()))
				head.replace(labelString, false);
				
		}
	}
	
	/**
	 * True if a given row of the dataset should be read.
	 *
	 * @param index the index
	 * @return true, if successful
	 */
	public synchronized boolean canRead(int rowIndex){
		if(batches != null && batches.size() > 0){
			for(LoaderBatch runItem : batches){
				if(runItem.includesRow(rowIndex))
					return true;
			}
			return false;
		} else return false;
	}
	
	/**
	 * Gets the runs to be used to load the CSV file.
	 *
	 * @param rowIndex the row index
	 * @return the run
	 */
	protected int getBatchIndex(int rowIndex){
		if(batches != null && batches.size() > 0){
			int i=0;
			for(LoaderBatch runItem : batches){
				if(runItem.includesRow(rowIndex))
					return i;
				i++;
			}
			return -1;
		} else return -1;
	}
	
	/**
	 * Gets the runs to be used to load the file.
	 *
	 * @param rowIndex the row index
	 * @return the run
	 */
	protected LoaderBatch getBatchFromRow(int rowIndex){
		int index = getBatchIndex(rowIndex);
		if(index >= 0 && index < batches.size())
			return batches.get(index);
		else return null;
	}
	
	/**
	 * Gets the runs to be used to load the file.
	 *
	 * @param rowIndex the row index
	 * @return the run
	 */
	protected LoaderBatch getBatch(int index){
		if(index >= 0 && index < batches.size())
			return batches.get(index);
		else return null;
	}
	
	public int getDataPoints() {
		int rNumb = 0;
		if(batches != null && batches.size() > 0){
			for(LoaderBatch runItem : batches){
				rNumb = rNumb + runItem.getDataPoints();
			}
		} 
		return rNumb;
	}	

	public List<Indicator> getRelevantFeatures() {
		return relevantFeatures;
	}

	public void setRelevantFeatures(List<Indicator> relevantFeatures) {
		this.relevantFeatures = relevantFeatures;
	}
	
	public String[] getFeatureNames() {
		String[] names = null;
		if(getHeader() != null){
			int i = 0;
			names = new String[header.size()];
			for(String ind : header.keySet()){
				if(ind != null && header.get(ind))
					names[i] = ind;
				else names[i] = "-";
				i++;
			}
		}
		return names;
	}
	
	public String[] getAllFeatureNames() {
		String[] names = null;
		if(getHeader() != null){
			int i = 0;
			names = new String[header.size()];
			for(String ind : header.keySet()){
				if(ind != null)
					names[i] = ind;
				else names[i] = "-";
				i++;
			}
		}
		return names;
	}
	
	public boolean hasFeature(String featureName){
		if(featureName != null && getHeader() != null && header.size() > 0){
			for(String ind : header.keySet()){
				if(ind != null && header.get(ind) && ind.toUpperCase().equals(featureName.toUpperCase()))
					return true;
			}
			return false;
		} else return false;
	}
	
	public int getFeatureIndex(String featureName){
		int i = 0;
		if(featureName != null && hasFeature(featureName)){
			for(String ind : getHeader().keySet()){
				if(ind != null && header.get(ind) && ind.toUpperCase().equals(featureName.toUpperCase()))
					return i;
				i++;
			}
		}
		return -1;
	}
	
	public List<MonitoredData> fetch() {
		return dataList;
	}

	public String getRuns() {
		LoaderBatch tag;
		if(dataList != null){
			tag = dataList.get(0).getDataID();
			if(dataList.size() == 1){
				return "[" + tag.toString() + "]";
			} else {
				String toRet = "";
				for(MonitoredData md : dataList){
					if(md != null && md.getDataID() != null)
						toRet = toRet + md.getDataID().toString() + ",";
				}			
				return toRet.substring(0, toRet.length()-1);
			}
		}
		else return null;
	}
	
	public List<LoaderBatch> getLoaderRuns(){
		return batches;
	}

	public void flush() {
		if(dataList != null)
			dataList.clear();
		if(header != null)
			header.clear();
		if(batches != null)
			batches.clear();
		if(relevantFeatures != null)
			relevantFeatures.clear();
	}
	
	public int getIndicatorNumber(){
		return getFeatureNames().length;
	}
	
	public abstract LoaderType getLoaderType();

	/**
	 * Gets the name of the Loader.
	 *
	 * @return the name
	 */
	public abstract String getLoaderName();
	
	/**
	 * Gets the name of the Loader.
	 *
	 * @return the name
	 */
	public abstract String getCompactName();
	
	public abstract int getRowNumber();
	
	public abstract double getMBSize();

	public abstract boolean canFetch();
	
	public abstract double getAnomalyRate();
	
	public abstract double getSkipRate();
	
	public abstract Map<String, Boolean> loadHeader();
	
	public abstract boolean hasBatches(String preferenceString);
	
	/**
	 * True if item should be skipped (occurs in the 'skip' list).
	 *
	 * @param toSkip the skip
	 * @param string the item
	 * @return true, if successful
	 */
	private static boolean occursIn(String[] toSkip, String string){
		for(int i=0;i<toSkip.length;i++){
			if(toSkip[i].toUpperCase().compareTo(string.toUpperCase()) == 0)
				return true;
		}
		return false;
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
	
	public static boolean isValid(Loader loader) {
		return loader != null && Double.isFinite(loader.getAnomalyRate()) && loader.getAnomalyRate() > 0;
	}
	
}
