/**
 * 
 */
package ippoz.madness.detector.algorithm.weka;

import ippoz.madness.detector.algorithm.AutomaticTrainingAlgorithm;
import ippoz.madness.detector.algorithm.DataSeriesExternalAlgorithm;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.dataseries.MultipleDataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.decisionfunction.AnomalyResult;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Tommy
 *
 */
public abstract class DataSeriesWEKAAlgorithm extends DataSeriesExternalAlgorithm implements AutomaticTrainingAlgorithm {

	private boolean outliersInTraining;
	
	public DataSeriesWEKAAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf, boolean outliersInTraining, boolean needNormalization) {
		super(dataSeries, conf, needNormalization);
		this.outliersInTraining = outliersInTraining;
	}

	@Override
	public void automaticTraining(List<Knowledge> kList, boolean createOutput) {
		Instances db = translateKnowledge(kList);
		if(db != null)
			automaticWEKATraining(db, createOutput);
		else AppLogger.logError(getClass(), "WrongDatabaseError", "Database must contain at least 1 valid instances");
	}
	
	private Instances translateKnowledge(List<Knowledge> kList) {
		double[][] dataMatrix = convertKnowledgeIntoMatrix(kList, outliersInTraining);
		String[] label = extractLabels(kList, outliersInTraining);
		if(dataMatrix.length > 0)
			return createWEKADatabase(dataMatrix, label);
		else return null;
	}

	private Instances createWEKADatabase(double[][] data, String[] label){ 
		Instances wInst;
		try {
			wInst = new Instances(getTrainARFFReader(data, label));
			wInst.setClassIndex(getDataSeries().size());
			return wInst;
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to create WEKA instances");
			return null;
		}
	}
	
	private Reader getTrainARFFReader(double[][] data, String[] label) {
		String arff = getStreamHeader(true);
		for(int i=0;i<label.length;i++){
			for(int j=0;j<data[i].length;j++){
				arff = arff + data[i][j] + ",";
			}
			arff = arff + label[i] + "\n";
		}
		return new StringReader(arff);
	}
	
	protected String getStreamHeader(boolean training){
		String header = "@relation " + getDataSeries().getCompactString() + "\n\n";
		if(getDataSeries().size() == 1){
			header = header + "@attribute " + getDataSeries().getName() + " numeric\n";
		} else {
			for(DataSeries ds : ((MultipleDataSeries)getDataSeries()).getSeriesList()){
				header = header + "@attribute " + ds.getName() + " numeric\n";
			}
		}
		if(training)
			header = header + "@attribute class {no, yes}\n";
		header = header + "\n@data\n";
		return header;
	}
	
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
			st = getStreamHeader(true) + st + "no";
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

	protected abstract void automaticWEKATraining(Instances db, boolean createOutput);

	@Override
	protected AnomalyResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		return evaluateWEKASnapshot(sysSnapshot);
	}
	
	protected abstract AnomalyResult evaluateWEKASnapshot(Snapshot sysSnapshot);

}
