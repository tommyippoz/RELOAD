/**
 * 
 */
package ippoz.reload.featureselection;

/**
 * The Enum FeatureSelectorType. Defines all the possible feature selection strategies.
 *
 * @author Tommy
 */
public enum FeatureSelectorType {
	
	/** The variance. */
	VARIANCE, 
	
	/** The Pearson correlation. */
	PEARSON_CORRELATION,
	
	/** The information gain. */
	INFORMATION_GAIN, 
	
	/** Relief. */
	RELIEF, 
	
	/** Selection through OneR classifier */
	ONER, 
	
	/** Principal Component Analysis */
	PCA, 
	
	/** Feature Selection through GAIN Ratio */
	GAIN_RATIO, 
	
	CHI_SQUARED, 
	
	RANDOM_FORESTS, 
	
	J48

}
