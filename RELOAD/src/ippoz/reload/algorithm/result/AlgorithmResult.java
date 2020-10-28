/**
 * 
 */
package ippoz.reload.algorithm.result;

import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.output.LabelledResult;

/**
 * The Class AlgorithmResult. Stores results of an evaluation of a Knowledge item by an algorithm.
 *
 * @author Tommy
 */
public class AlgorithmResult {
	
	/** The fault (if any) corresponding to each data point. */
	private boolean hasInjection;
	
	/** The evaluated score. */
	private double score;
	
	/** The evaluation in terms of AnomalyResult. */
	private AnomalyResult scoreEvaluation;
	
	/** The decision function used. */
	//private DecisionFunction dFunction;
	
	/** The confidence on the result. */
	private double confidence;

	private Object additionalScore;

	/**
	 * Instantiates a new algorithm result.
	 *
	 * @param dataValues the data values
	 * @param injection the injection
	 * @param score the score
	 * @param object 
	 */
	public AlgorithmResult(boolean hasInjection, double score, double confidence, Object additionalScore) {
		this.hasInjection = hasInjection;
		this.score = score;
		this.confidence = confidence;
		this.additionalScore = null;
	}
	
	public AlgorithmResult(LabelledResult lr) {
		this(lr.getLabel(), lr.getValue(), lr.getConfidence(), null);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new algorithm result.
	 *
	 * @param dataValues the data values
	 * @param injection the injection
	 * @param score the score
	 * @param scoreEvaluation the score evaluation
	 * @param dFunction the decision function
	 */
	public AlgorithmResult(boolean hasInjection, double score, AnomalyResult scoreEvaluation, double confidence) {
		this.hasInjection = hasInjection;
		this.score = score;
		this.scoreEvaluation = scoreEvaluation;
		//this.dFunction = dFunction;
		this.confidence = confidence;
	}

	/**
	 * Gets the evaluation score.
	 *
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * Gets the score evaluation in terms of AnomalyResult.
	 *
	 * @return the score evaluation
	 */
	public AnomalyResult getScoreEvaluation() {
		return scoreEvaluation;
	}

	/**
	 * Sets the decision function.
	 *
	 * @param dFunction the new decision function
	 
	public void setDecisionFunction(DecisionFunction dFunction) {
		this.dFunction = dFunction;
	}*/
	
	/**
	 * Sets the score evaluation.
	 *
	 * @param scoreEvaluation the new score evaluation
	 */
	public void setScoreEvaluation(AnomalyResult scoreEvaluation){
		this.scoreEvaluation = scoreEvaluation;
	}

	/**
	 * Gets the decision function.
	 *
	 * @return the decision function
	 
	public DecisionFunction getDecisionFunction() {
		return dFunction;
	}*/

	/**
	 * Outputs the default AlgorithmResult for an erroneous evaluation e.g., data is missing.
	 *
	 * @param dataValues the data values
	 * @param injection the injection
	 * @return the algorithm result
	 */
	public static AlgorithmResult error(boolean hasInjection) {
		return new AlgorithmResult(hasInjection, Double.NaN, AnomalyResult.ERROR, 1);
	}

	/**
	 * Outputs the default AlgorithmResult for an unknown evaluation e.g., not enough data is provided.
	 *
	 * @param dataValues the data values
	 * @param injection the injection
	 * @return the algorithm result
	 */
	public static AlgorithmResult unknown(boolean injection) {
		return new AlgorithmResult(injection, Double.NaN, AnomalyResult.UNKNOWN, 0);
	}	
	
	/**
	 * Prints the header to be used when printing files.
	 *
	 * @param sep the separator
	 * @return the string
	 */
	public String printFileHeader(String sep){
		return "data (enclosed in {})" + sep + 
				"injection" + sep +
				"algorithm score" + sep + 
				"score evaluation" + sep +
				"decision function";
	}

	/**
	 * Sets the score.
	 *
	 * @param value the new score
	 */
	public void setScore(double value) {
		score = value;		
	}

	public boolean getBooleanScore() {
		return getScoreEvaluation() == AnomalyResult.ANOMALY;
	}
	
	public double getConfidence() {
		return confidence;
	}

	public Object getAdditionalScore() {
		return additionalScore;
	}

	public boolean isCorrect() {
		return getBooleanScore() == hasInjection();
	}

	public boolean hasInjection() {
		return hasInjection;
	}

	public double getConfidencedScore() {
		if(getScoreEvaluation() == AnomalyResult.ANOMALY)
			return 0.5 + getConfidence()*0.5;
		else return 0.5 - getConfidence()*0.5;
	}

}
