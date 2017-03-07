/**
 * 
 */
package ippoz.multilayer.detector.commons.service;

/**
 * The Class IndicatorStat.
 * Stores statistical data for a given indicator in a given service.
 *
 * @author Tommy
 */
public class IndicatorStat {
	
	/** The indicator name. */
	private String indicatorName;
	
	/** Stats for the first observation. */
	private StatPair firstObs;
	
	/** Stats for the last observation. */
	private StatPair lastObs;
	
	/** Stats for all the observations. */
	private StatPair allObs;
	
	/**
	 * Instantiates a new indicator statistic.
	 *
	 * @param indicatorName the indicator name
	 * @param firstObs the first observation
	 * @param lastObs the last observation
	 * @param allObs the all observation
	 */
	public IndicatorStat(String indicatorName, StatPair firstObs, StatPair lastObs, StatPair allObs) {
		this.indicatorName = indicatorName;
		this.firstObs = firstObs;
		this.lastObs = lastObs;
		this.allObs = allObs;
	}
	
	/**
	 * Gets the indicator name.
	 *
	 * @return the indicator name
	 */
	public String getName(){
		return indicatorName;
	}
	
	/**
	 * Gets the stats for the first observation.
	 *
	 * @return the first observation's stats
	 */
	public StatPair getFirstObs() {
		return firstObs;
	}
	
	/**
	 * Gets the stats for the last observation.
	 *
	 * @return the last observation's stats
	 */
	public StatPair getLastObs() {
		return lastObs;
	}
	
	/**
	 * Gets the stats for all the observations.
	 *
	 * @return the stats for all the observations
	 */
	public StatPair getAllObs() {
		return allObs;
	}
	
}
