package ippoz.reload.manager.evaluate;

import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.metric.Metric;
import ippoz.reload.voter.ScoresVoter;

import java.util.List;
import java.util.Map;

public class OptimizatorManager extends EvaluatorManager {

	public OptimizatorManager(ScoresVoter voter, String outputFolder, String scoresFile,
			Map<KnowledgeType, List<Knowledge>> map, Metric[] validationMetrics, boolean printOutput) {
		super(EvaluatorType.OPTIMIZATION, voter, outputFolder, scoresFile, map, validationMetrics, printOutput);
		// TODO Auto-generated constructor stub
	}


}
