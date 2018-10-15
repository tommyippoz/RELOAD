/**
 * 
 */
package ippoz.madness.detector.metric;

import ippoz.madness.detector.commons.failure.InjectedElement;

import java.util.Date;
import java.util.List;

/**
 * The Class FN_Metric.
 * Implements a metric based on false negatives.
 *
 * @author Tommy
 */
public class FN_Metric extends ClassificationMetric {

	/**
	 * Instantiates a new fn_ metric.
	 *
	 * @param absolute the absolute flag
	 */
	public FN_Metric(boolean absolute, boolean validAfter) {
		super(MetricType.FN, absolute, validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "False Negatives";
	}
	
	@Override
	public String getMetricShortName() {
		return "FN";
	}

	@Override
	protected int classifyMetric(Date snapTime, Double anEvaluation, List<InjectedElement> injList) {
		int count = 0;
		if(!injList.isEmpty()){ 
			if(Metric.anomalyTrueFalse(anEvaluation)){
				injList.clear();
			} else {
				for(InjectedElement ie : injList){
					if(ie.getFinalTimestamp().equals(snapTime))
						count++;
				}
			}
		}
		return count;
	}

}
