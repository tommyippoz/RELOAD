/**
 * 
 */
package ippoz.reload.algorithm.weka;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.weka.support.CustomIsolationForest;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.decisionfunction.StaticThresholdGreaterThanDecision;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import weka.core.Instances;

/**
 * The Class IsolationForestWEKA. Imports Isolation Forest algorithm from WEKA.
 *
 * @author Tommy
 */
public class IsolationForestWEKA extends DataSeriesWEKAAlgorithm {
	
	/** The Constant N_TREES. */
	private static final String N_TREES = "n_trees";
	
	/** The Constant SAMPLE_SIZE. */
	private static final String SAMPLE_SIZE = "sample_size";
	
	/** The Constant TMP_FILE. */
	private static final String TMP_FILE = "tmp_file";
	
	/** The Constant DEFAULT_TMP_FOLDER. */
	public static final String DEFAULT_TMP_FOLDER = "iforest_tmp_RELOAD";
	
	/** The isolation forest object. */
	private CustomIsolationForest iForest;

	/**
	 * Instantiates a new isolation forest from WEKA.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public IsolationForestWEKA(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, true, false);
		if(conf.hasItem(TMP_FILE)){
			iForest = loadSerialized(conf.getItem(TMP_FILE));
			iForest.loadScores(new File(conf.getItem(TMP_FILE) + "scores"));
			clearLoggedScores();
			logScores(iForest.getScores());
		}
	}

	/**
	 * Loads an isolation forest object that was previously serialized in a file.
	 *
	 * @param filename the name of the file in which isolation forest was serialized.
	 * @return the isolation forest read
	 */
	private CustomIsolationForest loadSerialized(String filename) {
		FileInputStream file;
		ObjectInputStream in;
		CustomIsolationForest isf = null;
		try {
			if(new File(filename).exists()){
				file = new FileInputStream(filename);
	            in = new ObjectInputStream(file);
	            synchronized(CustomIsolationForest.class){
	            	isf = (CustomIsolationForest)in.readObject();
	            }
				in.close();
	            file.close();
			} else AppLogger.logError(getClass(), "SerializeError", "Unable to Deserialize: missing '" + filename + "' file");
		} catch (IOException | ClassNotFoundException ex) {
			AppLogger.logException(getClass(), ex, "Error while deserializing Isolation Forest");
		}
		return isf;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.weka.DataSeriesWEKAAlgorithm#automaticWEKATraining(weka.core.Instances, boolean)
	 */
	@Override
	protected boolean automaticWEKATraining(Instances db, boolean createOutput) {
		int nTrees;
		int sampleSize;
		try {
			nTrees = loadNTrees();
			sampleSize = loadSampleSize();
			iForest = new CustomIsolationForest(nTrees, sampleSize);
			iForest.buildClassifier(db);
			
			clearLoggedScores();
			logScores(iForest.getScores());
			
			conf.addItem(TMP_FILE, getFilename());
			if(createOutput){
		    	if(!new File(DEFAULT_TMP_FOLDER).exists())
		    		new File(DEFAULT_TMP_FOLDER).mkdirs();
		    	storeSerialized();
		    	iForest.printScores(new File(getFilename() + "scores"));
		    }
			return true;
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to train IsolationForest");
			return false;
		}
	}
	
	/**
	 * Loads the size of the sample to be used in each isolation tree.
	 *
	 * @return the int
	 */
	private int loadSampleSize() {
		if(conf.hasItem(SAMPLE_SIZE) && AppUtility.isInteger(conf.getItem(SAMPLE_SIZE)))
			return Integer.parseInt(conf.getItem(SAMPLE_SIZE));
		else return -1;
	}

	/**
	 * Loads the number of isolation trees composing the forest.
	 *
	 * @return the number of trees
	 */
	private int loadNTrees() {
		if(conf.hasItem(N_TREES) && AppUtility.isInteger(conf.getItem(N_TREES)))
			return Integer.parseInt(conf.getItem(N_TREES));
		else return -1;
	}

	/**
	 * Stores the isolation forest object in a file, serializing it.
	 */
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

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.weka.DataSeriesWEKAAlgorithm#evaluateWEKASnapshot(ippoz.reload.commons.knowledge.snapshot.Snapshot)
	 */
	@Override
	protected AlgorithmResult evaluateWEKASnapshot(Snapshot sysSnapshot) {
		AlgorithmResult ar;
		try {
			if(iForest != null){
				ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), iForest.classifyInstance(snapshotToInstance(sysSnapshot)));
				getDecisionFunction().assignScore(ar, true);
				return ar;
			} else return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to score IsolationForest");
			return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
		}
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DetectionAlgorithm#buildClassifier()
	 */
	@Override
	protected DecisionFunction buildClassifier() {
		return new StaticThresholdGreaterThanDecision(0.5);
	}

}
