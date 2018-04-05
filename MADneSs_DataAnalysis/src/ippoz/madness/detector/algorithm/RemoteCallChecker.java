/**
 * 
 */
package ippoz.madness.detector.algorithm;

import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.service.ServiceCall;
import ippoz.madness.detector.commons.support.AppUtility;

/**
 * The Class RemoteCallChecker.
 * Checks if the duration of a service call is compliant with the expectations.
 *
 * @author Tommy
 */
public class RemoteCallChecker extends DetectionAlgorithm {
	
	/** The weight tag. */
	private static String WEIGHT_TAG = "rcc_weight";

	/** The remote call checker weight. */
	private double weight;
	
	/**
	 * Instantiates a new remote call checker.
	 *
	 * @param weight the weight
	 */
	public RemoteCallChecker(double weight) {
		super(null);
		this.weight = weight;
	}

	/**
	 * Instantiates a new remote call checker.
	 *
	 * @param conf the configuration
	 */
	public RemoteCallChecker(AlgorithmConfiguration conf) {
		super(conf);
		weight = Double.parseDouble(conf.getItem(WEIGHT_TAG));
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.algorithm.DetectionAlgorithm#evaluateSnapshot(ippoz.multilayer.detector.data.Snapshot)
	 */
	@Override
	public double evaluateSnapshot(Knowledge knowledge, int currentIndex) {
		double evalResult = 0.0;
		Snapshot sysSnapshot = knowledge.get(getAlgorithmType(), currentIndex, null);
		for(ServiceCall call : sysSnapshot.getServiceCalls()){
			evalResult = evalResult + analyzeServiceCall(knowledge, sysSnapshot, call);
		}
		return evalResult / sysSnapshot.getServiceCalls().size();
	}

	/**
	 * Analyse service call.
	 *
	 * @param snapTime the snapshot time
	 * @param call the service call
	 * @param serviceStat the service stat
	 * @return the double
	 */
	private double analyzeServiceCall(Knowledge knowledge, Snapshot snapshot, ServiceCall call) {
		if(call.getEndTime().compareTo(snapshot.getTimestamp()) == 0){
			if(!call.getResponseCode().equals("200"))
				return weight;
			else return evaluateAbsDiff(AppUtility.getSecondsBetween(call.getEndTime(), call.getStartTime()), knowledge.getServiceObsStat(call.getServiceName()), 1.0);
		} else return evaluateOverDiff(AppUtility.getSecondsBetween(snapshot.getTimestamp(), call.getStartTime()), knowledge.getServiceTimingStat(call.getServiceName()));
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

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.algorithm.DetectionAlgorithm#getWeight()
	 */
	@Override
	public Double getWeight() {
		return weight;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	@Override
	public DataSeries getDataSeries() {
		// TODO Auto-generated method stub
		return null;
	}

}
