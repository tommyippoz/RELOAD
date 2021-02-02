/**
 * 
 */
package ippoz.reload.commons.loader.info;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Tommy
 *
 */
public class FeatureInfo {
	
	private Map<Double, Integer> normalMap;
	
	private Map<Double, Integer> anomalyMap;
	
	public FeatureInfo(){
		normalMap = new TreeMap<>();
		anomalyMap = new TreeMap<>();
	}
	
	public Map<Double, Integer> getNormalMap(){
		return normalMap;
	}
	
	public Map<Double, Integer> getAnomalyMap(){
		return anomalyMap;
	}
	
	public Map<Double, Integer> getAllMap(){
		Map<Double, Integer> map = new TreeMap<>(normalMap);
		map.putAll(anomalyMap);
		return map;
	}
	
	public Map<Double, Integer> getAllMapFinite(){
		Map<Double, Integer> map = getAllMap();
		if(map.containsKey(Double.NaN))
			map.remove(Double.NaN);
		if(map.containsKey(Double.POSITIVE_INFINITY))
			map.remove(Double.POSITIVE_INFINITY);
		if(map.containsKey(Double.NEGATIVE_INFINITY))
			map.remove(Double.NEGATIVE_INFINITY);
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
	
	public double getMaxFinite(){
		return getMax(getAllMapFinite());
	}
	
	public double getMinFinite(){
		return getMin(getAllMapFinite());
	}
	
	public double getMax(){
		return getMax(getAllMap());
	}
	
	public double getMin(){
		return getMin(getAllMap());
	}
	
	private double getMax(Map<Double, Integer> map){
		return Collections.max(map.keySet());
	}
	
	private double getMin(Map<Double, Integer> map){
		return Collections.min(map.keySet());
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

	public int countInfinite() {
		int count = 0;
		if(normalMap.containsKey(Double.NaN))
			count = count + normalMap.get(Double.NaN);
		if(anomalyMap.containsKey(Double.NaN))
			count = count + anomalyMap.get(Double.NaN);
		if(normalMap.containsKey(Double.NEGATIVE_INFINITY))
			count = count + normalMap.get(Double.NEGATIVE_INFINITY);
		if(anomalyMap.containsKey(Double.NEGATIVE_INFINITY))
			count = count + anomalyMap.get(Double.NEGATIVE_INFINITY);
		if(normalMap.containsKey(Double.POSITIVE_INFINITY))
			count = count + normalMap.get(Double.POSITIVE_INFINITY);
		if(anomalyMap.containsKey(Double.POSITIVE_INFINITY))
			count = count + anomalyMap.get(Double.POSITIVE_INFINITY);
		return count;
	}
	
}
