/**
 * 
 */
package ippoz.reload.commons.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class ValueSeries {
	
	private List<Double> allValues;
	
	private List<Double> values;
	
	private Map<Double, Integer> frequencies;
	
	private boolean sorted;
	
	public ValueSeries(){
		values = new LinkedList<Double>();
		allValues = new LinkedList<Double>();
		frequencies = new HashMap<Double, Integer>();
		sorted = true;
	}
	
	public ValueSeries(List<Double> scoresList) {
		values = new LinkedList<Double>();
		allValues = new LinkedList<Double>();
		frequencies = new HashMap<Double, Integer>();
		for(Double d : scoresList){
			addValue(d);
		}
		Collections.sort(values);
		sorted = true;
	}

	public void removeFirst(){
		if(!values.isEmpty())
			values.remove(0);
	}
	
	public void addValue(double newValue){
		allValues.add(newValue);
		if(frequencies.containsKey(newValue)){
			frequencies.put(newValue, frequencies.get(newValue) + 1);
		} else frequencies.put(newValue, 1);
		if(Double.isFinite(newValue)){
			values.add(newValue);
			sorted = false;
		}
	}
	
	public double getMin(){
		return quartile(0);
	}
	
	public double getMax(){
		return quartile(1);
	}
	
	public double getQ1(){
		return quartile(0.25);
	}
	
	public double getQ3(){
		return quartile(0.75);
	}
	
	private double quartile(double lowerPercent) {
        if (values == null || values.size() == 0) {
            return 0;
        }
        if(!sorted){
        	Collections.sort(values);
        	sorted = true;
        }
        int n = (int) Math.round((values.size()-1) * lowerPercent); 
        return values.get(n);
    }
	
	public double getAvg(){
		return AppUtility.calcAvg(values);
	}
	
	public double getStd(){
		return AppUtility.calcStd(values, AppUtility.calcAvg(values));
	}

	public void clear() {
		values.clear();
		sorted = true;
	}

	public double getMedian() {
		return quartile(0.5);
	}

	public int size() {
		return values.size();
	}

	public double get(int i) {
		if(!sorted){
			Collections.sort(values);
			sorted = true;
		}
		return values.get(i);
	}	
	
	public List<Double> getValues(){
		return values;
	}
	
	public double getMode() {
		double toReturn = Double.NaN;
		int maxOcc = 0;
		if(frequencies != null){
			for(Double d : frequencies.keySet()){
				if(frequencies.get(d) > maxOcc){
					maxOcc = frequencies.get(d);
					toReturn = d;
				}
			}
		}
		return toReturn;
	}

	public double getMinimumNonZero() {
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
	}

}
