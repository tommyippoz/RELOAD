/**
 * 
 */
package ippoz.multilayer.detector.commons.data;

import ippoz.madness.commons.indicator.Indicator;
import ippoz.madness.commons.layers.LayerType;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.failure.InjectedElement;
import ippoz.multilayer.detector.commons.invariants.DataSeriesMember;
import ippoz.multilayer.detector.commons.invariants.Invariant;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * The Class ExperimentData.
 * Stores information about the data of each experiment (service calls, snapshots, timings, injections, stats)
 *
 * @author Tommy
 */
public class ExperimentData implements Cloneable {
	
	/** The experiment name. */
	private String expName; 
	
	/** The service call list. */
	private LinkedList<ServiceCall> callList;
	
	/** The injections list. */
	private LinkedList<InjectedElement> injList;
	
	/** The service statistics list. */
	private HashMap<String, ServiceStat> ssList;
	
	/** The timings. */
	private HashMap<String, HashMap<LayerType, LinkedList<Integer>>> timings;
	
	/** The observation list. */
	private LinkedList<Observation> obsList;
	
	/** The snapshot list. */
	private ArrayList<Snapshot> snapList;
	
	/**
	 * Instantiates a new experiment data.
	 *
	 * @param expID the experiment id
	 * @param obsList the observation list
	 * @param callList the service call list
	 * @param injList the injections list
	 * @param ssList the service stats list
	 * @param timings the timings
	 */
	public ExperimentData(String expID, LinkedList<Observation> obsList, LinkedList<ServiceCall> callList, LinkedList<InjectedElement> injList, HashMap<String, ServiceStat> ssList, HashMap<String, HashMap<LayerType, LinkedList<Integer>>> timings){
		this(expID, obsList, new ArrayList<Snapshot>(), callList, injList, ssList, timings);
		snapList = buildSnapshots(obsList);
	}
	
	/**
	 * Instantiates a new experiment data without stats.
	 *
	 * @param expID the experiment id
	 * @param obsList the observation list
	 * @param injList the injections list
	 * @param timings the timings
	 */
	public ExperimentData(String expID, LinkedList<Observation> obsList, LinkedList<InjectedElement> injList, HashMap<String, HashMap<LayerType, LinkedList<Integer>>> timings){
		this(expID, obsList, null, injList, null, timings);
	}
	
