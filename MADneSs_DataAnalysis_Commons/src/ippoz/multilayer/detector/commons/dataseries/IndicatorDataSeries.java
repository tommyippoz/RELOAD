/**
 * 
 */
package ippoz.multilayer.detector.commons.dataseries;

import java.util.Date;

import ippoz.multilayer.commons.datacategory.DataCategory;
import ippoz.multilayer.commons.indicator.Indicator;
import ippoz.multilayer.commons.layers.LayerType;
import ippoz.multilayer.detector.commons.data.Observation;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;
import ippoz.multilayer.detector.commons.service.StatPair;

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
	public StatPair getSeriesServiceStat(Date timestamp, ServiceCall sCall, ServiceStat sStat) {
		return DataSeries.getPairByTime(timestamp, sCall, sStat.getIndStat(indicator.getName()));
	}

	@Override
	protected Double getPlainSeriesValue(Observation obs) {
		return Double.valueOf(obs.getValue(indicator.getName(), DataCategory.PLAIN));
	}

	@Override
	protected Double getDiffSeriesValue(Observation obs) {
		return Double.valueOf(obs.getValue(indicator.getName(), DataCategory.DIFFERENCE));
	}

}
