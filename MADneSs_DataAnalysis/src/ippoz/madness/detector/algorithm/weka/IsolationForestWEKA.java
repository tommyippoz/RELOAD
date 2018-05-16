/**
 * 
 */
package ippoz.madness.detector.algorithm.weka;

import ippoz.madness.detector.algorithm.weka.support.CustomIsolationForest;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Tommy
 *
 */
public class IsolationForestWEKA extends DataSeriesWEKAAlgorithm {
	
	private static final String N_TREES = "n_trees";
	
	private static final String SAMPLE_SIZE = "sample_size";
	
	private static final String TMP_FILE = "tmp_file";
	
	public static final String DEFAULT_TMP_FOLDER = "iforest_tmp";
	
	private CustomIsolationForest iForest;

	public IsolationForestWEKA(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, true, false);
		if(conf.hasItem(TMP_FILE)){
			iForest = loadSerialized(conf.getItem(TMP_FILE));
		}
	}

	private CustomIsolationForest loadSerialized(String item) {
		FileInputStream file;
		ObjectInputStream in;
		CustomIsolationForest isf = null;
		try {
			if(new File(item).exists()){
				file = new FileInputStream(item);
	            in = new ObjectInputStream(file);
	            isf = (CustomIsolationForest)in.readObject();
				in.close();
	            file.close();
			} else AppLogger.logError(getClass(), "SerializeError", "Unable to Serialize: missing '" + item + "' file");
		} catch (IOException | ClassNotFoundException ex) {
			AppLogger.logException(getClass(), ex, "Error while deserializing Isolation Forest");
		}
		return isf;
	}

	@Override
	protected void automaticWEKATraining(Instances db) {
		int nTrees;
		int sampleSize;
		try {
			nTrees = loadNTrees();
			sampleSize = loadSampleSize();
			iForest = new CustomIsolationForest(nTrees, sampleSize);
			iForest.buildClassifier(db);
			conf.addItem(TMP_FILE, getFilename());
			if(!new File(getFilename()).exists()){
		    	if(!new File(DEFAULT_TMP_FOLDER).exists())
		    		new File(DEFAULT_TMP_FOLDER).mkdirs();
		    	storeSerialized();
		    }
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to train IsolationForest");
		}
	}
	
	private int loadSampleSize() {
		if(conf.hasItem(SAMPLE_SIZE) && AppUtility.isInteger(conf.getItem(SAMPLE_SIZE)))
			return Integer.parseInt(conf.getItem(SAMPLE_SIZE));
		else return -1;
	}

	private int loadNTrees() {
		if(conf.hasItem(N_TREES) && AppUtility.isInteger(conf.getItem(N_TREES)))
			return Integer.parseInt(conf.getItem(N_TREES));
		else return -1;
	}

	private void storeSerialized() {
		FileOutputStream file;
		ObjectOutputStream out;
		try {
			if(iForest != null){
				file = new FileOutputStream(getFilename());
	            out = new ObjectOutputStream(file);
	            out.writeObject(iForest);
				out.close();
	            file.close();
			} else AppLogger.logError(getClass(), "SerializeError", "Unable to Serialize: null Isolation Forest");
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Error while serializing Isolation Forest");
		}
	}

	private String getFilename(){
		return DEFAULT_TMP_FOLDER + File.separatorChar + getDataSeries().toCompactString().replace("\\", "_").replace("/", "_") + ".iforest";
	}

	@Override
	protected double evaluateWEKASnapshot(Snapshot sysSnapshot) {
		Instance inst;
		try {
			if(iForest != null){
				inst = snapshotToInstance(sysSnapshot);
				return iForest.classifyInstance(inst);
			} else return 0.0;
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to score IsolationForest");
			return 0.0;
		}
	}

}
