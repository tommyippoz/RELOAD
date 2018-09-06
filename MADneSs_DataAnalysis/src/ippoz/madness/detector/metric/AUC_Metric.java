/**
 * 
 */
package ippoz.madness.detector.metric;

import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.support.TimedValue;

import java.util.List;

/**
 * @author Tommy
 *
 */
public class AUC_Metric extends BetterMaxMetric {
	
	private AlgorithmConfiguration conf; 

	public AUC_Metric(boolean validAfter) {
		super(null, validAfter);
		// TODO Auto-generated constructor stub
	}
	
	public void setConf(AlgorithmConfiguration conf){
		this.conf = conf;
	}

	@Override
	public double evaluateAnomalyResults(Knowledge knowledge, List<TimedValue> anomalyEvaluations) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMetricName() {
		return "AUC";
	}

}
