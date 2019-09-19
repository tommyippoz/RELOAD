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
public class FractionDataSeries extends ComplexDataSeries {

	public FractionDataSeries(DataSeries firstOperand, DataSeries secondOperand, DataCategory dataCategory) {
		super(firstOperand, secondOperand, "/", dataCategory);
	}

	@Override
	protected SnapshotValue composePlain(Observation obs) {
		if(secondOperand.getSeriesValue(obs).getFirst() != 0)
			return new SnapshotValue(firstOperand.getSeriesValue(obs).getFirst() / secondOperand.getSeriesValue(obs).getFirst());
		else return new SnapshotValue(Double.NaN);	
	}

	@Override
	protected SnapshotValue composeDiff(Observation obs) {
		double p1 = firstOperand.getPlainSeriesValue(obs).getFirst();
		double p2 = secondOperand.getPlainSeriesValue(obs).getFirst();
		double d1 = firstOperand.getDiffSeriesValue(obs).getFirst();
		double d2 = secondOperand.getDiffSeriesValue(obs).getFirst();
		if(p2 != 0 && (p2-d2) != 0)
			return new SnapshotValue(p1/p2 - (p1-d1)/(p2-d2));
		else return new SnapshotValue(Double.NaN);
	}

	@Override
	protected StatPair composeStat(StatPair s1stat, StatPair s2stat) {
		if(s2stat.getAvg() != 0)
			return new StatPair(s1stat.getAvg() / s2stat.getAvg(), s1stat.getStd() * s2stat.getStd());
		else return new StatPair(Double.NaN, s1stat.getStd() * s2stat.getStd());
	}

}

