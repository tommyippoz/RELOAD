/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.failure.InjectedElement;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class ClassificationMetric extends BetterMaxMetric {

	/** The absolute flag. */
	private boolean absolute;

	/**
	 * Instantiates a new binary classification metric.
	 *
	 * @param absolute
	 *            the absolute flag
	 * @param absolute
	 *            the validAfter flag
	 */
	public ClassificationMetric(MetricType mType, boolean absolute,
			boolean validAfter) {
		super(mType, validAfter);
		this.absolute = absolute;
	}
	
	
	
	@Override
	public double evaluateAnomalyResults(List<? extends AlgorithmResult> anomalyEvaluations) {
		int detectionHits = 0;
		List<InjectedElement> overallInj = new LinkedList<InjectedElement>();
		for (int i = 0; i < anomalyEvaluations.size(); i++) {
			if (anomalyEvaluations.get(i).getInjection() != null) {
				overallInj.add(anomalyEvaluations.get(i).getInjection());
			}
			//System.out.println(i + "," + anomalyEvaluations.get(i).getValue() + "," + (currentInj.size() > 0 ? 1 : 0));
			int d = classifyMetric(anomalyEvaluations.get(i));
			detectionHits = detectionHits + d;
		}
		if (anomalyEvaluations.size() > 0) {
			if (!absolute){
				// getUndetectable?
				return 1.0 * detectionHits / anomalyEvaluations.size();
			} else return detectionHits;
		} else return 0.0;
	}




	/*@Override
	public double evaluateAnomalyResults(List<TimedResult> anomalyEvaluations) {
		int detectionHits = 0;
		int undetectableCount = 0;
		List<InjectedElement> overallInj = new LinkedList<InjectedElement>();
		List<InjectedElement> currentInj = new LinkedList<InjectedElement>();
		for (int i = 0; i < anomalyEvaluations.size(); i++) {
			while (!currentInj.isEmpty() && currentInj.get(0).getFinalTimestamp().before(anomalyEvaluations.get(i).getDate())) {
				currentInj.remove(0);
			}
			if (anomalyEvaluations.get(i).getInjectedElement() != null) {
				overallInj.add(anomalyEvaluations.get(i).getInjectedElement());
				currentInj.add(anomalyEvaluations.get(i).getInjectedElement());
			}
			//System.out.println(i + "," + anomalyEvaluations.get(i).getValue() + "," + (currentInj.size() > 0 ? 1 : 0));
			int d = classifyMetric(anomalyEvaluations.get(i).getDate(), anomalyEvaluations.get(i).getValue(), currentInj);
			if (anomalyEvaluations.get(i).getValue() >= 0.0) {
				detectionHits = detectionHits + d;
			} else undetectableCount++;
		}
		if (anomalyEvaluations.size() > 0) {
			if (!absolute)
				// getUndetectable?
				return 1.0 * detectionHits / (anomalyEvaluations.size() - undetectableCount);
			else
				return detectionHits;
		} else
			return 0.0;
	}

	private int getUndetectable(List<InjectedElement> injList) {
		int undetectable = 0;
		List<InjectedElement> current;
		while (!injList.isEmpty()) {
			current = new LinkedList<InjectedElement>();
			current.add(injList.remove(0));
			while (!injList.isEmpty()
					&& current.get(current.size() - 1).compliesWith(
							injList.get(0))) {
				current.add(injList.remove(0));
			}
			undetectable = undetectable
					+ ((int) (current.get(current.size() - 1)
							.getFinalTimestamp().getTime() - current.get(0)
							.getTimestamp().getTime()) / 1000 - current.size());
		}
		return undetectable;
	}*/

	protected abstract int classifyMetric(AlgorithmResult tResult);

}
