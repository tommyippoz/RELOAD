/**
 * 
 */
package ippoz.multilayer.detector.algorithm;

import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.DataSeriesSnapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.StatPair;
import ippoz.multilayer.detector.commons.support.AppLogger;

/**
 * The Class HistoricalIndicatorChecker.
 * Defines a Checker that is able to evaluate if the observation is in the range of (avg,std)
 *
 * @author Tommy
 */
public class HistoricalIndicatorChecker extends DataSeriesDetectionAlgorithm {
	
	public static final String HIST_INTERVAL = "interval_width";
	
	/**
	 * Instantiates a new historical indicator checker.
	 *
	 * @param indicator the indicator
	 * @param categoryTag the data category tag
	 * @param conf the configuration
	 */
	public HistoricalIndicatorChecker(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
	}

	@Override
	public double evaluateDataSeriesSnapshot(DataSeriesSnapshot sysSnapshot) {
		double anomalyRate = 0.0;
		if(sysSnapshot.getServiceCalls().size() > 0){
			for(ServiceCall sCall : sysSnapshot.getServiceCalls()){
				anomalyRate = anomalyRate + analyzeCall(sysSnapshot.getSnapValue(), sCall, sysSnapshot.getSnapStat(sCall));
			}
			return anomalyRate / sysSnapshot.getServiceCalls().size();
		} else return 0;
	}

	/**
	 * Analyse call during running.
	 *
	 * @param value the string value
	 * @param sCall the service call
	 * @param stat the service stat
	 * @return the result of the evaluation
	 */
	private double analyzeCall(Double value, ServiceCall sCall, StatPair stat) {
		if(stat != null)
			return evaluateAbsDiffRate(value, stat, Double.valueOf(conf.getItem(HIST_INTERVAL)));
		else AppLogger.logError(getClass(), "StatError", "Unable to find Stat for " + sCall.getServiceName() + ":" + dataSeries.getName());
		return 0.0;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.algorithm.DetectionAlgorithm#printImageResults(java.lang.String, java.lang.String)
	 */
	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.algorithm.DetectionAlgorithm#printTextResults(java.lang.String, java.lang.String)
	 */
	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

}
