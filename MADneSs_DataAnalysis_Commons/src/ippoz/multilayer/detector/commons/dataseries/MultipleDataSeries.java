/**
 * 
 */
package ippoz.multilayer.detector.commons.dataseries;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.commons.layers.LayerType;
import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.data.Observation;
import ippoz.multilayer.detector.commons.data.SnapshotValue;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.ServiceStat;
import ippoz.multilayer.detector.commons.service.StatPair;

import java.util.Date;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class MultipleDataSeries extends DataSeries {
	
	private LinkedList<DataSeries> dsList;

	public MultipleDataSeries(IndicatorDataSeries[] ids) {
		super(aggregateSeriesName(ids), DataCategory.PLAIN);
		dsList = new LinkedList<DataSeries>();
		for(IndicatorDataSeries is : ids){
			dsList.add(is);
		}
	}
	
	public MultipleDataSeries(DataSeries firstDS, DataSeries secondDS) {
		super(aggregateSeriesName(firstDS, secondDS), DataCategory.PLAIN);
		dsList = new LinkedList<DataSeries>();
		dsList.add(firstDS);
		dsList.add(secondDS);
	}
	
	private static String aggregateSeriesName(DataSeries firstDS, DataSeries secondDS) {
		return firstDS.toString() + "@" + secondDS.toString();
	}

	private static String aggregateSeriesName(IndicatorDataSeries[] ids){
		if(ids != null && ids.length >= 2)
			return aggregateSeriesName(ids[0], ids[1]);
		else return null;
	}

	public void add(IndicatorDataSeries is){
		dsList.add(is);
	}

	@Override
	public LayerType getLayerType() {
		return LayerType.COMPOSITION;
	}
	
	@Override
	public boolean compliesWith(AlgorithmType algType) {
		return algType.equals(AlgorithmType.INV);
	}

	@Override
	protected SnapshotValue getPlainSeriesValue(Observation obs) {
		return null;
	}

	@Override
	protected SnapshotValue getDiffSeriesValue(Observation obs) {
		return null;
	}

	@Override
	public StatPair getSeriesServiceStat(Date timestamp, ServiceCall sCall, ServiceStat sStat) {
		return null;
	}

	public DataSeries[] getSeriesList() {
		return dsList.toArray(new DataSeries[dsList.size()]);
	}
	
	public LinkedList<DataSeries> getSeriesLinkedList() {
		return dsList;
	}

	public String getSeriesString() {
		String string = "";
		for(DataSeries ds : dsList){
			string = string + ds.toString() + ";";
		}
		return string.substring(0, string.length()-1);
	}

}
