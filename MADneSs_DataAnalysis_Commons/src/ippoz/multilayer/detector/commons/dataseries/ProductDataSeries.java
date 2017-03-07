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
public class ProductDataSeries extends ComplexDataSeries {

	public ProductDataSeries(DataSeries firstOperand, DataSeries secondOperand, DataCategory dataCategory) {
		super(firstOperand, secondOperand, "*", dataCategory);
	}

	@Override
	protected Double composePlain(Observation obs) {
		return firstOperand.getSeriesValue(obs) * secondOperand.getSeriesValue(obs);
	}

	@Override
	protected Double composeDiff(Observation obs) {
		double p1 = firstOperand.getPlainSeriesValue(obs);
		double p2 = secondOperand.getPlainSeriesValue(obs);
		double d1 = firstOperand.getDiffSeriesValue(obs);
		double d2 = secondOperand.getDiffSeriesValue(obs);
		return p1*p2 - (p1-d1)*(p2-d2);
	}

	@Override
	protected StatPair composeStat(StatPair s1stat, StatPair s2stat) {
		return new StatPair(s1stat.getAvg() * s2stat.getAvg(), s1stat.getStd() * s2stat.getStd());
	}

}

