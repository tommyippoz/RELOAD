/**
 * 
 */
package ippoz.madness.detector.algorithm.elki;

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
import ippoz.madness.detector.decisionfunction.DecisionFunction;

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
	
	private static final String THRESHOLD = "threshold";
	
	private static final String TMP_FILE = "tmp_file";
	
	private boolean outliersInTraining;
	
	private ELKIAlgorithm<?> customELKI;
	
	private List<Double> scoresList;
	
	public DataSeriesELKIAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf, boolean outliersInTraining, boolean needNormalization) {
		super(dataSeries, conf, needNormalization);
		this.outliersInTraining = outliersInTraining;
		customELKI = generateELKIAlgorithm();
		if(conf.hasItem(TMP_FILE)){
			customELKI.loadFile(conf.getItem(TMP_FILE));
			clearLoggedScores();
			logScores(customELKI.getScoresList());
			scoresList = customELKI.getScoresList();
		}
	}
	
	protected abstract void storeAdditionalPreferences();

	protected ELKIAlgorithm<?> getAlgorithm(){
		return customELKI;
	}

	protected abstract ELKIAlgorithm<?> generateELKIAlgorithm();

	@Override
	public void automaticTraining(List<Knowledge> kList, boolean createOutput) {
		Database db = translateKnowledge(kList, outliersInTraining);
		if(db != null)
			automaticElkiTraining(db, createOutput);
		else AppLogger.logError(getClass(), "WrongDatabaseError", "Database must contain at least 1 valid instances");
	}
	
	@Override
	protected DecisionFunction buildClassifier() {
		if(conf != null && conf.hasItem(THRESHOLD))
			return DecisionFunction.getClassifier(scoresList, conf.getItem(THRESHOLD));
		else return null;
	}
	
	protected void automaticElkiTraining(Database db, boolean createOutput){
		customELKI.run(db, db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
		
		clearLoggedScores();
		scoresList = customELKI.getScoresList();
		logScores(scoresList);
		
		conf.addItem(TMP_FILE, getFilename());
	    
	    if(createOutput){
	    	if(!new File(getDefaultTmpFolder()).exists())
	    		new File(getDefaultTmpFolder()).mkdirs();
	    	customELKI.printFile(new File(getFilename()));
	    }
	    
	    storeAdditionalPreferences();
	}
	
	private String getDefaultTmpFolder(){
		return customELKI.getAlgorithmName() + "_tmp_RELOAD";
	}
	
	private String getFilename(){
		return getDefaultTmpFolder() + File.separatorChar + getDataSeries().getCompactString().replace("\\", "_").replace("/", "_") + "." + customELKI.getAlgorithmName();
	}

	@Override
	protected AnomalyResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		return evaluateElkiSnapshot(sysSnapshot);
	}
	
	protected abstract AnomalyResult evaluateElkiSnapshot(Snapshot sysSnapshot);
	
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
