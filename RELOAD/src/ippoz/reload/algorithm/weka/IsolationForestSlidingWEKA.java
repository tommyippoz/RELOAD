/**
 * 
 */
package ippoz.reload.algorithm.weka;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.weka.support.CustomIsolationForest;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.decisionfunction.StaticThresholdGreaterThanDecision;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Tommy
 *
 */
public class IsolationForestSlidingWEKA extends DataSeriesSlidingWEKAAlgorithm {

	private static final String N_TREES = "n_trees";
	
	private static final String SAMPLE_SIZE = "sample_size";
	
	private int nTrees;
	
	private int sampleSize;
	
	public IsolationForestSlidingWEKA(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false);
		nTrees = loadNTrees();
		sampleSize = loadSampleSize();
	}

	@Override
	protected AlgorithmResult evaluateSlidingWEKASnapshot(SlidingKnowledge sKnowledge, Instances windowInstances, Instance newInstance, Snapshot dsSnapshot) {
		CustomIsolationForest iForest;
		AlgorithmResult ar;
		try {
			if(windowInstances.size() > sampleSize)
				iForest = new CustomIsolationForest(nTrees, sampleSize);
			else iForest = new CustomIsolationForest(1, windowInstances.size());
			iForest.buildClassifier(windowInstances);
			ar = new AlgorithmResult(
					dsSnapshot.listValues(true), dsSnapshot.getInjectedElement(), iForest.classifyInstance(newInstance));
			getDecisionFunction().classifyScore(ar, true);
			return ar;
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to train and evaluate SlidingIsolationForest");
		}
		return AlgorithmResult.unknown(dsSnapshot.listValues(true), dsSnapshot.getInjectedElement());
	}
	
	@Override
	protected DecisionFunction buildClassifier() {
		return new StaticThresholdGreaterThanDecision(0.5);
	}
	
	private int loadSampleSize() {
		if(conf.hasItem(SAMPLE_SIZE) && AppUtility.isInteger(conf.getItem(SAMPLE_SIZE)))
			return Integer.parseInt(conf.getItem(SAMPLE_SIZE));
		else return -1;
	}

	private int loadNTrees() {
		if(conf.hasItem(N_TREES) && AppUtility.isInteger(conf.getItem(N_TREES)))
			return Integer.parseInt(conf.getItem(N_TREES));
		else return -1;
	}
	
}
