package ippoz.madness.detector.decisionfunction;

public enum AnomalyResult {
	
	/* Anomaly. */
	ANOMALY,
	
	/* Not sure. */
	MAYBE,
	
	/* Normal. */
	NORMAL,
	
	/* Do not have enough elements to decide. */
	UNKNOWN,
	
	/* Error while executing classifier. */
	ERROR

}
