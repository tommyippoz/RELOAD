/**
 * 
 */
package ippoz.reload.algorithm.elki;

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

import java.io.File;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public abstract class DataSeriesELKIAlgorithm extends DataSeriesExternalAlgorithm implements AutomaticTrainingAlgorithm {
	
	private static final String TMP_FILE = "tmp_file";
	
	private boolean outliersInTraining;
	
	private ELKIAlgorithm<?> customELKI;
	
	public DataSeriesELKIAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf, boolean outliersInTraining, boolean needNormalization) {
		super(dataSeries, conf, needNormalization);
		this.outliersInTraining = outliersInTraining;
		customELKI = generateELKIAlgorithm();
		if(conf.hasItem(TMP_FILE)){
			customELKI.loadFile(conf.getItem(TMP_FILE));
			clearLoggedScores();
			logScores(customELKI.getScoresList());
		}
	}
	
	protected abstract void storeAdditionalPreferences();

	protected ELKIAlgorithm<?> getAlgorithm(){
		return customELKI;
	}

	protected abstract ELKIAlgorithm<?> generateELKIAlgorithm();

	@Override
	public boolean automaticTraining(List<Knowledge> kList, boolean createOutput) {
		Database db = translateKnowledge(kList, outliersInTraining);
		if(db != null)
			return automaticElkiTraining(db, createOutput);
		else {
			AppLogger.logError(getClass(), "WrongDatabaseError", "Database must contain at least 1 valid instances");
			return false;
		}
	}
	
	protected boolean automaticElkiTraining(Database db, boolean createOutput){
		Object trainOut = customELKI.run(db, db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
		if(trainOut != null){
			clearLoggedScores();
			logScores(customELKI.getScoresList());
			
			conf.addItem(TMP_FILE, getFilename());
		    
		    if(createOutput){
		    	if(!new File(getDefaultTmpFolder()).exists())
		    		new File(getDefaultTmpFolder()).mkdirs();
		    	customELKI.printFile(new File(getFilename()));
		    }
		    
		    storeAdditionalPreferences();
		} else AppLogger.logError(getClass(), "UnvalidDataSeries", "Unable to apply " + getAlgorithmType() + " to dataseries " + getDataSeries().getName());
		return trainOut != null;
	}
	
	private String getDefaultTmpFolder(){
		return "." + File.separatorChar + "tmp" + File.separatorChar + customELKI.getAlgorithmName() + "_tmp_RELOAD";
	}
	
	private String getFilename(){
		return getDefaultTmpFolder() + File.separatorChar + getDataSeries().getCompactString().replace("\\", "_").replace("/", "-").replace("*", "_") + "." + customELKI.getAlgorithmName();
	}

	@Override
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		return evaluateElkiSnapshot(sysSnapshot);
	}
	
	protected abstract AlgorithmResult evaluateElkiSnapshot(Snapshot sysSnapshot);
	
	private Database translateKnowledge(List<Knowledge> kList, boolean includeFaulty){
		double[][] dataMatrix = convertKnowledgeIntoMatrix(kList, includeFaulty);
		if(dataMatrix.length > 0)
			return createElkiDatabase(dataMatrix);
		else return null;
	}
	
	private Database createElkiDatabase(double[][] data){
		DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
	    Database db = new StaticArrayDatabase(dbc, null);
	    db.initialize();  
	    return db;
	}

	protected Vector convertSnapToVector(Snapshot sysSnapshot) {
		Vector vec = new Vector(getDataSeries().size());
		if(getDataSeries().size() == 1){
			if(needNormalization)
				vec.set(0, (((DataSeriesSnapshot)sysSnapshot).getSnapValue().getFirst() - minmax[0][0])/(minmax[0][1] - minmax[0][0]));
			else vec.set(0, ((DataSeriesSnapshot)sysSnapshot).getSnapValue().getFirst());
		} else {
			for(int j=0;j<getDataSeries().size();j++){
				if(((MultipleSnapshot)sysSnapshot).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue() != null){
					if(needNormalization)
						vec.set(j, (((MultipleSnapshot)sysSnapshot).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst() - minmax[j][0])/(minmax[j][1] - minmax[j][0]));
					else vec.set(j, ((MultipleSnapshot)sysSnapshot).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst());	
				} else vec.set(j, 0.0);					
			}
		}
		return vec;
	}

}
