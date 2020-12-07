/**
 * 
 */
package ippoz.reload.algorithm.meta;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.meta.MetaLearnerType;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Tommy
 *
 */
public class FullStackingMetaLearner extends StackingMetaLearner {

	public FullStackingMetaLearner(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, MetaLearnerType.STACKING_FULL);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected DataSeries getStackingDataSeries() {
		List<DataSeries> sList = new LinkedList<>();
		sList.add(super.getStackingDataSeries());
		sList.add(getDataSeries());
		return new DataSeries(sList);
	}

	@Override
	protected double[] getMetaArray(double[] meta, double[] snap) {
		return ArrayUtils.addAll(meta, snap);
	}
	
	
	
	

}
