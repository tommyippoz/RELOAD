/**
 * 
 */
package ippoz.multilayer.detector.commons.data;

import ippoz.multilayer.commons.datacategory.DataCategory;
import ippoz.multilayer.commons.indicator.Indicator;
import ippoz.multilayer.detector.commons.support.AppLogger;
import ippoz.multilayer.detector.commons.support.AppUtility;

import java.util.Date;
import java.util.HashMap;

/**
 * The Class Observation.
 * STores data related to observations of all the indicator at a given time timestamp.
 *
 * @author Tommy
 */
public class Observation {
	
	/** The timestamp. */
	private Date timestamp;
	
	/** The observed indicators. */
	private HashMap<Indicator, IndicatorData> observedIndicators;
	
	/**
	 * Instantiates a new observation.
	 *
	 * @param timestamp the timestamp
	 */
	public Observation(String timestamp){
		this.timestamp = AppUtility.convertStringToDate(timestamp);
		observedIndicators = new HashMap<Indicator, IndicatorData>();
	}
	
	/**
	 * Adds the indicator.
	 *
	 * @param newInd the new indicator
	 * @param newValue the new value of the indicator
	 */
	public void addIndicator(Indicator newInd, IndicatorData newValue){
		observedIndicators.put(newInd, newValue);
	}

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Gets the indicators of the current observation.
	 *
	 * @return the indicators
	 */
	public Indicator[] getIndicators(){
		return observedIndicators.keySet().toArray(new Indicator[observedIndicators.keySet().size()]);
	}

	/**
	 * Gets the value of an indicator for this specific observation.
	 *
	 * @param indicator the indicator
	 * @param categoryTag the data category (plain, diff)
	 * @return the indicator value
	 */
	public String getValue(Indicator indicator, DataCategory categoryTag) {
		return observedIndicators.get(indicator).getCategoryValue(categoryTag);
	}
	
	/**
	 * Gets the value of an indicator for this specific observation.
	 *
	 * @param indicator the indicator
	 * @param categoryTag the data category (plain, diff)
	 * @return the indicator value
	 */
	public String getValue(String indicatorName, DataCategory categoryTag) {
		for(Indicator ind : getIndicators()){
			if(ind.getName().equals(indicatorName.trim()))
				return getValue(ind, categoryTag);
		}
		AppLogger.logError(getClass(), "NoSuchIndicator", "Unable to find Indicator '" + indicatorName + "'");
		return null;
	}
	
	/**
	 * Gets the number of observed indicators.
	 *
	 * @return the number of indicators
	 */
	public int getIndicatorNumber(){
		return observedIndicators.size();
	}

}
