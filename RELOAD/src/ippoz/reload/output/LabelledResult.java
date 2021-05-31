/**
 * 
 */
package ippoz.reload.output;

import ippoz.reload.algorithm.result.AlgorithmResult;


/**
 * @author Tommy
 *
 */
public class LabelledResult {
	
	private boolean label;
	
	private double value;
	
	private double confidencedScore;
	
	private boolean isUnknown;
	
	public LabelledResult(AlgorithmResult ar) {
		label = ar.isAnomalous();
		value = ar.getScore();
		isUnknown = ar.isUnknown();
		confidencedScore = ar.getConfidencedScore();
	}

	public boolean getLabel() {
		return label;
	}
	
	public double getValue() {
		return value;
	}
	
	public double getConfidencedScore() {
		return confidencedScore;
	}

	@Override
	public String toString() {
		return "LabelledResult [label=" + label + ", value=" + value + "]";
	}

	public double getConfidence() {
		return 0;
	}

	public boolean isUnknown() {
		return isUnknown;
	}	
	
}
