/**
 * 
 */
package ippoz.reload.commons.dataseries;

import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.data.Observation;
import ippoz.reload.commons.knowledge.snapshot.SnapshotValue;
import ippoz.reload.commons.layers.LayerType;
import ippoz.reload.commons.service.ServiceCall;
import ippoz.reload.commons.service.ServiceStat;
import ippoz.reload.commons.service.StatPair;

import java.util.Date;

/**
 * @author Tommy
 *
 */
public class IndicatorDataSeries extends DataSeries {
	
	private Indicator indicator;

	public IndicatorDataSeries(Indicator indicator, DataCategory dataCategory) {
		super(indicator.getName(), dataCategory);
		this.indicator = indicator;
	}

	@Override
	public LayerType getLayerType() {
		return indicator.getLayer();
	}
	
	@Override
	public boolean compliesWith(AlgorithmType algType) {
		return !algType.equals(AlgorithmType.INV);
	}

	@Override
	public StatPair getSeriesServiceStat(Date timestamp, ServiceCall sCall, ServiceStat sStat) {
		return DataSeries.getPairByTime(timestamp, sCall, sStat.getIndStat(indicator.getName()));
	}

	@Override
	protected SnapshotValue getPlainSeriesValue(Observation obs) {
		return new SnapshotValue(Double.valueOf(obs.getValue(indicator.getName(), DataCategory.PLAIN)));
	}

	@Override
	protected SnapshotValue getDiffSeriesValue(Observation obs) {
		return new SnapshotValue(Double.valueOf(obs.getValue(indicator.getName(), DataCategory.DIFFERENCE)));
	}

	@Override
	public String toCompactString() {
		return toString();
	}

}
