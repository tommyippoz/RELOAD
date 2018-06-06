/**
 * 
 */
package ippoz.madness.detector.algorithm.elki;

import ippoz.madness.detector.algorithm.elki.support.CustomABOD;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.support.AppUtility;

import java.io.File;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.HellingerDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class ABODELKI extends DataSeriesELKIAlgorithm {
	
	private static final String THRESHOLD = "threshold";
	
	private static final String TMP_FILE = "tmp_file";
	
	public static final String DEFAULT_TMP_FOLDER = "abod_tmp";
	
	private CustomABOD<NumberVector> abod;
	
	private double threshold;
	
	public ABODELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
		threshold = parseThreshold(conf);
		abod = new CustomABOD<NumberVector>(HellingerDistanceFunction.STATIC);
		if(conf.hasItem(TMP_FILE)){
			abod.loadFile(conf.getItem(TMP_FILE));
		}
	}

	private double parseThreshold(AlgorithmConfiguration conf) {
		if(conf != null && conf.hasItem(THRESHOLD)){
			if(AppUtility.isNumber(conf.getItem(THRESHOLD)))
				return Double.parseDouble(conf.getItem(THRESHOLD));
			else return -1;
		} else return -1;
	}

	@Override
	protected void automaticElkiTraining(Database db, boolean createOutput) {

		abod.run(db, db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
		
		conf.addItem(TMP_FILE, getFilename());
	    
	    if(createOutput){
	    	if(!new File(DEFAULT_TMP_FOLDER).exists())
	    		new File(DEFAULT_TMP_FOLDER).mkdirs();
	    	abod.printFile(new File(getFilename()));
	    }
	}
	
	private String getFilename(){
		return DEFAULT_TMP_FOLDER + File.separatorChar + getDataSeries().toCompactString().replace("\\", "_").replace("/", "_") + ".abod";
	}

	@Override
	protected double evaluateElkiSnapshot(Snapshot sysSnapshot) {
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0))){
			if(abod.rankSingleABOF(v) <= threshold*abod.getDbSize())
				return 1.0;
			else return 0.0;
		} else return 0.0;
	}

}

