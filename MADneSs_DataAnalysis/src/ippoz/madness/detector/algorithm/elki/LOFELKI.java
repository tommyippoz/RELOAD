/**
 * 
 */
package ippoz.madness.detector.algorithm.elki;

import ippoz.madness.detector.algorithm.elki.support.CustomLOF;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.scoreclassifier.AnomalyResult;
import ippoz.madness.detector.scoreclassifier.ScoreClassifier;

import java.io.File;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class LOFELKI extends DataSeriesELKIAlgorithm {
	
	private static final String K = "k";
	
	private static final String THRESHOLD = "threshold";
	
	private static final String TMP_FILE = "tmp_file";
	
	private static final Integer DEFAULT_K = 5;
	
	public static final String DEFAULT_TMP_FOLDER = "lof_tmp";
	
	private CustomLOF cLOF;
	
	private List<Double> scoresList;
	
	public LOFELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, false);
		cLOF = new CustomLOF( 
	    		conf.hasItem(K) ? Integer.parseInt(conf.getItem(K)) : DEFAULT_K,
	    		SquaredEuclideanDistanceFunction.STATIC);
		if(conf.hasItem(TMP_FILE)){
			cLOF.loadFile(conf.getItem(TMP_FILE));
			clearLoggedScores();
			for(int i=0;i<cLOF.size();i++){
				logScore(cLOF.getScore(i+1));
			}
			scoresList = cLOF.getScoresList();
		}
	}

	private double parseThreshold(AlgorithmConfiguration conf) {
		double ratio;
		clearLoggedScores();
		for(int i=0;i<cLOF.size();i++){
			logScore(cLOF.getScore(i+1));
		}
		if(conf != null && conf.hasItem(THRESHOLD)){
			if(AppUtility.isNumber(conf.getItem(THRESHOLD))){
				ratio = Double.parseDouble(conf.getItem(THRESHOLD));
				if(ratio <= 1)
					ratio = ratio * cLOF.size();
				return cLOF.getScore((int) ratio);
			}
			else return -1;
		} else return cLOF.getScore(cLOF.size()-1);
	}

	@Override
	protected void automaticElkiTraining(Database db, boolean createOutput) {

		cLOF.run(db, db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
		
		clearLoggedScores();
		for(int i=0;i<cLOF.size();i++){
			logScore(cLOF.getScore(i+1));
		}
		scoresList = cLOF.getScoresList();
		
		conf.addItem(TMP_FILE, getFilename());
	    
	    if(createOutput){
	    	if(!new File(DEFAULT_TMP_FOLDER).exists())
	    		new File(DEFAULT_TMP_FOLDER).mkdirs();
	    	cLOF.printFile(new File(getFilename()));
	    }
	}
	
	private String getFilename(){
		return DEFAULT_TMP_FOLDER + File.separatorChar + getDataSeries().getCompactString().replace("\\", "_").replace("/", "_") + ".abod";
	}

	@Override
	protected AnomalyResult evaluateElkiSnapshot(Snapshot sysSnapshot) {
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0)) && getClassifier() != null){
			double of = cLOF.calculateSingleOF(v);
			return getClassifier().classify(of);
		} else return AnomalyResult.UNKNOWN;
	}

	@Override
	protected ScoreClassifier buildClassifier() {
		if(conf != null && conf.hasItem(THRESHOLD))
			return ScoreClassifier.getClassifier(scoresList, conf.getItem(THRESHOLD));
		else return null;
	}
	
}