/**
 * 
 */
package ippoz.reload.algorithm.weka;

import ippoz.reload.algorithm.DataSeriesExternalSlidingAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.externalutils.WEKAUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javafx.util.Pair;
import weka.core.Instance;
import weka.core.Instances;

/**
 * The Class DataSeriesSlidingWEKAAlgorithm. Wrapper to DataSeriesAlgorithm to embed WEKA algorithms.
 *
 * @author Tommy
 */
public abstract class DataSeriesSlidingWEKAAlgorithm extends DataSeriesExternalSlidingAlgorithm {
	
	/**
	 * Instantiates a new data series sliding weka algorithm.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 * @param needNormalization the need for normalization of data
	 */
	public DataSeriesSlidingWEKAAlgorithm(DataSeries dataSeries, BasicConfiguration conf, boolean needNormalization) {
		super(dataSeries, conf, needNormalization);
	}
	
	/**
	 * Converts a Snapshot to a WEKA Instance object.
	 *
	 * @param snap the snapshot
	 * @return the WEKA instance
	 */
	protected Instance snapshotToInstance(Snapshot snap){
		String st = "";
		Instances iList;
		try {
			if(getDataSeries().size() == 1){
				if(needNormalization)
					st = st + (((DataSeriesSnapshot)snap).getSnapValue().getFirst() - minmax[0][0])/(minmax[0][1] - minmax[0][0]) + ",";
				else st = st + ((DataSeriesSnapshot)snap).getSnapValue().getFirst() + ",";
			} else {
				for(int j=0;j<getDataSeries().size();j++){
					if(needNormalization)
						st = st + (((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst() - minmax[j][0])/(minmax[j][1] - minmax[j][0]) + ",";
					else st = st + ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst() + ",";						
				}
			}
			st = WEKAUtils.getStreamHeader(getDataSeries(), true) + st + "no";
			iList = new Instances(new StringReader(st));
			iList.setClassIndex(getDataSeries().size());
			if(iList != null && iList.size() > 0)
				return iList.instance(0);
			else return null;
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Error while converting snapshot to WEKA instance");
			return null;
		}
	}

	@Override
	protected Pair<Double, Object> evaluateSlidingSnapshot(SlidingKnowledge sKnowledge, List<Snapshot> snapList, Snapshot dsSnapshot) {
		return evaluateSlidingWEKASnapshot(sKnowledge, translateSnapList(snapList, true, getDataSeries()), snapshotToInstance(dsSnapshot), dsSnapshot); 
	}
	
	/**
	 * Translates snapshot list to Instances object.
	 *
	 * @param snapList the snapshot list
	 * @param includeFaulty the flag to include&exclude faulty data points in the training set.
	 * @return the instances
	 */
	protected Instances translateSnapList(List<Snapshot> snapList, boolean includeFaulty, DataSeries ds) {
		double[][] dataMatrix = convertSnapshotListIntoMatrix(snapList, includeFaulty);
		String[] label = extractLabels(includeFaulty, snapList);
		if(dataMatrix.length > 0)
			return WEKAUtils.createWEKADatabase(dataMatrix, label, ds);
		else return null;
	}

	/**
	 * Evaluates a sliding snapshot using a WEKA algorithm.
	 *
	 * @param sKnowledge the s knowledge
	 * @param windowInstances the window instances
	 * @param newInstance the new instance
	 * @param dsSnapshot the ds snapshot
	 * @return the algorithm result
	 */
	protected abstract Pair<Double, Object> evaluateSlidingWEKASnapshot(SlidingKnowledge sKnowledge, Instances windowInstances, Instance newInstance, Snapshot dsSnapshot);

}