	/**
	 * Instantiates a new experiment data.
	 *
	 * @param expID the experiment id
	 * @param snapList the snapshot list
	 * @param callList the service call list
	 * @param injList the injections list
	 * @param ssList the service stats list
	 * @param timings the timings
	 */
	public ExperimentData(String expID, LinkedList<Observation> obsList, ArrayList<Snapshot> snapList, LinkedList<ServiceCall> callList, LinkedList<InjectedElement> injList, HashMap<String, ServiceStat> ssList, HashMap<String, HashMap<LayerType, LinkedList<Integer>>> timings){
		expName = "exp" + expID;
		this.obsList = obsList;
		this.snapList = snapList;
		this.callList = callList;
		this.injList = injList;
		this.ssList = ssList;
		this.timings = timings;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ExperimentData clone() throws CloneNotSupportedException {
		ExperimentData eData = new ExperimentData(expName, obsList, snapList, callList, injList, ssList, timings);
		return eData;
	}

	/**
	 * Gets the experiment name.
	 *
	 * @return the name
	 */
	public String getName() {
		return expName;
	}

	/**
	 * Builds the snapshots of the experiment depending on the observations.
	 */
	private ArrayList<Snapshot> buildSnapshots(LinkedList<Observation> obsList) {
		int injIndex = 0;
		LinkedList<ServiceCall> currentCalls;
		InjectedElement currentInj;
		ArrayList<Snapshot> builtSnap = new ArrayList<Snapshot>();
		for(Observation obs : obsList){
			currentCalls = new LinkedList<ServiceCall>();
			if(callList != null) {
				for(ServiceCall call : callList){
					if(call.isAliveAt(obs.getTimestamp()))
						currentCalls.add(call);
				}
			}
			while(injList.size() > injIndex && injList.get(injIndex).getTimestamp().before(obs.getTimestamp())){
				injIndex++;
			}
			if(injList.size() > injIndex && injList.get(injIndex).getTimestamp().compareTo(obs.getTimestamp()) == 0)
				currentInj = injList.get(injIndex);
			else currentInj = null;		
			builtSnap.add(new Snapshot(obs.getTimestamp(), currentCalls, currentInj, ssList));
		}
		return builtSnap;
	}

	/**
	 * Returns the snapshot number.
	 *
	 * @return the number of snapshots
	 */
	public int getSnapshotNumber() {
		return snapList.size();
	}	
	
	/**
	 * Gets the service stats.
	 *
	 * @return the service stats
	 */
	public HashMap<String, ServiceStat> getServiceStats(){
		return ssList;
	}

	/**
	 * Gets the injections for this experiment.
	 *
	 * @return the injections
	 */
	public LinkedList<InjectedElement> getInjections() {
		return injList;
	}
	
	/**
	 * Gets the first timestamp.
	 *
	 * @return the first timestamp
	 */
	public Date getFirstTimestamp(){
		return snapList.get(0).getTimestamp();
	}

	/**
	 * Gets the monitor performance indexes.
	 *
	 * @return the monitor performance indexes
	 */
	public HashMap<String, HashMap<LayerType, LinkedList<Integer>>> getMonitorPerformanceIndexes() {
		return timings;
	}

	public HashMap<LayerType, Integer> getLayerIndicators(){
		HashMap<LayerType, Integer> layerInd = new HashMap<LayerType, Integer>();
		if(snapList.size() > 0){
			for(Indicator ind : obsList.get(0).getIndicators()){
				if(layerInd.get(ind.getLayer()) == null)
					layerInd.put(ind.getLayer(), 0);
				layerInd.replace(ind.getLayer(), layerInd.get(ind.getLayer())+1);
			}
		}
		return layerInd;
	}
	
	/**
	 * Gets the indicators.
	 *
	 * @return the indicators
	 */
	public Indicator[] getIndicators() {
		return obsList.get(0).getIndicators();
	}
	
	private MultipleSnapshot getMultipleSnapshot(int index, String textItem) {
		LinkedList<DataSeries> sList = new LinkedList<DataSeries>();
		sList.add(DataSeries.fromString(textItem.split(";")[0], true));
		sList.add(DataSeries.fromString(textItem.split(";")[1], true));
		return new MultipleSnapshot(obsList.get(index), callList, snapList.get(index).getInjectedElement(), ssList, sList.toArray(new DataSeries[sList.size()]));
	}
	
	private MultipleSnapshot getMultipleSnapshot(int index, Invariant inv) {
		LinkedList<DataSeries> sList = new LinkedList<DataSeries>();
		if(inv.getFirstMember() instanceof DataSeriesMember)
			sList.add(((DataSeriesMember)inv.getFirstMember()).getDataSeries());
		if(inv.getSecondMember() instanceof DataSeriesMember)
			sList.add(((DataSeriesMember)inv.getSecondMember()).getDataSeries());
		return new MultipleSnapshot(obsList.get(index), callList, snapList.get(index).getInjectedElement(), ssList, sList.toArray(new DataSeries[sList.size()]));
	}

	public DataSeriesSnapshot getDataSeriesSnapshot(DataSeries dataSeries, int index) {
		return new DataSeriesSnapshot(obsList.get(index), callList, snapList.get(index).getInjectedElement(), ssList, dataSeries); 
	}

	public Snapshot getSnapshot(int index) {
		return snapList.get(index);
	}
	
	public LinkedList<Snapshot> buildSnapshotsFor(AlgorithmType algType, DataSeries dataSeries, AlgorithmConfiguration conf){
		LinkedList<Snapshot> outList = new LinkedList<Snapshot>();
		for(int i=0;i<getSnapshotNumber();i++){
			outList.add(buildSnapshotFor(algType, i, dataSeries, conf));
		}
		return outList;
	}
	
	public Snapshot buildSnapshotFor(AlgorithmType algType, int index, DataSeries dataSeries, AlgorithmConfiguration conf){
		Invariant inv;
		switch(algType){
			case RCC:
				return getSnapshot(index);
			case INV:
				inv = (Invariant)conf.getRawItem(AlgorithmConfiguration.INVARIANT);
				return getMultipleSnapshot(index, inv);
			case PEA:
				return getMultipleSnapshot(index, conf.getItem(AlgorithmConfiguration.PEARSON_DETAIL));
			default:
				return getDataSeriesSnapshot(dataSeries, index);
		}
	}
	
	

	public double[] getDataSeriesValue(DataSeries ds){
		double[] outList = new double[obsList.size()];
		for(int i=0;i<obsList.size();i++){
			outList[i] = ds.getSeriesValue(obsList.get(i));
		}
		return outList;
	}
	
}
