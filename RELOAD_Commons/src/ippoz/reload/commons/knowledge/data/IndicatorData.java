/*
 * 
 */
package ippoz.reload.commons.knowledge.data;

import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.support.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class IndicatorData.
 * Stores data of a single indicator with different instances depending on the considered data types.
 *
 * @author Tommy
 */
public class IndicatorData {
	
	/** The indicator data map. */
	private Map<DataCategory, String> dataMap;
	
	/**
	 * Instantiates a new indicator data.
	 *
	 * @param dataMap the data map
	 */
	public IndicatorData(Map<DataCategory, String> dataMap){
		this.dataMap = dataMap;
	}
	
	public IndicatorData(String indData, DataCategory dataTag) {
		dataMap = new HashMap<>();
		dataMap.put(dataTag, indData);
	}

	/**
	 * Gets the indicator data related to a chosen category value.
	 *
	 * @param categoryTag the category tag
	 * @return the indicator category value
	 */
	public String getCategoryValue(DataCategory categoryTag){
		if(dataMap.containsKey(categoryTag))
			return dataMap.get(categoryTag);
		else {
			AppLogger.logError(getClass(), "NoSuchCategoryData", "Category '" + categoryTag + "' not found");
			return null;
		}
	}

}
