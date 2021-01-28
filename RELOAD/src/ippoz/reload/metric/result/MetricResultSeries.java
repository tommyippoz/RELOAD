/**
 * 
 */
package ippoz.reload.metric.result;

import ippoz.reload.commons.support.AppUtility;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class MetricResultSeries {
	
	private List<MetricResult> allValues;
	
	private List<MetricResult> values;
	
	private Map<MetricResult, Integer> frequencies;
	
	private boolean sorted;
	
	public MetricResultSeries(){
		values = new LinkedList<>();
		allValues = new LinkedList<>();
		frequencies = new HashMap<>();
		sorted = true;
	}
	
	public MetricResultSeries(List<MetricResult> scoresList) {
		values = new LinkedList<>();
		allValues = new LinkedList<>();
		frequencies = new HashMap<>();
		for(MetricResult d : scoresList){
			addValue(d);
		}
		Collections.sort(values);
		sorted = true;
	}

	public void removeFirst(){
		if(!values.isEmpty())
			values.remove(0);
	}
	
	public void addValue(MetricResult newValue){
		allValues.add(newValue);
		if(frequencies.containsKey(newValue)){
			frequencies.put(newValue, frequencies.get(newValue) + 1);
		} else frequencies.put(newValue, 1);
		if(newValue != null && Double.isFinite(newValue.getDoubleValue())){
			values.add(newValue);
			sorted = false;
		}
	}
	
	public MetricResult getMin(){
		return quartile(0);
	}
	
	public MetricResult getMax(){
		return quartile(1);
	}
	
	public MetricResult getQ1(){
		return quartile(0.25);
	}
	
	public MetricResult getQ3(){
		return quartile(0.75);
	}
	
	private MetricResult quartile(double lowerPercent) {
        if (values == null || values.size() == 0) {
            return new DoubleMetricResult(0.0);
        }
        if(!sorted){
        	Collections.sort(values);
        	sorted = true;
        }
        int n = (int) Math.round((values.size()-1) * lowerPercent); 
        return values.get(n);
    }
	
	public double getAvg(){
		return AppUtility.calcAvg(getDoubleArray(values));
	}
	
	public double getStd(){
		return AppUtility.calcStd(getDoubleArray(values), AppUtility.calcAvg(getDoubleArray(values)));
	}

	public void clear() {
		values.clear();
		sorted = true;
	}

	public MetricResult getMedian() {
		return quartile(0.5);
	}

	public int size() {
		return values.size();
	}

	public MetricResult get(int i) {
		if(!sorted){
			Collections.sort(values);
			sorted = true;
		}
		return values.get(i);
	}	
	
	public List<MetricResult> getValues(){
		return values;
	}
	
	public MetricResult getMode() {
		MetricResult toReturn = null;
		int maxOcc = 0;
		if(frequencies != null){
			for(MetricResult d : frequencies.keySet()){
				if(frequencies.get(d) > maxOcc){
					maxOcc = frequencies.get(d);
					toReturn = d;
				}
			}
		}
		return toReturn;
	}
	
	public static List<Double> getDoubleArray(List<MetricResult> mrList){
		List<Double> list = new LinkedList<>();
		for(MetricResult mr : mrList){
			if(mr != null)
				list.add(mr.getDoubleValue());
		}
		return list;
	}

	/*public MetricResult getMinimumNonZero() {
		double min = Math.abs(getMin());
		if(min > 0)
			return min;
		else {
			min = Double.MAX_VALUE;
			for(Double v : values){
				if(Math.abs(v) > 0 && Math.abs(v) < min){
					min = Math.abs(v);
				}
			}
			return min;
		}
	}*/

}