/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.DataSeriesExternalAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.externalutils.ELKIUtils;

import java.io.File;
import java.util.List;

import javafx.util.Pair;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * The Class DataSeriesELKIAlgorithm. Wraps ELKI algorithms to be used in a non-sliding way.
 *
 * @author Tommy
 */
public abstract class DataSeriesELKIAlgorithm extends DataSeriesExternalAlgorithm {

	/** The flag to decide on outliers in training. */
	private boolean outliersInTraining;
	
	/** The ELKI algorithm used. */
	private ELKIAlgorithm<?> customELKI;
	
	/**
	 * Instantiates a new data series ELKI algorithm.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 * @param outliersInTraining the flag to decide on outliers in training
	 * @param needNormalization the flag to define if algorithm needs normalization
	 */
	public DataSeriesELKIAlgorithm(DataSeries dataSeries, BasicConfiguration conf, boolean outliersInTraining, boolean needNormalization) {
		super(dataSeries, conf, needNormalization);
		this.outliersInTraining = outliersInTraining;
		customELKI = generateELKIAlgorithm();
		if(conf.hasItem(TMP_FILE)){
			customELKI.loadFile(conf.getItem(TMP_FILE));
		}
	}

	/**
	 * Gets the ELKI algorithm.
	 *
	 * @return the algorithm
	 */
	protected ELKIAlgorithm<?> getAlgorithm(){
		return customELKI;
	}
	
	

	@Override
	public List<Double> getTrainScores() {
		return customELKI.getScoresList();
	}

	/**
	 * Generates the ELKI algorithm.
	 *
	 * @return the ELKI algorithm
	 */
	protected abstract ELKIAlgorithm<?> generateELKIAlgorithm();

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.AutomaticTrainingAlgorithm#automaticInnerTraining(java.util.List, boolean)
	 */
	@Override
	public boolean automaticInnerTraining(List<Knowledge> kList, boolean createOutput) {
		Database db = translateKnowledge(kList, outliersInTraining);
		if(db != null){
			return automaticElkiTraining(db, kList, createOutput);
		}
		else {
			AppLogger.logError(getClass(), "WrongDatabaseError", "Database must contain at least 1 valid instances");
			return false;
		}
	}
	
	/**
	 * Defines how the automatic training is declined in the ELKI data series.
	 *
	 * @param db the ELKI Database object
	 * @param createOutput the create output flag
	 * @return true, if training is successful
	 */
	protected boolean automaticElkiTraining(Database db, List<Knowledge> kList, boolean createOutput){
		Object trainOut = customELKI.run(db, db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
		if(trainOut != null){		    
		    if(createOutput){
		    	customELKI.printFile(new File(getFilename()));
		    }
		} else AppLogger.logError(getClass(), "UnvalidDataSeries", "Unable to apply " + getLearnerType() + " to dataseries " + getDataSeries().getName());
		return trainOut != null;
	}
	
	@Override
	public Pair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		return getELKIScore(new Vector(snapArray));
	}

	@Override
	protected boolean checkCalculationCondition(double[] snapValue) {
		return getELKIEvaluationFlag(new Vector(snapValue));
	}
	
	/**
	 * Translates the knowledge list into an ELKI Database object.
	 *
	 * @param kList the knowledge list
	 * @param includeFaulty the flag to include faulty data points into training
	 * @return the ELKI Database object
	 */
	private Database translateKnowledge(List<Knowledge> kList, boolean includeFaulty){
		double[][] dataMatrix = convertKnowledgeIntoMatrix(kList, includeFaulty);
		if(dataMatrix.length > 0)
			return ELKIUtils.createElkiDatabase(dataMatrix);
		else return null;
	}
	/**
	 * Converts a snapshot to and ELKI Vector (that extends NumberVector).
	 *
	 * @param sysSnapshot the system snapshot
	 * @return the vector
	 */
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
	
	public abstract Pair<Double, Object> getELKIScore(Vector v);
	
	public boolean getELKIEvaluationFlag(Vector v){
		return v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0)) && getDecisionFunction() != null;
	}

}
