/**
 * 
 */
package ippoz.reload.algorithm;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.ValueSeries;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javafx.util.Pair;

/**
 * @author Tommy
 *
 */
public abstract class DataSeriesNonSlidingAlgorithm extends DataSeriesDetectionAlgorithm implements AutomaticTrainingAlgorithm {
	
	public DataSeriesNonSlidingAlgorithm(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf);
		if(conf != null && conf.hasItem(TMP_FILE)){
			clearLoggedScores();
			loadLoggedScores();
		}
	}

	private void loadLoggedScores() {
		BufferedReader reader;
		String readed;
		try {
			loggedScores = new ValueSeries();		
			loggedAnomalyScores = new ValueSeries();	
			if(new File(getScoresFilename()).exists()){
				reader = new BufferedReader(new FileReader(new File(getScoresFilename())));
				reader.readLine();
				while(reader.ready()){
					readed = reader.readLine();
					if(readed != null && !readed.startsWith("*") && readed.contains(";")){
						readed = readed.trim();
						if(readed.contains(";")){
							boolean flag = Boolean.valueOf(readed.split(";")[1]);
							double score = Double.parseDouble(readed.split(";")[0]);
							if(flag)
								loggedAnomalyScores.addValue(score);
							else loggedScores.addValue(score);
						} else loggedScores.addValue(Double.parseDouble(readed));
					}
				}
				reader.close();
			} else AppLogger.logError(getClass(), "NoLoggedScoresError", "Unable to find logged scores in '" + getScoresFilename() + "'");
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to read logged scores file");
		} 
	}
	
	private void saveLoggedScores() {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(getScoresFilename()));
			writer.write("score;anomaly\n");
			if(loggedScores != null && loggedScores.size() > 0){
				for(Double d : loggedScores.getValues()){
					writer.write(d + ";false\n");
				}
			}
			if(loggedAnomalyScores != null && loggedAnomalyScores.size() > 0){
				for(Double d : loggedAnomalyScores.getValues()){
					writer.write(d + ";true\n");
				}
			}
			writer.close();
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to write logged scores file");
		} 
	}
	
	protected String getScoresFilename(){
		String base = getFilename();
		base = base.substring(0, base.indexOf("."));
		base = base + "_logged_scores." + getLearnerType().toString().toLowerCase();
		return base;
	}

	@Override
	public boolean automaticTraining(List<Knowledge> kList, boolean createOutput) {
		boolean trainOut;
		if(createOutput && !new File(getDefaultTmpFolder()).exists())
			new File(getDefaultTmpFolder()).mkdirs();
		trainOut = automaticInnerTraining(kList, createOutput);
		if(trainOut){
			ValueSeries vs = new ValueSeries(getTrainScores());
			if(vs.size() > 0)
				setDecisionFunction("IQR", vs, false);
			else setDecisionFunction("STATIC_THRESHOLD_GREATERTHAN(1)", vs, false);
			
			clearLoggedScores();
			for(Knowledge know : kList){
				for(int i=0;i<know.size();i++){
					Snapshot snap = know.buildSnapshotFor(i, getDataSeries());
					AlgorithmResult ar = evaluateSnapshot(know, i);
					logScore(ar.getScore(), snap.isAnomalous());
				}
			}
			if(createOutput){
		    	saveLoggedScores();
		    }
			
			conf.addItem(TMP_FILE, getFilename());		    
		    storeAdditionalPreferences();
		    
		} else AppLogger.logError(getClass(), "UnvalidDataSeries", "Unable to apply " + getLearnerType() + " to dataseries " + getDataSeries().getName());
		return trainOut;
	}
	
	@Override
	public Pair<Double, Object> calculateSnapshotScore(Knowledge knowledge, int currentIndex, Snapshot sysSnapshot, double[] snapArray) {
		return calculateSnapshotScore(snapArray);
	}

	public abstract Pair<Double, Object> calculateSnapshotScore(double[] snapArray);

	/**
	 * Stores additional preferences (if any).
	 */
	protected abstract void storeAdditionalPreferences();
	
	public abstract List<Double> getTrainScores();
	
	public abstract boolean automaticInnerTraining(List<Knowledge> kList, boolean createOutput);	

}
