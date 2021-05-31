/**
 * 
 */
package ippoz.reload.evaluation;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.loader.LoaderBatch;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.metric.Metric;
import ippoz.reload.metric.result.MetricResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Class ExperimentVoter.
 *
 * @author Tommy
 */
public class ExperimentEvaluator extends Thread {
	
	/** The Constant ANOMALY_SCORE_LABEL. */
	public static final String ANOMALY_SCORE_LABEL = "Anomaly Score";
	
	/** The Constant FAILURE_LABEL. */
	public static final String FAILURE_LABEL = "Failure";
	
	/** The experiment name. */
	private LoaderBatch expBatch;
	
	/** The algorithm list. */
	private AlgorithmModel evalModel;
	
	/** The complete results of the voting. */
	private List<AlgorithmResult> modelResults;
	
	private List<AlgorithmResult> failureScores;
	
	private Knowledge evalKnowledge;
	
	/**
	 * Instantiates a new experiment voter.
	 *
	 * @param expData the experiment data
	 * @param algVoters the algorithm list
	 * @param pManager 
	 * @throws CloneNotSupportedException 
	 */
	public ExperimentEvaluator(AlgorithmModel evalModel, Knowledge evalKnowledge) throws CloneNotSupportedException {
		super();
		if(evalModel != null)
		this.evalModel = evalModel.clone();
		this.evalKnowledge = evalKnowledge;
		if(evalKnowledge != null)
			expBatch = evalKnowledge.getID();
		else expBatch = null;
	}
	
	public LoaderBatch getExperimentID(){
		if(expBatch == null)
			return null;
		return expBatch;
	}
	
	/**
	 * Deep clone of the voters' list.
	 *
	 * @param algVoters the algorithms
	 * @return the deep-cloned list
	 *//*
	private List<AlgorithmModel> deepClone(List<AlgorithmModel> algVoters) {
		List<AlgorithmModel> list = new ArrayList<AlgorithmModel>(algVoters.size());
		try {
			for(AlgorithmModel aVoter : algVoters){
				list.add(aVoter.clone());
			}
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone Experiment");
		}
		return list;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		int n = evalKnowledge.size();
		modelResults = new ArrayList<>(n);
		failureScores = new LinkedList<AlgorithmResult>();
		if(evalModel != null) {
			for(int i=0;i<n;i++){
				AlgorithmResult modelResult = evalModel.voteKnowledgeSnapshot(evalKnowledge, i);
				modelResults.add(modelResult);
				if(evalKnowledge.getInjection(i) != null){
					failureScores.add(modelResult);
				}
				if(evalKnowledge.getKnowledgeType() == KnowledgeType.SLIDING){
					((SlidingKnowledge)evalKnowledge).slide(i, modelResult.getScore());
				}
				if(((int)(n/10)) > 0 && i > 0 && i % ((int)(n/10)) == 0)
					AppLogger.logInfo(getClass(), ((int)(i / ((int)(n/10))))*10 + "% of test set is already evaluated.");
			}
			if(evalKnowledge.getKnowledgeType() == KnowledgeType.SLIDING){
				((SlidingKnowledge)evalKnowledge).reset();
			}
		}
	}
	
	public synchronized Map<Metric, MetricResult> calculateMetricScores(Metric[] validationMetrics) {
		Map<Metric, MetricResult> metResults = new HashMap<Metric, MetricResult>();
		for(Metric met : validationMetrics){
			metResults.put(met, met.evaluateAnomalyResults(modelResults));
		}
		return metResults;
	}

	public List<AlgorithmResult> getSingleAlgorithmScores() {
		return modelResults;
	}

	public int getFailuresNumber() {
		return failureScores != null ? failureScores.size() : 0;
	}
	
	public List<InjectedElement> getFailuresList() {
		List<InjectedElement> list = new LinkedList<InjectedElement>();
		for(int i=0;i<evalKnowledge.size();i++){
			list.add(evalKnowledge.getInjection(i));
		}
		return list;
	}

	public List<AlgorithmResult> getExperimentResults() {
		return modelResults;
	}

}
