/**
 * 
 */
package ippoz.reload.commons.knowledge.data;

import ippoz.reload.commons.loader.DatasetIndex;
import ippoz.reload.commons.support.AppLogger;

/**
 * The Class Observation.
 * STores data related to observations of all the indicator at a given time timestamp.
 *
 * @author Tommy
 */
public class Observation {
	
	/** The index. */
	private DatasetIndex index;
	
	/** The observed indicators. */
	private Object[] observedIndicators;
	
	private int values;
	
	/**
	 * Instantiates a new observation.
	 *
	 * @param timestamp the timestamp
	 */
	public Observation(DatasetIndex index, int indicatorNumber){
		this.index = index;
		observedIndicators = new Object[indicatorNumber];
		values = 0;
	}
	
	/**
	 * Adds the indicator.
	 *
	 * @param newInd the new indicator
	 * @param newValue the new value of the indicator
	 */
	public void addIndicator(Object newValue){
		if(values < observedIndicators.length - 1)
			observedIndicators[values++] = newValue;
		else AppLogger.logError(getClass(), "NoSpace", "no space to store observations' data");
	}

	/**
	 * Gets the value of an indicator for this specific observation.
	 *
	 * @param indicator the indicator
	 * @param categoryTag the data category (plain, diff)
	 * @return the indicator value
	 */
	public Object getValue(int valueIndex) {
		if(valueIndex < values && valueIndex >= 0)
			return observedIndicators[valueIndex];
		else return null;
	}
	
	/**
	 * Gets the number of observed indicators.
	 *
	 * @return the number of indicators
	 */
	public int getIndicatorNumber(){
		return observedIndicators.length;
	}

	public DatasetIndex getIndex() {
		return index;
	}

}
