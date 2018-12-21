/**
 * 
 */
package ippoz.madness.detector.algorithm.elki;

import ippoz.madness.detector.algorithm.DataSeriesExternalSlidingAlgorithm;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.dataseries.MultipleDataSeries;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;

import java.util.Arrays;
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
public abstract class DataSeriesSlidingELKIAlgorithm extends DataSeriesExternalSlidingAlgorithm {
	
	public DataSeriesSlidingELKIAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf, boolean needNormalization) {
		super(dataSeries, conf, needNormalization);
	}

	@Override
	protected double evaluateSlidingSnapshot(SlidingKnowledge sKnowledge, List<Snapshot> snapList, Snapshot dsSnapshot) {
		// TODO
		return evaluateSlidingELKISnapshot(sKnowledge, translateSnapList(snapList, true), convertSnapToVector(dsSnapshot)); 
	}

	protected abstract double evaluateSlidingELKISnapshot(SlidingKnowledge sKnowledge, Database windowDb, Vector newInstance);

	private Database translateSnapList(List<Snapshot> kList, boolean includeFaulty){
		double[][] dataMatrix = convertSnapshotListIntoMatrix(kList, includeFaulty);
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
				if(needNormalization)
					vec.set(j, (((MultipleSnapshot)sysSnapshot).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst() - minmax[j][0])/(minmax[j][1] - minmax[j][0]));
				else vec.set(j, ((MultipleSnapshot)sysSnapshot).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst());						
			}
		}
		return vec;
	}

}
