/**
 * 
 */
package ippoz.madness.detector.commons.support;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class ValueSeries {
	
	private List<Double> values;
	
	public ValueSeries(){
		values = new LinkedList<Double>();
	}
	
	public void addValue(double newValue){
		values.add(newValue);
	}
	
	public double getMin(){
		if(values != null && values.size() > 0)
			return Collections.min(values);
		else return Double.NaN;
	}
	
	public double getMax(){
		if(values != null && values.size() > 0)
			return Collections.max(values);
		else return Double.NaN;
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

        Collections.sort(values);

        int n = (int) Math.round(values.size() * lowerPercent / 100);
        
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
	}

	public double getMedian() {
		return quartile(0.5);
	}	

}
