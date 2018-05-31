/**
 * 
 */
package ippoz.madness.detector.algorithm.elki;

import ippoz.madness.detector.algorithm.elki.support.CustomCOF;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.support.AppUtility;

import java.io.File;

import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class COFELKI extends DataSeriesELKIAlgorithm {
	
	private static final String K = "k";
	
	private static final String THRESHOLD = "threshold";
	
	private static final String TMP_FILE = "tmp_file";
	
	private static final Integer DEFAULT_K = 5;
	
	public static final String DEFAULT_TMP_FOLDER = "cof_tmp";
	
	private CustomCOF cCOF;
	
	private double threshold;
	
	public COFELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
		cCOF = new CustomCOF( 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K,
	    		SquaredEuclideanDistanceFunction.STATIC);
		if(conf.hasItem(TMP_FILE)){
			cCOF.loadFile(conf.getItem(TMP_FILE));
			threshold = parseThreshold(conf);
		}
	}

	private double parseThreshold(AlgorithmConfiguration conf) {
		double ratio;
		if(conf != null && conf.hasItem(THRESHOLD)){
			if(AppUtility.isNumber(conf.getItem(THRESHOLD))){
				ratio = Double.parseDouble(conf.getItem(THRESHOLD));
				if(ratio <= 1)
					ratio = ratio * cCOF.size();
				return cCOF.getScore((int) ratio);
			}
			else return -1;
		} else return cCOF.getScore(cCOF.size()-1);
	}

	@Override
	protected void automaticElkiTraining(Database db, boolean createOutput) {

		cCOF.run(db, db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
		
		threshold = parseThreshold(conf);
		
		conf.addItem(TMP_FILE, getFilename());
	    
	    if(createOutput){
	    	if(!new File(DEFAULT_TMP_FOLDER).exists())
	    		new File(DEFAULT_TMP_FOLDER).mkdirs();
	    	cCOF.printFile(new File(getFilename()));
	    }
	}
	
	private String getFilename(){
		return DEFAULT_TMP_FOLDER + File.separatorChar + getDataSeries().toCompactString().replace("\\", "_").replace("/", "_") + ".cof";
	}

	@Override
	protected double evaluateElkiSnapshot(Snapshot sysSnapshot) {
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0))){
			double of = cCOF.calculateSingleOF(v);
			if(of >= threshold)
				return 1.0;
			else return 0.0;
		} else return 0.0;
	}

}

