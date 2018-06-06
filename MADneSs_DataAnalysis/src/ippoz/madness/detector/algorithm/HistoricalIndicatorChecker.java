/**
 * 
 */
package ippoz.madness.detector.algorithm;

import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.service.ServiceCall;
import ippoz.madness.detector.commons.service.StatPair;
import ippoz.madness.detector.commons.support.AppLogger;

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
	public double evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		double anomalyRate = 0.0;
		DataSeriesSnapshot dsSnapshot = (DataSeriesSnapshot) sysSnapshot;
		if(sysSnapshot.getServiceCalls().size() > 0){
			for(ServiceCall sCall : sysSnapshot.getServiceCalls()){
				anomalyRate = anomalyRate + analyzeCall(dsSnapshot.getSnapValue().getFirst(), sCall, dsSnapshot.getSnapStat(sCall, knowledge.getStats()));
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
