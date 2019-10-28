/**
 * 
 */
package ippoz.reload.commons.support;

import ippoz.reload.commons.failure.InjectedElement;

import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class TimedResult.
 *
 * @author Tommy
 */
public class TimedResult extends TimedValue {
	
	/** The algorithm score. */
	private Double algorithmScore;
	
	/** The injected element. */
	private InjectedElement injectedElement;

	/**
	 * Instantiates a new timed result.
	 *
	 * @param the time
	 * @param value the anomaly score (double)
	 * @param algorithmScore the algorithm score
	 * @param injectedElement if fault/attaclk manifestated
	 */
	public TimedResult(Date vDate, Double value, Double algorithmScore, InjectedElement injectedElement) {
		super(vDate, value);
		this.algorithmScore = algorithmScore;
		this.injectedElement = injectedElement;
	}
	
	/**
	 * Gets the algorithm score.
	 *
	 * @return the algorithm score
	 */
	public Double getAlgorithmScore(){
		return algorithmScore;
	}
	
	/**
	 * Gets the injected element.
	 *
	 * @return the injected element
	 */
	public InjectedElement getInjectedElement(){
		return injectedElement;
	}

	@Override
	public String toString() {
		return "[AScore:" + algorithmScore + ", Score:" + getValue() + ", Injection:" + (injectedElement!= null) + "]";
	}
	
	public void updateEvaluationScore(double newEvalScore) {
		updateValue(newEvalScore);
	}	
 
}
