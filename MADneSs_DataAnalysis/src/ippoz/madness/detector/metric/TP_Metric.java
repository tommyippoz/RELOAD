/**
 * 
 */
package ippoz.madness.detector.metric;

import ippoz.reload.commons.failure.InjectedElement;

import java.util.Date;
import java.util.List;

/**
 * The Class TP_Metric.
 * Implements a metric based on true positives.
 *
 * @author Tommy
 */
public class TP_Metric extends ClassificationMetric {

	/**
	 * Instantiates a new tp_ metric.
	 *
	 * @param absolute the absolute flag
	 */
	public TP_Metric(boolean absolute, boolean validAfter) {
		super(MetricType.TP, absolute, validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "True Positives";
	}
	
	@Override
	public String getMetricShortName() {
		return "TP";
	}

	@Override
	protected int classifyMetric(Date snapTime, Double anEvaluation, List<InjectedElement> injList) {
		int count = 0;
		if(!injList.isEmpty() && Metric.anomalyTrueFalse(anEvaluation)){
			count = injList.size();
			injList.clear();
		}
		return count;
	}

}
