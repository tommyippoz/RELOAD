/**
 * 
 */
package ippoz.multilayer.detector.commons.configuration;

import ippoz.madness.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;

import java.util.HashMap;

/**
 * The Class AlgorithmConfiguration.
 * Basic Configuration for the involved Algorithms.
 *
 * @author Tommy
 */
public class AlgorithmConfiguration implements Cloneable {
	
	/** The Constant WEIGHT. */
	public static final String WEIGHT = "weight";
	
	/** The Constant SCORE. */
	public static final String SCORE = "metric_score";

	public static final String INVARIANT = "invariant";
	
	public static final String DETAIL = "detail";

	public static final String PEARSON_TOLERANCE = "pi_tolerance";

	public static final String PEARSON_WINDOW = "pi_window";

	/** The configuration map. */
	private HashMap<String, Object> confMap;
	
	/** The algorithm type */
	private AlgorithmType algType;
	
	/**
	 * Instantiates a new algorithm configuration.
	 */
	public AlgorithmConfiguration(AlgorithmType algType){
		confMap = new HashMap<String, Object>();
		this.algType = algType;
	}
	
	private void setMap(HashMap<String, Object> newMap){
		confMap = newMap;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		HashMap<String, Object> newMap = new HashMap<String, Object>();
		AlgorithmConfiguration newConf = null;
		try {
			newConf = getConfiguration(algType, this);
			for(String mapKey : confMap.keySet()){
				newMap.put(mapKey, confMap.get(mapKey));
			}
			newConf.setMap(newMap);
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone configuration");
		}
		return newConf;
	}
	
	public static AlgorithmConfiguration getConfiguration(AlgorithmType algType, AlgorithmConfiguration oldConf) {
		AlgorithmConfiguration conf = new AlgorithmConfiguration(algType);
		return conf;
	}

	/**
	 * Adds an item.
	 *
	 * @param item the item tag
	 * @param value the itam value
	 */
	public void addItem(String item, String value){
		confMap.put(item, value);
	}
	
	/**
	 * Adds a raw item.
	 *
	 * @param item the item tag
	 * @param value the item value
	 */
	public void addRawItem(String item, Object value){
		confMap.put(item, value);
	}
	
	/**
	 * Gets the item.
	 *
	 * @param tag the item tag
	 * @return the item
	 */
	public String getItem(String tag){
		return getItem(tag, true);
	}
	
	/**
	 * Gets the item.
	 *
	 * @param tag the item tag
	 * @return the item
	 */
	public String getItem(String tag, boolean flag){
		if(!confMap.containsKey(tag)){
			if(flag && !tag.equals("weight"))
				AppLogger.logInfo(getClass(), "Unable to find tag '" + tag + "'");
			return null;
		} else return confMap.get(tag).toString();
	}
	
	/**
	 * Gets the raw item.
	 *
	 * @param tag the item tag
	 * @return the item
	 */
	public Object getRawItem(String tag){
		return getRawItem(tag, true);
	}
	
	/**
	 * Gets the raw item.
	 *
	 * @param tag the item tag
	 * @return the item
	 */
	public Object getRawItem(String tag, boolean flag){
		if(!confMap.containsKey(tag)){
			if(flag)
				AppLogger.logInfo(getClass(), "Unable to find tag '" + tag + "'");
			return null;
		} else return confMap.get(tag);
	}
	
	/**
	 * Gets the algorithm type.
	 *
	 * @return the algType
	 */
	public AlgorithmType getAlgorithmType(){
		return algType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return algType + ":[" + getSpecificItems() + "]";
	}

	/**
	 * Converts to a file row.
	 *
	 * @param complete the complete flag, defines if the description is extended or not.
	 * @return the file row string
	 */
	public String toFileRow(boolean complete){
		if(complete)
			return getItem(WEIGHT, false) + ", " + getItem(SCORE, false) + ", " + getSpecificItems();
		else return getSpecificItems();
	}

	private String getSpecificItems() {
		String all = "";
		for(String itemTag : confMap.keySet()){
			if(!itemTag.equals(SCORE) && !itemTag.equals(WEIGHT)){
				all = all + itemTag + "=" + getRawItem(itemTag, false).toString() + "&";
			}
		}
		return all;
	}	
	
	public static AlgorithmConfiguration buildConfiguration(AlgorithmType algType, String descRow){
		String tag, value;
		AlgorithmConfiguration conf = new AlgorithmConfiguration(algType);
		if(descRow != null){
			for(String splitted : descRow.split("&")){
				if(splitted.contains("=")){
					tag = splitted.split("=")[0].trim();
					value = splitted.split("=")[1].trim();
					conf.addItem(tag, value);
				}
			}
		}
		return conf;
	}
	
}
