/*
 * 
 */
package ippoz.multilayer.detector.commons.data;

import ippoz.multilayer.commons.datacategory.DataCategory;
import ippoz.multilayer.detector.commons.support.AppLogger;

import java.util.HashMap;

/**
 * The Class IndicatorData.
 * Stores data of a single indicator with different instances depending on the considered data types.
 *
 * @author Tommy
 */
public class IndicatorData {
	
	/** The indicator data map. */
	private HashMap<DataCategory, String> dataMap;
	
	/**
	 * Instantiates a new indicator data.
	 *
	 * @param dataMap the data map
	 */
	public IndicatorData(HashMap<DataCategory, String> dataMap){
		this.dataMap = dataMap;
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
