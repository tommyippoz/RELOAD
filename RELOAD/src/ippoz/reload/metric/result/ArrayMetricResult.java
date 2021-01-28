/**
 * 
 */
package ippoz.reload.metric.result;

import java.util.Arrays;

/**
 * @author Tommy
 *
 */
public class ArrayMetricResult extends MetricResult {

	public ArrayMetricResult(double[] result) {
		super(result);
	}

	@Override
	public double getDoubleValue() {
		if(result != null && result instanceof double[]){
			double[] r = (double[])result;
			if(r != null && r.length > 0)
				return r[0];
		}
		return Double.NaN;
	}

	@Override
	public String toString() {
		return Arrays.toString((double[])result).replace(",", ";");
	}

}
