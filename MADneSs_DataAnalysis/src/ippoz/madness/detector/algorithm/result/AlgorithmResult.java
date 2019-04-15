/**
 * 
 */
package ippoz.madness.detector.algorithm.result;

import ippoz.madness.detector.decisionfunction.AnomalyResult;
import ippoz.madness.detector.decisionfunction.DecisionFunction;
import ippoz.reload.commons.failure.InjectedElement;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class AlgorithmResult {
	
	private List<Double> dataValues; 
	
	private InjectedElement injection;
	
	private double score;
	
	private AnomalyResult scoreEvaluation;
	
	private DecisionFunction dFunction;

	public AlgorithmResult(List<Double> dataValues, InjectedElement injection, double score) {
		this.dataValues = dataValues;
		this.injection = injection;
		this.score = score;
	}

	public AlgorithmResult(List<Double> dataValues, InjectedElement injection, double score, AnomalyResult scoreEvaluation, DecisionFunction dFunction) {
		this.dataValues = dataValues;
		this.injection = injection;
		this.score = score;
		this.scoreEvaluation = scoreEvaluation;
		this.dFunction = dFunction;
	}

	public double getScore() {
		return score;
	}

	public AnomalyResult getScoreEvaluation() {
		return scoreEvaluation;
	}

	public void setDecisionFunction(DecisionFunction dFunction) {
		this.dFunction = dFunction;
	}
	
	public void setScoreEvaluation(AnomalyResult scoreEvaluation){
		this.scoreEvaluation = scoreEvaluation;
	}

	public DecisionFunction getDecisionFunction() {
		return dFunction;
	}

	public static AlgorithmResult error(List<Double> dataValues, InjectedElement injection) {
		return new AlgorithmResult(dataValues, injection, Double.NaN, AnomalyResult.ERROR, null);
	}

	public static AlgorithmResult unknown(List<Double> dataValues, InjectedElement injection) {
		return new AlgorithmResult(dataValues, injection, Double.NaN, AnomalyResult.UNKNOWN, null);
	}	
	
	public String printFileHeader(String sep){
		return "data (enclosed in {})" + sep + 
				"injection" + sep +
				"algorithm score" + sep + 
				"score evaluation" + sep +
				"decision function";
	}
	
	public String toFileString(String sep){
		return "{" +  dataValues.toString() + "}" + sep + 
				injection.getDescription() + sep +
				score + sep + 
				scoreEvaluation.toString() + sep +
				dFunction.getClassifierType();
	}

}
