/**
 * 
 */
package ippoz.reload.commons.dataseries;

import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.knowledge.snapshot.SnapshotValue;
import ippoz.reload.commons.service.StatPair;

/**
 * @author Tommy
 *
 */
public class DiffDataSeries extends ComplexDataSeries {

	public DiffDataSeries(DataSeries firstOperand, DataSeries secondOperand, DataCategory dataCategory) {
		super(firstOperand, secondOperand, "-", dataCategory);
	}

	@Override
	protected SnapshotValue composePlain(Observation obs) {
		return new SnapshotValue(firstOperand.getSeriesValue(obs).getFirst() - secondOperand.getSeriesValue(obs).getFirst());
	}

	@Override
	protected SnapshotValue composeDiff(Observation obs) {
		double d1 = firstOperand.getDiffSeriesValue(obs).getFirst();
		double d2 = secondOperand.getDiffSeriesValue(obs).getFirst();
		return new SnapshotValue(d1 - d2);
	}

	@Override
	protected StatPair composeStat(StatPair s1stat, StatPair s2stat) {
		return new StatPair(s1stat.getAvg() - s2stat.getAvg(), s1stat.getStd() + s2stat.getStd());
	}

}
