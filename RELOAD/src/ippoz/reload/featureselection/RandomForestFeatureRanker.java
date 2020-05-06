/**
 * 
 */
package ippoz.reload.featureselection;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.WekaException;

/**
 * @author Tommy
 *
 */
public class RandomForestFeatureRanker extends WEKAClassifierRanker {

	public RandomForestFeatureRanker(double selectorThreshold, boolean isRankThreshold) {
		super(FeatureSelectorType.RANDOM_FORESTS, selectorThreshold, isRankThreshold, true, false);
	}

	@Override
	protected double[] getClassifierScores(Classifier wcf, int featureNumber) {
		try {
			if(wcf instanceof RandomForest)
				return ((RandomForest)wcf).computeAverageImpurityDecreasePerAttribute(null);
		} catch (WekaException e) {	}
		return null;
	}

	@Override
	protected Classifier instantiateWEKAClassifier() {
		RandomForest rf = new RandomForest();
		rf.setComputeAttributeImportance(true);
		return rf;
	}

	@Override
	public String getSelectorName() {
		return "RandomForest";
	}

}
