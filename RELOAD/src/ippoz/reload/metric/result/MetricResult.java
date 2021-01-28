/**
 * 
 */
package ippoz.reload.metric.result;

import ippoz.reload.commons.support.AppUtility;

/**
 * @author Tommy
 *
 */
public abstract class MetricResult implements Comparable<MetricResult> {
	
	protected Object result;
	
	public MetricResult(Object result){
		this.result = result;
	}
	
	public abstract double getDoubleValue();
	
	public abstract String toString();
	
	@Override
	public int compareTo(MetricResult o) {
		return Double.compare(getDoubleValue(), o.getDoubleValue());
	}

	public static MetricResult valueOf(String item) {
		if(AppUtility.isNumber(item))
			return new DoubleMetricResult(Double.valueOf(item));
		else if (item != null) {
			double[] array = new double[4];
			String[] splitted = item.replace("[", "").replace("]", "").split(";");
			array[0] = Double.valueOf(splitted[0]);
			array[1] = Double.valueOf(splitted[1]);
			array[2] = Double.valueOf(splitted[2]);
			array[3] = Double.valueOf(splitted[3]);
			return new ArrayMetricResult(array);
		} else return null;
	}

}
