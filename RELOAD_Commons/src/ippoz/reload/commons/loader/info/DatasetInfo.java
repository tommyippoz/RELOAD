/**
 * 
 */
package ippoz.reload.commons.loader.info;

import ippoz.reload.commons.support.AppLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class DatasetInfo {
	
	private Map<String, FeatureInfo> featureStats;
	
	private double anomalyRatio;
	
	private double skipRatio;
	
	private int dataPoints;
	
	public DatasetInfo(){
		this(null);
	}
	
	public DatasetInfo(Map<String, Boolean> map){
		featureStats = new HashMap<>();
		if(map != null){
			for(String key : map.keySet()){
				if(map.get(key))
					featureStats.put(key, new FeatureInfo());
			}
		}
		anomalyRatio = Double.NaN;
		skipRatio = Double.NaN;
		dataPoints = 0;
	}
	
	public FeatureInfo getInfoFor(String fName){
		if(featureStats.containsKey(fName))
			return featureStats.get(fName);
		else return new FeatureInfo();
	}
	
	public void addFeature(String fName){
		featureStats.put(fName, new FeatureInfo());
	}
	
	public void addValue(String fName, Double value, boolean label){
		if(featureStats.containsKey(fName)){
			if(label)
				featureStats.get(fName).addAnomaly(value);
			else featureStats.get(fName).addNormal(value); 
		} else AppLogger.logError(getClass(), "NoFeatureFound", "Unable to add value to feature '" + fName + "'");
	}
	
	public double getAnomalyRatio() {
		return anomalyRatio;
	}

	public void setAnomalyRatio(double anomalyRatio) {
		this.anomalyRatio = anomalyRatio;
	}

	public int getDataPoints() {
		return dataPoints;
	}

	public void setDataPoints(int dataPoints) {
		this.dataPoints = dataPoints;
	}

	public double getSkipRatio() {
		return skipRatio;
	}

	public void setSkipRatio(double skipRatio) {
		this.skipRatio = skipRatio;
	}

	public List<String> getFeatures() {
		if(this.featureStats != null)
			return new ArrayList<>(featureStats.keySet());
		else return new ArrayList<String>();
	}

}
