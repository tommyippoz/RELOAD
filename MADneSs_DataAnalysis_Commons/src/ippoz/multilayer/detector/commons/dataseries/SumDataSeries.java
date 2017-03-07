/**
 * 
 */
package ippoz.multilayer.detector.commons.dataseries;

import ippoz.multilayer.commons.datacategory.DataCategory;
import ippoz.multilayer.detector.commons.data.Observation;
import ippoz.multilayer.detector.commons.service.StatPair;

/**
 * @author Tommy
 *
 */
public class SumDataSeries extends ComplexDataSeries {

	public SumDataSeries(DataSeries firstOperand, DataSeries secondOperand, DataCategory dataCategory) {
		super(firstOperand, secondOperand, "+", dataCategory);
	}

	@Override
	protected Double composePlain(Observation obs) {
		return firstOperand.getSeriesValue(obs) + secondOperand.getSeriesValue(obs);
	}

	@Override
	protected Double composeDiff(Observation obs) {
		double d1 = firstOperand.getDiffSeriesValue(obs);
		double d2 = secondOperand.getDiffSeriesValue(obs);
		return d1 + d2;
	}

	@Override
	protected StatPair composeStat(StatPair s1stat, StatPair s2stat) {
		return new StatPair(s1stat.getAvg() + s2stat.getAvg(), s1stat.getStd() + s2stat.getStd());
	}

}
