/**
 * 
 */
package ippoz.multilayer.detector.performance;

import ippoz.multilayer.commons.layers.LayerType;
import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.support.AppUtility;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class ExperimentPerformance.
 * Used by PerformanceManager to store performance data about a specific experiment.
 *
 * @author Tommy
 */
public class ExperimentPerformance {
	
	/** The Constant OT_SERIES. */
	private static final String OT_SERIES = "ot";
	
	/** The Constant PMTT_SERIES. */
	private static final String PMTT_SERIES = "pmtt";
	
	/** The Constant DAT_SERIES. */
	private static final String DAT_SERIES = "dat";
	
	/** The experiment name. */
	private String expName;
	
	/** The experiment observations. */
	private int expObs;
	
	/** The number of indicators per layer. */
	private HashMap<LayerType, Integer> layerIndicators;
	
	/** The number of algorithms per detection category. */
	private HashMap<String, Integer> detCategories;
	
	/** The monitor time series. */
	private HashMap<String, HashMap<LayerType, LinkedList<Integer>>> monitorTimeSeries;
	
	/** The detection times. */
	private HashMap<String, LinkedList<Integer>> detectionTimes;
	
	/**
	 * Instantiates a new experiment performance.
	 *
	 * @param expData the experiment data
	 * @param algCategories the algorithm categories
	 * @param timeSeries the time series
	 */
	public ExperimentPerformance(ExperimentData expData, HashMap<String, Integer> algCategories, HashMap<String, LinkedList<Integer>> timeSeries) {
		expName = expData.getName();
		expObs = expData.getSnapshotNumber();
		layerIndicators = expData.getLayerIndicators();
		monitorTimeSeries = expData.getMonitorPerformanceIndexes();
		detCategories = algCategories;
		detectionTimes = timeSeries;
	}
	
	/**
	 * Summarizes the header.
	 *
	 * @return the header
	 */
	public String[] summaryHeader(){
		String[] header = new String[3];
		header[0] = "exp,,";
		header[1] = "name,obs,";
		header[2] = ",,";
		for(String serieTag : new String[]{OT_SERIES, PMTT_SERIES, DAT_SERIES}){
			header[0] = header[0] + serieTag;
			for(LayerType layer : monitorTimeSeries.get(serieTag).keySet()){
				header[0] = header[0] + ",,,";
				header[1] = header[1] + layer.toString() + ",,,";
				header[2] = header[2] + "ind_number,avg,var,";
			}
		}
		header[0] = header[0] + "dt";
		for(String detCategory : detectionTimes.keySet()){
			header[0] = header[0] + ",,,";
			header[1] = header[1] + detCategory + ",,,";
			header[2] = header[2] + "alg_number,avg,var,";
		}
		
		return header;
	}
	
	/**
	 * Summarizes the monitor attributes (ot, pmtt, dat).
	 *
	 * @return the summary
	 */
	private String monitorSummary(){
		String mSummary = "";
		mSummary = mSummary + seriesSummary(OT_SERIES);
		mSummary = mSummary + seriesSummary(PMTT_SERIES);
		mSummary = mSummary + seriesSummary(DAT_SERIES);
		return mSummary;
	}
	
	/**
	 * Summarizes the series.
	 *
	 * @param serieTag the serie tag
	 * @return the summary
	 */
	private String seriesSummary(String serieTag) {
		String sSummary = "";
		HashMap<LayerType, LinkedList<Integer>> map = monitorTimeSeries.get(serieTag);
		double[][] layerStats = new double[map.keySet().size()][2];
		int i = 0;
		for(LayerType layer : map.keySet()){
			layerStats[i][0] = AppUtility.calcAvg(map.get(layer));
			layerStats[i][1] = AppUtility.calcStd(map.get(layer), layerStats[i][0]);
			sSummary = sSummary + layerIndicators.get(layer) + "," + layerStats[i][0] + "," + layerStats[i][1] + ",";
			i++;
		}
		return sSummary;
	}
	
	/**
	 * Detector summary.
	 *
	 * @return the summary
	 */
	private String detectorSummary(){
		String dSummary = "";
		for(String detCategory : detectionTimes.keySet()){
			dSummary = dSummary + detCategories.get(detCategory) + "," + AppUtility.calcAvg(detectionTimes.get(detCategory)) + "," + AppUtility.calcStd(detectionTimes.get(detCategory), AppUtility.calcAvg(detectionTimes.get(detCategory))) + ",";
		}
		return dSummary;
	}

	/**
	 * Compact summary.
	 *
	 * @return the compact summary
	 */
	public String compactSummary(){
		return expName + "," + expObs + "," + monitorSummary() + detectorSummary();
	}

}
