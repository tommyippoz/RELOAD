/**
 * 
 */
package ippoz.madness.detector.metric;

import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.support.TimedValue;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class Matthews_Coefficient extends BetterMaxMetric {

	public Matthews_Coefficient(boolean validAfter) {
		super(validAfter);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#evaluateAnomalyResults(ippoz.multilayer.detector.data.ExperimentData, java.util.HashMap)
	 */
	@Override
	public double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		double tp = new TP_Metric(true, isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double fp = new FP_Metric(true, isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double tn = new TN_Metric(true, isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double fn = new FN_Metric(true, isValidAfter()).evaluateAnomalyResults(knowledge, anomalyEvaluations);
		double num = tp*tn - fp*fn;
		if(num != 0.0)
			return 1.0*num/Math.sqrt((tp+fp)*(tp+fn)*(tn+fp)*(tn+fn));
		else return 0.0;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.metric.Metric#getMetricName()
	 */
	@Override
	public String getMetricName() {
		return "MatthewsCorrelationCoefficient";
	}

}
