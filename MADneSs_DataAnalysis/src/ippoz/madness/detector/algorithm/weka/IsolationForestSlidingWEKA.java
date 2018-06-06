/**
 * 
 */
package ippoz.madness.detector.algorithm.weka;

import ippoz.madness.detector.algorithm.weka.support.CustomIsolationForest;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;
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
	protected double evaluateSlidingWEKASnapshot(SlidingKnowledge sKnowledge, Instances windowInstances, Instance newInstance) {
		CustomIsolationForest iForest;
		try {
			if(windowInstances.size() > sampleSize)
				iForest = new CustomIsolationForest(nTrees, sampleSize);
			else iForest = new CustomIsolationForest(1, windowInstances.size());
			iForest.buildClassifier(windowInstances);
			return iForest.classifyInstance(newInstance);
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to train and evaluate SlidingIsolationForest");
		}
		return 0;
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
