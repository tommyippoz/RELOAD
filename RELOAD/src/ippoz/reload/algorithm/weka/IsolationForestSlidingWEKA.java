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

// TODO: Auto-generated Javadoc
/**
 * The Class IsolationForestSlidingWEKA.
 *
 * @author Tommy
 */
public class IsolationForestSlidingWEKA extends DataSeriesSlidingWEKAAlgorithm {

	/** The Constant N_TREES. */
	private static final String N_TREES = "n_trees";
	
	/** The Constant SAMPLE_SIZE. */
	private static final String SAMPLE_SIZE = "sample_size";
	
	/** The number of trees. */
	private int nTrees;
	
	/** The sample size. */
	private int sampleSize;
	
	/**
	 * Instantiates a new WEKA isolation forest declined in a sliding fashion.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public IsolationForestSlidingWEKA(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false);
		nTrees = loadNTrees();
		sampleSize = loadSampleSize();
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.weka.DataSeriesSlidingWEKAAlgorithm#evaluateSlidingWEKASnapshot(ippoz.reload.commons.knowledge.SlidingKnowledge, weka.core.Instances, weka.core.Instance, ippoz.reload.commons.knowledge.snapshot.Snapshot)
	 */
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
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to train and evaluate SlidingIsolationForest");
		}
		return AlgorithmResult.unknown(dsSnapshot.listValues(true), dsSnapshot.getInjectedElement());
	}
	
	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DetectionAlgorithm#buildClassifier()
	 */
	@Override
	protected DecisionFunction buildClassifier() {
		return new StaticThresholdGreaterThanDecision(0.5);
	}
	
	/**
	 * Load the number of samples to be used by each tree in the forest.
	 *
	 * @return the int
	 */
	private int loadSampleSize() {
		if(conf.hasItem(SAMPLE_SIZE) && AppUtility.isInteger(conf.getItem(SAMPLE_SIZE)))
			return Integer.parseInt(conf.getItem(SAMPLE_SIZE));
		else return -1;
	}

	/**
	 * Loads the number of trees in the forest.
	 *
	 * @return the int
	 */
	private int loadNTrees() {
		if(conf.hasItem(N_TREES) && AppUtility.isInteger(conf.getItem(N_TREES)))
			return Integer.parseInt(conf.getItem(N_TREES));
		else return -1;
	}
	
}
