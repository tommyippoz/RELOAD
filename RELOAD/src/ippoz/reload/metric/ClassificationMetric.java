/**
 * 
 */
package ippoz.reload.metric;

import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.TimedValue;

import java.util.Date;
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
	public double evaluateAnomalyResults(Knowledge knowledge,
			List<TimedValue> anomalyEvaluations) {
		int detectionHits = 0;
		int undetectableCount = 0;
		List<InjectedElement> overallInj = new LinkedList<InjectedElement>();
		List<InjectedElement> currentInj = new LinkedList<InjectedElement>();
		for (int i = 0; i < knowledge.size(); i++) {
			while (!currentInj.isEmpty()
					&& currentInj.get(0).getFinalTimestamp()
							.before(knowledge.getTimestamp(i))) {
				currentInj.remove(0);
			}
			if (knowledge.getInjection(i) != null) {
				overallInj.add(knowledge.getInjection(i));
				currentInj.add(knowledge.getInjection(i));
			}
			int d = classifyMetric(knowledge.getTimestamp(i),
					anomalyEvaluations.get(i).getValue(), currentInj);
			if (anomalyEvaluations.get(i).getValue() >= 0.0) {
				detectionHits = detectionHits + d;
			} else
				undetectableCount++;
		}
		if (knowledge.size() > 0) {
			if (!absolute)
				return 1.0
						* detectionHits
						/ (knowledge.size() - getUndetectable(overallInj) - undetectableCount);
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
	}

	protected abstract int classifyMetric(Date snapTime, Double anEvaluation,
			List<InjectedElement> injList);

}