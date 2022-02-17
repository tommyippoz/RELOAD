/**
 * 
 */
package ippoz.reload.algorithm.weka;

import ippoz.reload.algorithm.ExternalDetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.externalutils.WEKAUtils;

import java.io.File;
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
public abstract class DataSeriesWEKAAlgorithm extends ExternalDetectionAlgorithm {

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
	public DataSeriesWEKAAlgorithm(DataSeries dataSeries, BasicConfiguration conf, boolean outliersInTraining, boolean needNormalization) {
		super(dataSeries, conf, needNormalization);
		this.outliersInTraining = outliersInTraining;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.AutomaticTrainingAlgorithm#automaticTraining(java.util.List, boolean)
	 */
	@Override
	public boolean automaticInnerTraining(List<Knowledge> kList) {
		Instances db = translateKnowledge(kList);
		if(db != null) {
			return automaticWEKATraining(db);
		} else {
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
	protected Instance snapshotToInstance(double[] snapValues){
		String st = "";
		Instances iList;
		try {
			if(getDataSeries().size() == 1){
				if(needNormalization)
					st = st + (snapValues[0] - minmax[0][0])/(minmax[0][1] - minmax[0][0]) + ",";
				else st = st + snapValues[0] + ",";
			} else {
				for(int j=0;j<getDataSeries().size();j++){
					if(needNormalization)
						st = st + (snapValues[j] - minmax[j][0])/(minmax[j][1] - minmax[j][0]) + ",";
					else st = st + snapValues[j] + ",";						
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
	 * @return true, if training is successful
	 */
	protected abstract boolean automaticWEKATraining(Instances db);
	
	@Override
	public ObjectPair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		try {
			return calculateWEKAScore(snapArray);
		} catch (Exception e) {
			AppLogger.logException(getClass(), e, "Unabl to calculate WEKA score for " + snapArray);
			return new ObjectPair<Double, Object>(Double.NaN, null);
		}
	}

	/**
	 * Evaluates a WEKA snapshot.
	 *
	 * @param sysSnapshot the sys snapshot
	 * @return the algorithm result
	 * @throws Exception 
	 */
	protected abstract ObjectPair<Double, Object> calculateWEKAScore(double[] snapArray) throws Exception;
	
	/**
	 * Gets the filename used to store data about scores and histograms.
	 *
	 * @return the filename
	 */
	protected String getFilename(){
		String folder = getDefaultTmpFolder() + File.separatorChar;
		if(!new File(folder).exists())
			new File(folder).mkdirs();
		return folder + getDataSeries().getCompactString().replace("\\", "_").replace("/", "-").replace("*", "_") + "." + getLearnerType().toString().toLowerCase();
	}
	
	/**
	 * Gets the default folder used to store temporary data.
	 *
	 * @return the default temporary folder
	 */
	protected String getDefaultTmpFolder(){
		if(conf.hasItem(BasicConfiguration.DATASET_NAME) && conf.getItem(BasicConfiguration.DATASET_NAME).length() > 0)
			return "tmp" + File.separatorChar + conf.getItem(BasicConfiguration.DATASET_NAME) + File.separatorChar + getLearnerType().toString();
		else return "tmp" + File.separatorChar + getLearnerType().toString();
	}

}
