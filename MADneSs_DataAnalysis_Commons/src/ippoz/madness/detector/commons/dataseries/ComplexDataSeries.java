/**
 * 
 */
package ippoz.madness.detector.commons.dataseries;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.commons.layers.LayerType;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.knowledge.data.Observation;
import ippoz.madness.detector.commons.knowledge.snapshot.SnapshotValue;
import ippoz.madness.detector.commons.service.ServiceCall;
import ippoz.madness.detector.commons.service.ServiceStat;
import ippoz.madness.detector.commons.service.StatPair;

import java.util.Date;

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
	public boolean compliesWith(AlgorithmType algType) {
		return !algType.equals(AlgorithmType.INV);
	}

	@Override
	protected SnapshotValue getPlainSeriesValue(Observation obs) {
		return composePlain(obs);
	}

	@Override
	protected SnapshotValue getDiffSeriesValue(Observation obs) {
		return composeDiff(obs);
	}

	@Override
	public StatPair getSeriesServiceStat(Date timestamp, ServiceCall sCall, ServiceStat sStat) {
		return composeStat(firstOperand.getSeriesServiceStat(timestamp, sCall, sStat), secondOperand.getSeriesServiceStat(timestamp, sCall, sStat));
	}
	
	@Override
	public String toCompactString() {
		return toString();
	}

	protected abstract SnapshotValue composePlain(Observation obs);
	
	protected abstract SnapshotValue composeDiff(Observation obs);
	
	protected abstract StatPair composeStat(StatPair s1stat, StatPair s2stat);
	
}
