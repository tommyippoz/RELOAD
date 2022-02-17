/**
 * 
 */
package ippoz.reload.commons.knowledge;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.loader.DatasetIndex;

import java.util.Map;

/**
 * The Class Snapshot.
 * Stores data related to a single observation of a target system, enriching it with general information about the system (serviceCalls, injections, serviceStats).
 *
 * @author Tommy
 */
public class Snapshot {
	
	private DataSeries dataSeries;
	
	/** The injection at that time instant. */
	private InjectedElement injEl;
	
	private Map<Indicator, Double> snapValues;
	
	/**
	 * Instantiates a new snapshot.
	 *
	 * @param injEl the injection
	 */
	public Snapshot(Map<Indicator, Double> snapValues, InjectedElement injEl, DataSeries dataSeries) {
		this.dataSeries = dataSeries;
		this.injEl = injEl;
		this.snapValues = snapValues;
	}

	/**
	 * Gets the timestamp of that snapshot.
	 *
	 * @return the timestamp
	 */
	public DatasetIndex getIndex(){
		return injEl.getIndex();
	}
	
	public double[] getDoubleValues() {
		if(dataSeries != null){
			int index = 0;
			double[] valArr = new double[dataSeries.size()];
			for(Indicator ind : dataSeries.getIndicators()){
				valArr[index++] = getDoubleValueFor(ind);
			}
			return valArr;
		} else return null;
	}
	
	/*
	 public double[] getDoubleValues() {
		if(snapValues != null){
			return ArrayUtils.toPrimitive(snapValues.values().toArray(new Double[snapValues.size()]));
		} else return null;
	}
	 */
	
	public Double getValueFor(Indicator indicator) {
		if(indicator != null && snapValues != null){
			if(snapValues.keySet().contains(indicator)){
				return snapValues.get(indicator);
			} else {
				for(Indicator ind : snapValues.keySet()){
					if(indicator.getName().compareTo(ind.getName()) == 0)
						return snapValues.get(ind);
				}
			}
		}
		return null;
	}
	
	public double getDoubleValueFor(Indicator indicator) {
		Double ob = getValueFor(indicator);
		if(ob != null)
			return ob;
		else return 0.0;
	}
	
	/**
	 * Gets the injected element.
	 *
	 * @return the injected element
	 */
	public InjectedElement getInjectedElement() {
		return injEl;
	}

	/**
	 * Converts a snapshot to string.
	 *
	 * @param snap the snapshot
	 * @return the string
	 */
	public String snapToString(){
		String snapValue = "{";
		if(snapValues.size() > 0){
			for(Indicator ind : snapValues.keySet()){
				if(snapValues.get(ind) != null)
					snapValue = snapValue + snapValues.get(ind).toString() + ", ";
				else snapValue = snapValue + "0.0, ";
			}
			snapValue = snapValue.substring(0,  snapValue.length()-2);
		}
		return snapValue + "}";
	}
	
	public boolean isAnomalous(){
		return injEl != null;
	}
	
}
