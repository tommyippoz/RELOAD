package ippoz.reload.decisionfunction;

/**
 * The Enum DecisionFunctionType. Describes all the possible types of decision functions. 
 */
public enum DecisionFunctionType {
	
	/** The threshold. */
	THRESHOLD, 
	
	/** The double threshold extern. */
	DOUBLE_THRESHOLD_EXTERN,
	
	/** The double threshold intern. */
	DOUBLE_THRESHOLD_INTERN,
	
	/** The iqr. */
	IQR, 
	
	/** The left positive iqr. */
	LEFT_POSITIVE_IQR, 
	
	/** The left iqr. */
	LEFT_IQR, 
	
	/** The right iqr. */
	RIGHT_IQR, 
	
	/** The left positive confidence interval. */
	LEFT_POSITIVE_CONFIDENCE_INTERVAL, 
	
	/** The left confidence interval. */
	LEFT_CONFIDENCE_INTERVAL, 
	
	/** The right confidence interval. */
	RIGHT_CONFIDENCE_INTERVAL, 
	
	/** The confidence interval. */
	CONFIDENCE_INTERVAL, 
	
	/** The static threshold greaterthan. */
	STATIC_THRESHOLD_GREATERTHAN,
	
	/** The static threshold lowerthan. */
	STATIC_THRESHOLD_LOWERTHAN,
	
	/** The log threshold. */
	LOG_THRESHOLD, 
	
	/** The cluster. */
	CLUSTER, 

}
