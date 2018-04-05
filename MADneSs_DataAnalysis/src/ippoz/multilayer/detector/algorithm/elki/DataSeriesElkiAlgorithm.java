/**
 * 
 */
package ippoz.multilayer.detector.algorithm.elki;

import java.util.List;

import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import ippoz.multilayer.detector.algorithm.AutomaticTrainingAlgorithm;
import ippoz.multilayer.detector.algorithm.DataSeriesDetectionAlgorithm;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.dataseries.MultipleDataSeries;
import ippoz.multilayer.detector.commons.knowledge.Knowledge;
import ippoz.multilayer.detector.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.multilayer.detector.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.multilayer.detector.commons.knowledge.snapshot.Snapshot;

/**
 * @author Tommy
 *
 */
public abstract class DataSeriesElkiAlgorithm extends DataSeriesDetectionAlgorithm implements AutomaticTrainingAlgorithm {

	public DataSeriesElkiAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void automaticTraining(List<Knowledge> kList) {
		automaticElkiTraining(translateKnowledge(kList, false));
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
	
	private Database translateKnowledge(Knowledge knowledge, boolean includeFaulty){
		return createElkiDatabase(convertKnowledgeIntoMatrix(knowledge, includeFaulty));
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

	private double[][] convertKnowledgeIntoMatrix(Knowledge knowledge, boolean includeFaulty) {
		List<Snapshot> kSnapList = knowledge.toArray(getAlgorithmType(), getDataSeries());
		return convertSnapshotListIntoMatrix(kSnapList, includeFaulty);
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
		if(includeFaulty)
			dataMatrix = new double[kSnapList.size()][getDataSeries().size()];
		else dataMatrix = new double[Knowledge.goldenPointsSize(kSnapList)][getDataSeries().size()]; 
		for(int i=0;i<kSnapList.size();i++){
			if(includeFaulty || !includeFaulty && kSnapList.get(i).getInjectedElement() == null) {
				if(getDataSeries().size() == 1){
					dataMatrix[insertIndex][0] =  ((DataSeriesSnapshot)kSnapList.get(i)).getSnapValue().getFirst();
				} else {
					for(int j=0;j<getDataSeries().size();j++){
						dataMatrix[insertIndex][j] = ((MultipleSnapshot)kSnapList.get(i)).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();		
					}
				}
				insertIndex++;
			}
		}
		return dataMatrix;
	}
	
	protected Vector convertSnapToVector(Snapshot sysSnapshot) {
		Vector vec = new Vector(getDataSeries().size());
		//System.out.println(getDataSeries().toString());
		if(getDataSeries().size() == 1){
			vec.set(0, ((DataSeriesSnapshot)sysSnapshot).getSnapValue().getFirst());
			//System.out.println(((DataSeriesSnapshot)sysSnapshot).getSnapValue().getFirst() + " - " + ((DataSeriesSnapshot)sysSnapshot).getSnapValue().getLast());
		} else {
			MultipleSnapshot ms = (MultipleSnapshot)sysSnapshot;
			for(int j=0;j<getDataSeries().size();j++){
				vec.set(j, ((MultipleSnapshot)sysSnapshot).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst());						
			}
		}
		return vec;
	}

}
