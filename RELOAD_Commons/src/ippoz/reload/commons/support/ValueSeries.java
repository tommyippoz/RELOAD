/**
 * 
 */
package ippoz.reload.commons.support;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class ValueSeries {
	
	private List<Double> values;
	
	private boolean sorted;
	
	public ValueSeries(){
		values = new LinkedList<Double>();
		sorted = true;
	}
	
	public ValueSeries(List<Double> scoresList) {
		values = scoresList;
		Collections.sort(values);
		sorted = true;
	}

	public void removeFirst(){
		if(!values.isEmpty())
			values.remove(0);
	}
	
	public void addValue(double newValue){
		values.add(newValue);
		sorted = false;
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

}
