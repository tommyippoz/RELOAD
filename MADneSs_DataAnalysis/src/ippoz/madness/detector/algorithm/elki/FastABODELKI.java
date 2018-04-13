/**
 * 
 */
package ippoz.madness.detector.algorithm.elki;

import ippoz.madness.detector.algorithm.elki.support.CustomFastABOD;
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
public class FastABODELKI extends DataSeriesElkiAlgorithm {
	
	private static final String K = "k";
	
	private static final String THRESHOLD = "threshold";
	
	private static final String TMP_FILE = "tmp_file";
	
	private static final Integer DEFAULT_K = 5;
	
	public static final String DEFAULT_TMP_FOLDER = "abod_tmp";
	
	private CustomFastABOD<NumberVector> fAbod;
	
	private double threshold;
	
	public FastABODELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
		threshold = parseThreshold(conf);
		fAbod = new CustomFastABOD<NumberVector>(
				HellingerDistanceFunction.STATIC, 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K);
		if(conf.hasItem(TMP_FILE)){
			fAbod.loadFile(conf.getItem(TMP_FILE));
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
	protected void automaticElkiTraining(Database db) {

		fAbod.run(db, db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
		
		conf.addItem(TMP_FILE, getFilename());
	    
	    if(!new File(getFilename()).exists()){
	    	if(!new File(DEFAULT_TMP_FOLDER).exists())
	    		new File(DEFAULT_TMP_FOLDER).mkdirs();
	    	fAbod.printFile(new File(getFilename()));
	    }
	}
	
	private String getFilename(){
		return DEFAULT_TMP_FOLDER + File.separatorChar + getDataSeries().toCompactString().replace("\\", "_").replace("/", "_") + ".abod";
	}

	@Override
	protected double evaluateElkiSnapshot(Snapshot sysSnapshot) {
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0))){
			if(fAbod.rankSingleABOF(v) >= (threshold*100)*fAbod.getDbSize())
				return 1.0;
			else return 0.0;
		} else return 0.0;
	}

}
