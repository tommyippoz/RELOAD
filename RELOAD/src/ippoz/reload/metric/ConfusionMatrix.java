/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.metric.result.DoubleMetricResult;
import ippoz.reload.metric.result.MetricResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class ConfusionMatrix {
	
	private Map<MetricType, MetricResult> matrix;
	
	private int listSize;

	public ConfusionMatrix(List<AlgorithmResult> list) {
		matrix = new HashMap<>();
		if(list != null && !list.isEmpty()){
			calculateMatrix(list);
			listSize = list.size();
		}
	}
	
	private void calculateMatrix(List<AlgorithmResult> list) {
		int tp = 0, tn = 0, fp = 0, fn = 0;
		for (int i = 0; i < list.size(); i++) {
			AlgorithmResult tResult = list.get(i);
			if (tResult.isAnomalous()){
				if(tResult.getBooleanScore())
					tp++;
				else fn++;
			} else {
				if(tResult.getBooleanScore())
					fp++;
				else tn++;
			}
		}
		matrix.put(MetricType.TP, new DoubleMetricResult(tp));
		matrix.put(MetricType.TN, new DoubleMetricResult(tn));
		matrix.put(MetricType.FP, new DoubleMetricResult(fp));
		matrix.put(MetricType.FN, new DoubleMetricResult(fn));
	}

	public boolean hasMetric(MetricType metricType) {
		return matrix != null && matrix.containsKey(metricType);
	}

	public MetricResult getValueFor(MetricType metricType, boolean absolute) {
		if(hasMetric(metricType)){
			if(absolute)
				return matrix.get(metricType);
			else return new DoubleMetricResult(matrix.get(metricType).getDoubleValue()/listSize*100.0);
		} else return null;
	}

}
