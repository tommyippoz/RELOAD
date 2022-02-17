/**
 * 
 */
package ippoz.reload.evaluation;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.loader.LoaderBatch;
import ippoz.reload.commons.support.AppLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	
	private Knowledge evalKnowledge;
	
	private int startIndex;
	
	private int endIndex;
	
	/**
	 * Instantiates a new experiment voter.
	 *
	 * @param expData the experiment data
	 * @param algVoters the algorithm list
	 * @param pManager 
	 * @throws CloneNotSupportedException 
	 */
	public ExperimentEvaluator(AlgorithmModel evalModel, Knowledge evalKnowledge, int startIndex, int endIndex) throws CloneNotSupportedException {
		super();
		this.evalModel = evalModel;
		this.evalKnowledge = evalKnowledge;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		if(evalKnowledge != null)
			expBatch = evalKnowledge.getID();
		else expBatch = null;
	}
	
	public LoaderBatch getExperimentID(){
		if(expBatch == null)
			return null;
		return expBatch;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		int n = evalKnowledge.size();
		modelResults = new ArrayList<>(n);
		if(evalModel != null) {
			for(int i=startIndex;i<endIndex;i++){
				AlgorithmResult modelResult = evalModel.voteKnowledgeSnapshot(evalKnowledge, i);
				modelResults.add(modelResult);
			}
			AppLogger.logInfo(getClass(), "Evaluator Thread [" + startIndex + " - " + endIndex + "] Completed.");
		}
	}

	public List<AlgorithmResult> getSingleAlgorithmScores() {
		return modelResults;
	}
	
	public List<InjectedElement> getFailuresList() {
		List<InjectedElement> list = new LinkedList<InjectedElement>();
		for(int i=0;i<evalKnowledge.size();i++){
			list.add(evalKnowledge.getInjection(i));
		}
		return list;
	}

	public void flushScores() {
		modelResults.clear();
		modelResults = null;
	}

}
