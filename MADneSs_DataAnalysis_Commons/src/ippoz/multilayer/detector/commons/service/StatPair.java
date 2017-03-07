/**
 * 
 */
package ippoz.multilayer.detector.commons.service;

import ippoz.multilayer.detector.commons.support.AppUtility;

import java.util.LinkedList;

/**
 * The Class StatPair.
 * Represents an (avg,std) pair for statistic storage purposes.
 *
 * @author Tommy
 */
public class StatPair {
	
	/** The average value. */
	private double avg;
	
	/** The standard deviation. */
	private double std;
	
	/**
	 * Instantiates a new stat pair.
	 *
	 * @param avg the average
	 * @param std the standard deviation
	 */
	public StatPair(String avg, String std) {
		if(avg != null)
			this.avg = Double.parseDouble(avg);
		else this.avg = 0;
		if(std != null)
			this.std = Double.parseDouble(std);
		else this.std = this.avg / 2;
	}
	
	/**
	 * Instantiates a new stat pair.
	 *
	 * @param avg the average
	 * @param std the standard deviation
	 */
	public StatPair(double avg, double std) {
		this.avg = avg;
		this.std = std;
	}
	
	public StatPair(LinkedList<Double> pCalc) {
		avg = AppUtility.calcAvg(pCalc);
		std = AppUtility.calcStd(pCalc, avg);
	}

	/**
	 * Gets the average.
	 *
	 * @return the average
	 */
	public double getAvg() {
		return avg;
	}
	
	/**
	 * Gets the standard deviation.
	 *
	 * @return the standard deviation
	 */
	public double getStd() {
		return std;
	}

}
