/**
 * 
 */
package ippoz.multilayer.detector.commons.dataseries;

import java.util.Date;

import ippoz.multilayer.commons.datacategory.DataCategory;
import ippoz.multilayer.commons.layers.LayerType;
import ippoz.multilayer.detector.commons.data.Observation;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;
import ippoz.multilayer.detector.commons.service.StatPair;

/**
 * @author Tommy
 *
 */
public abstract class ComplexDataSeries extends DataSeries {
	
	protected DataSeries firstOperand;
	protected DataSeries secondOperand;

	protected ComplexDataSeries(DataSeries firstOperand, DataSeries secondOperand, String operandTag, DataCategory dataCategory) {
		super("(" + firstOperand.toString() + ")" + operandTag + "(" + secondOperand.toString() + ")", dataCategory);
		this.firstOperand = firstOperand;
		this.secondOperand = secondOperand;
	}

	public DataSeries getFirstOperand() {
		return firstOperand;
	}

	public DataSeries getSecondOperand() {
		return secondOperand;
	}

	@Override
	public LayerType getLayerType() {
		return LayerType.COMPOSITION;
	}
	
	@Override
	protected Double getPlainSeriesValue(Observation obs) {
		return composePlain(obs);
	}

	@Override
	protected Double getDiffSeriesValue(Observation obs) {
		return composeDiff(obs);
	}

	@Override
	public StatPair getSeriesServiceStat(Date timestamp, ServiceCall sCall, ServiceStat sStat) {
		return composeStat(firstOperand.getSeriesServiceStat(timestamp, sCall, sStat), secondOperand.getSeriesServiceStat(timestamp, sCall, sStat));
	}
	
	protected abstract Double composePlain(Observation obs);
	
	protected abstract Double composeDiff(Observation obs);
	
	protected abstract StatPair composeStat(StatPair s1stat, StatPair s2stat);
	
}
