package ippoz.reload.decisionfunction;

/**
 * The Enum AnomalyResult. It is used to describe the results of evaluations of a data point, 
 * either ANOMALY, NORMAL, UNKNOWN (do not know), ERROR (some values to decide on anomalies are missing), 
 * or MAYBE (not sure if the evaluation is trustable)
 */
public enum AnomalyResult {
	
	/** The anomaly. */
	/* Anomaly. */
	ANOMALY,
	
	/** The maybe. */
	/* Not sure. */
	MAYBE,
	
	/** The normal. */
	/* Normal. */
	NORMAL,
	
	/** The unknown. */
	/* Do not have enough elements to decide. */
	UNKNOWN,
	
	/** The error. */
	/* Error while executing classifier. */
	ERROR

}
