/**
 * 
 */
package ippoz.reload.algorithm.elki;

import ippoz.reload.algorithm.DataSeriesExternalSlidingAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.utils.ObjectPair;
import ippoz.reload.externalutils.ELKIUtils;

import java.util.List;

import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

// TODO: Auto-generated Javadoc
/**
 * The Class DataSeriesSlidingELKIAlgorithm.
 *
 * @author Tommy
 */
public abstract class DataSeriesSlidingELKIAlgorithm extends DataSeriesExternalSlidingAlgorithm {
	
	/** The ELKI algorithm that is executed by this class. */
	private ELKIAlgorithm<?> customELKI;
	
	/**
	 * Instantiates a new sliding window algorithm taken by ELKI.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 * @param needNormalization the flag that indicates the need of data normalization
	 */
	public DataSeriesSlidingELKIAlgorithm(DataSeries dataSeries, BasicConfiguration conf, boolean needNormalization) {
		super(dataSeries, conf, needNormalization);
	}
	
	/**
	 * Gets the ELKI algorithm.
	 *
	 * @return the algorithm
	 */
	protected ELKIAlgorithm<?> getAlgorithm(){
		return customELKI;
	}

	/**
	 * Generates the ELKI algorithm.
	 *
	 * @return the ELKI algorithm
	 */
	protected abstract ELKIAlgorithm<?> generateELKIAlgorithm();

	/* (non-Javadoc)
	 * @see ippoz.reload.algorithm.DataSeriesExternalSlidingAlgorithm#evaluateSlidingSnapshot(ippoz.reload.commons.knowledge.SlidingKnowledge, java.util.List, ippoz.reload.commons.knowledge.snapshot.Snapshot)
	 */
	@Override
	protected ObjectPair<Double, Object> evaluateSlidingSnapshot(SlidingKnowledge sKnowledge, List<Snapshot> snapList, Snapshot dsSnapshot) {
		Database windowDb = translateSnapList(snapList, true);
		if(windowDb.getRelation(TypeUtil.NUMBER_VECTOR_FIELD).getDBIDs().size() >= 5){
			customELKI = generateELKIAlgorithm();
			customELKI.run(windowDb, windowDb.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
			return evaluateSlidingELKISnapshot(sKnowledge, windowDb, convertSnapToVector(dsSnapshot), dsSnapshot);			
		} else return new ObjectPair<Double, Object>(Double.NaN, null);
	}

	/**
	 * Evaluates a snapshot using the ELKI sliding algorithm.
	 *
	 * @param sKnowledge the sliding knowledge
	 * @param windowDb the sliding window database
	 * @param newInstance the new instance to be evaluated using the sliding window
	 * @param dsSnapshot the snapshot to be evaluated
	 * @return the algorithm result
	 */
	protected abstract ObjectPair<Double, Object> evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance, Snapshot dsSnapshot);

	/**
	 * Translates a snapshot list into an ELKI Database object.
	 *
	 * @param kList the knowledge list
	 * @param includeFaulty the flag to include faulty data into training
	 * @return the database object
	 */
	private Database translateSnapList(List<Snapshot> kList, boolean includeFaulty){
		double[][] dataMatrix = convertSnapshotListIntoMatrix(kList, includeFaulty);
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
		double[] arr = sysSnapshot.getDoubleValues();
		for(int j=0;j<getDataSeries().size();j++){
			vec.set(j, arr[j]);					
		}
		return vec;
	}
	
	@Override
	protected boolean checkCalculationCondition(double[] snapArray) {
		Vector v = new Vector(snapArray);
		return v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0)) && getDecisionFunction() != null;
	}

}
