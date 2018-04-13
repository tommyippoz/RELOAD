/**
 * 
 */
package ippoz.madness.detector.algorithm.elki;

import ippoz.madness.detector.algorithm.AutomaticTrainingAlgorithm;
import ippoz.madness.detector.algorithm.DataSeriesDetectionAlgorithm;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.dataseries.MultipleDataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;

import java.util.List;

import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public abstract class DataSeriesElkiAlgorithm extends DataSeriesDetectionAlgorithm implements AutomaticTrainingAlgorithm {

	private static final String MINMAX = "MINMAX";
	
	private static int MAX_RANGE = 100;
	
	private boolean outliersInTraining;
	
	private boolean needNormalization;
	
	private double[][] minmax;
	
	public DataSeriesElkiAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf, boolean outliersInTraining, boolean needNormalization) {
		super(dataSeries, conf);
		this.outliersInTraining = outliersInTraining;
		this.needNormalization = needNormalization;
		if(conf.hasItem(MINMAX))
			loadMinMax(conf.getItem(MINMAX));
	}

	private void loadMinMax(String item) {
		int i = 0;
		if(item != null && item.trim().length() > 0){
			if(item.contains(";")){
				minmax = new double[item.split(";").length][2];
				for(String splitted : item.split(";")){
					minmax[i][0] = Double.valueOf(splitted.trim().split(",")[0]);
					minmax[i][1] = Double.valueOf(splitted.trim().split(",")[1]);
					i++;
				}
			} else if(item.contains(",")){
				minmax = new double[1][2];
				minmax[0][0] = Double.valueOf(item.trim().split(",")[0]);
				minmax[0][1] = Double.valueOf(item.trim().split(",")[1]);
			} else minmax = null;
		} else minmax = null;
	}

	@Override
	public void automaticTraining(List<Knowledge> kList) {
		automaticElkiTraining(translateKnowledge(kList, outliersInTraining));
	}
	
	protected abstract void automaticElkiTraining(Database db);

	@Override
	protected double evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		return evaluateElkiSnapshot(sysSnapshot);
	}
	
	protected abstract double evaluateElkiSnapshot(Snapshot sysSnapshot);

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}
	
	private Database translateKnowledge(List<Knowledge> kList, boolean includeFaulty){
		return createElkiDatabase(convertKnowledgeIntoMatrix(kList, includeFaulty));
	}
	
	private Database createElkiDatabase(double[][] data){
		DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
	    Database db = new StaticArrayDatabase(dbc, null);
	    db.initialize();  
	    return db;
	}
	
	private double[][] convertKnowledgeIntoMatrix(List<Knowledge> kList, boolean includeFaulty) {
		List<Snapshot> kSnapList = null;
		for(Knowledge knowledge : kList){
			if(kSnapList == null)
				kSnapList = knowledge.toArray(getAlgorithmType(), getDataSeries());
			else kSnapList.addAll(knowledge.toArray(getAlgorithmType(), getDataSeries()));
		}
		return convertSnapshotListIntoMatrix(kSnapList, includeFaulty);
	}
	
	private double[][] convertSnapshotListIntoMatrix(List<Snapshot> kSnapList, boolean includeFaulty) {
		int insertIndex = 0;
		double[][] dataMatrix;
		minmax = new double[getDataSeries().size()][2];
		if(includeFaulty)
			dataMatrix = new double[kSnapList.size()][getDataSeries().size()];
		else dataMatrix = new double[Knowledge.goldenPointsSize(kSnapList)][getDataSeries().size()]; 
		for(int i=0;i<kSnapList.size();i++){
			if(includeFaulty || !includeFaulty && kSnapList.get(i).getInjectedElement() == null) {
				if(getDataSeries().size() == 1){
					dataMatrix[insertIndex][0] = ((DataSeriesSnapshot)kSnapList.get(i)).getSnapValue().getFirst();
					if(insertIndex == 0){
						minmax[0][0] = dataMatrix[insertIndex][0];
						minmax[0][1] = dataMatrix[insertIndex][0];
					} else {
						if(dataMatrix[insertIndex][0] < minmax[0][0])
							minmax[0][0] = dataMatrix[insertIndex][0];
						if(dataMatrix[insertIndex][0] > minmax[0][1])
							minmax[0][1] = dataMatrix[insertIndex][0];
					}
				} else {
					for(int j=0;j<getDataSeries().size();j++){
						dataMatrix[insertIndex][j] = ((MultipleSnapshot)kSnapList.get(i)).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
						if(insertIndex == 0){
							minmax[j][0] = dataMatrix[insertIndex][0];
							minmax[j][1] = dataMatrix[insertIndex][0];
						} else {
							if(dataMatrix[insertIndex][0] < minmax[j][0])
								minmax[j][0] = dataMatrix[insertIndex][0];
							if(dataMatrix[insertIndex][0] > minmax[j][1])
								minmax[j][1] = dataMatrix[insertIndex][0];
						}
					}
				}
				insertIndex++;
			}
		}
		if(needNormalization){
			for(int j=0;j<getDataSeries().size();j++){
				if(minmax[j][1] - minmax[j][0] >= MAX_RANGE){
					for(int i=0;i<dataMatrix.length;i++){
						dataMatrix[i][j] = (dataMatrix[i][j] - minmax[j][0])/(minmax[j][1] - minmax[j][0]);
					}
				}
			}
			conf.addItem(MINMAX, minmaxToString());
		}
		return dataMatrix;
	}
	
	private String minmaxToString() {
		String mm = "";
		for(int i=0;i<minmax.length;i++){
			mm = mm + minmax[i][0] + "," + minmax[i][1] + ";";
		}
		return mm.substring(0, mm.length()-1);
	}

	protected Vector convertSnapToVector(Snapshot sysSnapshot) {
		Vector vec = new Vector(getDataSeries().size());
		if(getDataSeries().size() == 1){
			if(needNormalization)
				vec.set(0, (((DataSeriesSnapshot)sysSnapshot).getSnapValue().getFirst() - minmax[0][0])/(minmax[0][1] - minmax[0][0]));
			else vec.set(0, ((DataSeriesSnapshot)sysSnapshot).getSnapValue().getFirst());
		} else {
			for(int j=0;j<getDataSeries().size();j++){
				if(needNormalization)
					vec.set(0, (((MultipleSnapshot)sysSnapshot).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst() - minmax[j][0])/(minmax[j][1] - minmax[j][0]));
				else vec.set(j, ((MultipleSnapshot)sysSnapshot).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst());						
			}
		}
		return vec;
	}

}
