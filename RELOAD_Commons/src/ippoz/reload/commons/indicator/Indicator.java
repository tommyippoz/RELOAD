/**
 * 
 */
package ippoz.reload.commons.indicator;

/**
 * The Class Indicator.
 * Represents an indicator monitored during the experiments, linked with its layer and data type.
 *
 * @author Tommy
 */
public class Indicator implements Comparable<Indicator>{
	
	/** The indicator name. */
	private String indicatorName;
	
	/** The indicator value type. */
	private Class<?> indicatorType;
	
	/**
	 * Instantiates a new indicator.
	 *
	 * @param indicatorName the indicator name
	 * @param indicatorLayer the indicator layer
	 * @param indicatorType the indicator value type
	 */
	public Indicator(String indicatorName, Class<?> indicatorType) {
		this.indicatorName = indicatorName;
		this.indicatorType = indicatorType;
	}

	/**
	 * Gets the value casing raw values depending on its value type.
	 *
	 * @param rawValue the raw value
	 * @return the casted value
	 */
	public Object getValue(String rawValue){
		return indicatorType.cast(rawValue);
	}
	
	/**
	 * Gets the indicator name.
	 *
	 * @return the name
	 */
	public String getName(){
		return indicatorName;
	}
	
	public Class<?> getIndicatorType(){
		return indicatorType;
	}

	@Override
	public int compareTo(Indicator other) {
		return indicatorName.compareTo(other.getName());
	}

	@Override
	public String toString() {
		return "[" + indicatorName + ", " + indicatorType.toString() + "]";
	}
	
	

}
