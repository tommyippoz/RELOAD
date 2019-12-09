/**
 * 
 */
package ippoz.reload.algorithm.result;

import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.metric.Metric;

import java.util.List;

/**
 * The Class AlgorithmResult. Stores results of an evaluation of a Knowledge item by an algorithm.
 *
 * @author Tommy
 */
public class AlgorithmResult {
	
	/** The data point values. */
	private List<Double> dataValues; 
	
	/** The fault (if any) corresponding to each data point. */
	private InjectedElement injection;
	
	/** The evaluated score. */
	private double score;
	
	/** The evaluation in terms of AnomalyResult. */
	private AnomalyResult scoreEvaluation;
	
	/** The decision function used. */
	private DecisionFunction dFunction;

	/**
	 * Instantiates a new algorithm result.
	 *
	 * @param dataValues the data values
	 * @param injection the injection
	 * @param score the score
	 */
	public AlgorithmResult(List<Double> dataValues, InjectedElement injection, double score) {
		this.dataValues = dataValues;
		this.injection = injection;
		this.score = score;
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
	public AlgorithmResult(List<Double> dataValues, InjectedElement injection, double score, AnomalyResult scoreEvaluation, DecisionFunction dFunction) {
		this.dataValues = dataValues;
		this.injection = injection;
		this.score = score;
		this.scoreEvaluation = scoreEvaluation;
		this.dFunction = dFunction;
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
	 * Gets the evaluation score.
	 *
	 * @return the score
	 */
	public List<Double> getData() {
		return dataValues;
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
	 */
	public void setDecisionFunction(DecisionFunction dFunction) {
		this.dFunction = dFunction;
	}
	
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
	 */
	public DecisionFunction getDecisionFunction() {
		return dFunction;
	}

	/**
	 * Outputs the default AlgorithmResult for an erroneous evaluation e.g., data is missing.
	 *
	 * @param dataValues the data values
	 * @param injection the injection
	 * @return the algorithm result
	 */
	public static AlgorithmResult error(List<Double> dataValues, InjectedElement injection) {
		return new AlgorithmResult(dataValues, injection, Double.NaN, AnomalyResult.ERROR, null);
	}

	/**
	 * Outputs the default AlgorithmResult for an unknown evaluation e.g., not enough data is provided.
	 *
	 * @param dataValues the data values
	 * @param injection the injection
	 * @return the algorithm result
	 */
	public static AlgorithmResult unknown(List<Double> dataValues, InjectedElement injection) {
		return new AlgorithmResult(dataValues, injection, Double.NaN, AnomalyResult.UNKNOWN, null);
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
	 * Converts results to file string.
	 *
	 * @param sep the separator
	 * @return the string
	 */
	public String toFileString(String sep){
		return "{" +  dataValues.toString() + "}" + sep + 
				injection.getDescription() + sep +
				score + sep + 
				scoreEvaluation.toString() + sep +
				dFunction.getDecisionFunctionType();
	}

	/**
	 * Sets the score.
	 *
	 * @param value the new score
	 */
	public void setScore(double value) {
		score = value;		
	}

	public InjectedElement getInjection() {
		return injection;
	}

	public boolean getBooleanScore() {
		return Metric.anomalyTrueFalse(getScore());
	}

}
