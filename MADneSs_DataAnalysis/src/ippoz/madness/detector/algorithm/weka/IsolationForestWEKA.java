/**
 * 
 */
package ippoz.madness.detector.algorithm.weka;

import ippoz.madness.detector.algorithm.result.AlgorithmResult;
import ippoz.madness.detector.algorithm.weka.support.CustomIsolationForest;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.decisionfunction.DecisionFunction;
import ippoz.madness.detector.decisionfunction.StaticThresholdDecision;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import weka.core.Instances;

/**
 * @author Tommy
 *
 */
public class IsolationForestWEKA extends DataSeriesWEKAAlgorithm {
	
	private static final String N_TREES = "n_trees";
	
	private static final String SAMPLE_SIZE = "sample_size";
	
	private static final String TMP_FILE = "tmp_file";
	
	public static final String DEFAULT_TMP_FOLDER = "iforest_tmp_RELOAD";
	
	private CustomIsolationForest iForest;

	public IsolationForestWEKA(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, true, false);
		if(conf.hasItem(TMP_FILE)){
			iForest = loadSerialized(conf.getItem(TMP_FILE));
			iForest.loadScores(new File(conf.getItem(TMP_FILE) + "scores"));
			clearLoggedScores();
			logScores(iForest.getScores());
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
	            synchronized(CustomIsolationForest.class){
	            	isf = (CustomIsolationForest)in.readObject();
	            }
				in.close();
	            file.close();
			} else AppLogger.logError(getClass(), "SerializeError", "Unable to Deserialize: missing '" + item + "' file");
		} catch (IOException | ClassNotFoundException ex) {
			AppLogger.logException(getClass(), ex, "Error while deserializing Isolation Forest");
		}
		return isf;
	}

	@Override
	protected void automaticWEKATraining(Instances db, boolean createOutput) {
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
		return DEFAULT_TMP_FOLDER + File.separatorChar + getDataSeries().getCompactString().replace("\\", "_").replace("/", "_") + ".iforest";
	}

	@Override
	protected AlgorithmResult evaluateWEKASnapshot(Snapshot sysSnapshot) {
		AlgorithmResult ar;
		try {
			if(iForest != null){
				ar = new AlgorithmResult(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement(), iForest.classifyInstance(snapshotToInstance(sysSnapshot)));
				getDecisionFunction().classifyScore(ar);
				return ar;
			} else return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
		} catch (Exception ex) {
			AppLogger.logException(getClass(), ex, "Unable to score IsolationForest");
			return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
		}
	}

	@Override
	protected DecisionFunction buildClassifier() {
		return new StaticThresholdDecision(0.5);
	}

}
