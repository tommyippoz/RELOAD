/**
 * 
 */
package ippoz.reload.metric.result;

/**
 * @author Tommy
 *
 */
public class DoubleMetricResult extends MetricResult {

	public DoubleMetricResult(Double result) {
		super(result);
	}
	
	public DoubleMetricResult(int result) {
		super(Double.valueOf(result));
	}

	@Override
	public double getDoubleValue() {
		if(result != null && result instanceof Double)
			return ((Double)result).doubleValue();
		else return Double.NaN;
	}

	@Override
	public String toString() {
		return String.valueOf(getDoubleValue());
	}

}
