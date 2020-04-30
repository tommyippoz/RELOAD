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

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleLoader.
 *
 * @author Tommy
 */
public abstract class SimpleLoader implements Loader {
	
	/** The Constant TRAIN_CSV_FILE. */
	public static final String TRAIN_FILE = "TRAIN_FILE";
	
	/** The Constant TRAIN_FAULTY_TAGS. */
	public static final String TRAIN_FAULTY_TAGS = "TRAIN_FAULTY_TAGS";

	/** The Constant VALIDATION_CSV_FILE. */
	public static final String VALIDATION_FILE = "VALIDATION_FILE";
	
	/** The Constant VALIDATION_FAULTY_TAGS. */
	public static final String VALIDATION_FAULTY_TAGS = "VALIDATION_FAULTY_TAGS";
	
	/** The data list. */
	protected List<MonitoredData> dataList;
	
	/** The runs. */
	private List<LoaderBatch> batches;
	
	/** The Relevant Features. */
	private List<Indicator> relevantFeatures;
	
	/** The header. */
	private List<Indicator> header;
	
	/**
	 * Instantiates a new simple loader.
	 *
	 * @param batches the runs
	 */
	public SimpleLoader(){
		dataList = new LinkedList<MonitoredData>();
	}

	/**
	 * Instantiates a new simple loader.
	 *
	 * @param runs the runs
	 */
	public SimpleLoader(List<LoaderBatch> runs, List<Indicator> relevantFeatures){
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
	
	protected List<Indicator> getHeader(){
		if(header == null)
			header = loadHeader();
		return header;
	}
	
	protected int getRunsNumber(){
		if(batches != null)
			return batches.size();
		else return 0;
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
	
	@Override
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
	
	@Override
	public String[] getFeatureNames() {
		String[] names = null;
		if(getHeader() != null){
			names = new String[header.size()];
			for(int i=0;i<header.size();i++){
				if(header.get(i) != null)
					names[i] = header.get(i).getName();
				else names[i] = "-";
			}
		}
		return names;
	}
	
	public boolean hasFeature(String featureName){
		if(featureName != null && getHeader() != null && header.size() > 0){
			for(Indicator ind : header){
				if(ind != null && ind.getName().toUpperCase().equals(featureName.toUpperCase()))
					return true;
			}
			return false;
		} else return false;
	}
	
	public int getFeatureIndex(String featureName){
		int i = 0;
		if(featureName != null && hasFeature(featureName)){
			for(Indicator ind : getHeader()){
				if(ind != null && ind.getName().toUpperCase().equals(featureName.toUpperCase()))
					return i;
				i++;
			}
		}
		return -1;
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
	
	@Override
	public List<LoaderBatch> getLoaderRuns(){
		return batches;
	}

}
