/**
 * 
 */
package ippoz.reload.algorithm.weka;

import ippoz.reload.algorithm.AutomaticTrainingAlgorithm;
import ippoz.reload.algorithm.DataSeriesExternalAlgorithm;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.externalutils.WEKAUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import weka.core.Instance;
import weka.core.Instances;

/**
 * The Class DataSeriesWEKAAlgorithm. Embeds a non-sliding WEKA algorithm. 
 *
 * @author Tommy
 */
public abstract class DataSeriesWEKAAlgorithm extends DataSeriesExternalAlgorithm implements AutomaticTrainingAlgorithm {

	/** Flag to specify if faulty items should be used for training. */
	private boolean outliersInTraining;
	
	/**
	 * Instantiates a new data series WEKA algorithm.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 * @param outliersInTraining the flag for faulty items in training
	 * @param needNormalization the flag that defines need for normalization
	 */
	public DataSeriesWEKAAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf, boolean outliersInTraining, boolean needNormalization) {
		super(dataSeries, conf, needNormalization);
		this.outliersInTraining = outliersInTraining;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.AutomaticTrainingAlgorithm#automaticTraining(java.util.List, boolean)
	 */
	@Override
	public boolean automaticTraining(List<Knowledge> kList, boolean createOutput) {
		Instances db = translateKnowledge(kList);
		if(db != null)
			return automaticWEKATraining(db, createOutput);
		else {
			AppLogger.logError(getClass(), "WrongDatabaseError", "Database must contain at least 1 valid instances");
			return false;
		}
	}
	
	/**
	 * Translates the knowledge list to a WEKA Instances object.
	 *
	 * @param kList the knowledge list
	 * @return the Instances WEKA object
	 */
	private Instances translateKnowledge(List<Knowledge> kList) {
		double[][] dataMatrix = convertKnowledgeIntoMatrix(kList, outliersInTraining);
		String[] label = extractLabels(kList, outliersInTraining);
		if(dataMatrix.length > 0)
			return WEKAUtils.createWEKADatabase(dataMatrix, label, getDataSeries());
		else return null;
	}
	
	/**
	 * Converts a Snapshot to a WEKA Instance.
	 *
	 * @param snap the snapshot
	 * @return the instance
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

	/**
	 * ABstract method for Automatic WEKA training.
	 *
	 * @param db the instances object resembling the DataBase
	 * @param createOutput the create output flag
	 * @return true, if training is successful
	 */
	protected abstract boolean automaticWEKATraining(Instances db, boolean createOutput);

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DataSeriesDetectionAlgorithm#evaluateDataSeriesSnapshot(ippoz.reload.commons.knowledge.Knowledge, ippoz.reload.commons.knowledge.snapshot.Snapshot, int)
	 */
	@Override
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		return evaluateWEKASnapshot(sysSnapshot);
	}
	
	/**
	 * Evaluates a WEKA snapshot.
	 *
	 * @param sysSnapshot the sys snapshot
	 * @return the algorithm result
	 */
	protected abstract AlgorithmResult evaluateWEKASnapshot(Snapshot sysSnapshot);

}
