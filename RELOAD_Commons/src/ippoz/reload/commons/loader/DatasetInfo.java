/**
 * 
 */
package ippoz.reload.commons.loader;

import ippoz.reload.commons.support.AppLogger;

import java.util.HashMap;
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



	private class FeatureInfo {
		
		private Map<Double, Integer> normalMap;
		
		private Map<Double, Integer> anomalyMap;
		
		public FeatureInfo(){
			normalMap = new HashMap<>();
			anomalyMap = new HashMap<>();
		}
		
		public Map<Double, Integer> getNormalMap(){
			return normalMap;
		}
		
		public Map<Double, Integer> getAnomalyMap(){
			return anomalyMap;
		}
		
		public Map<Double, Integer> getAllMap(){
			Map<Double, Integer> map = new HashMap<>(normalMap);
			map.putAll(anomalyMap);
			return map;
		}
		
		public void addNormal(Double value){
			addToMap(value, normalMap);
		}
		
		public void addAnomaly(Double value){
			addToMap(value, anomalyMap);
		}
		
		private void addToMap(Double value, Map<Double, Integer> map){
			if(map.containsKey(value)){
				map.replace(value, map.get(value) + 1);
			} else {
				map.put(value, 1);
			}
		}
		
		public double getNormalAverage(){
			return getAverage(normalMap);
		}
		
		public double getNormalStd(){
			return getStd(normalMap);
		}
		
		public double getAnomalyAverage(){
			return getAverage(anomalyMap);
		}
		
		public double getAnomalyStd(){
			return getStd(anomalyMap);
		}
		
		public double getAllAverage(){
			return getAverage(getAllMap());
		}
		
		public double getAllStd(){
			return getStd(getAllMap());
		}
		
		private double getAverage(Map<Double, Integer> map){
			int count = 0;
			double sum = 0.0;
			for(Double value : map.keySet()){
				int nItems = map.get(value);
				sum = sum + value*nItems;
				count = count + nItems;
			}
			if(count > 0)
				return sum / count;
			else return Double.NaN;
		}
		
		private double getStd(Map<Double, Integer> map){
			int count = 0;
			double sum = 0.0;
			double avg = getAverage(map);
			for(Double value : map.keySet()){
				int nItems = map.get(value);
				sum = sum + Math.pow(value - avg, 2)*nItems;
				count = count + nItems;
			}
			if(count > 0)
				return sum / count;
			else return Double.NaN;
		}
		
	}

}
