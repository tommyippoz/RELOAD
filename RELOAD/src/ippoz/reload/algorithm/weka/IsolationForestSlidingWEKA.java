/**
 * 
 */
package ippoz.reload.algorithm.weka;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.weka.support.CustomIsolationForest;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;

import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;
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
	public IsolationForestSlidingWEKA(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, false);
		nTrees = loadNTrees();
		sampleSize = loadSampleSize();
	}

	

	@Override
	protected boolean checkCalculationCondition(double[] snapArray) {
		return true;
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
	
	@Override
	public Map<String, String[]> getDefaultParameterValues() {
		Map<String, String[]> defPar = new HashMap<String, String[]>();
		defPar.put("n_trees", new String[]{"1", "2", "3", "5"});
		defPar.put("sample_size", new String[]{"10", "20", "50", "100"});
		return defPar;
	}

	@Override
	protected Pair<Double, Object> evaluateSlidingWEKASnapshot(SlidingKnowledge sKnowledge, Instances windowInstances, Instance newInstance, Snapshot dsSnapshot) {
		CustomIsolationForest iForest;
		try {
			if(windowInstances.size() > sampleSize)
				iForest = new CustomIsolationForest(nTrees, sampleSize);
			else iForest = new CustomIsolationForest(1, windowInstances.size());
			iForest.buildClassifier(windowInstances);
			return new Pair<Double, Object>(iForest.classifyInstance(newInstance), null);
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to train and evaluate SlidingIsolationForest");
			return new Pair<Double, Object>(Double.NaN, null);
		}
	}
	
}
