/**
 * 
 */
package ippoz.madness.detector.algorithm.elki;

import ippoz.madness.detector.algorithm.elki.support.CustomSVM;
import ippoz.madness.detector.algorithm.elki.support.CustomSVM.SVMKernel;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;

import java.io.File;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class SVMELKI extends DataSeriesElkiAlgorithm {
	
	private static final String NU = "nu";
	
	private static final String KERNEL = "kernel";
	
	private static final String TMP_FILE = "tmp_file";
	
	public static final String DEFAULT_TMP_FOLDER = "svm_tmp";
	
	private CustomSVM<NumberVector> cSVM;
	
	public SVMELKI(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf, false, true);
		SVMKernel kernel = getKernel(conf);
		cSVM = new CustomSVM<NumberVector>(kernel != null ? kernel : SVMKernel.LINEAR, getNU(conf));
		if(conf.hasItem(TMP_FILE)){
			cSVM.loadFile(conf.getItem(TMP_FILE));
		}
	}

	private double getNU(AlgorithmConfiguration conf) {
		if(conf.hasItem(NU)){
			if(conf.getItem(NU).trim().length() > 0){
				try {
					return Double.valueOf(conf.getItem(NU).trim());
				} catch(Exception ex){
					return 0.05;
				}
			} else return 0.05;
		}return 0.05;
	}

	private SVMKernel getKernel(AlgorithmConfiguration conf) {
		if(conf.hasItem(KERNEL)){
			if(conf.getItem(KERNEL).trim().length() > 0){
				try {
					return SVMKernel.valueOf(conf.getItem(KERNEL).trim());
				} catch(Exception ex){
					return null;
				}
			} else return null;
		} else return null;
	}

	@Override
	protected void automaticElkiTraining(Database db) {

		cSVM.run(db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD));
		
		conf.addItem(TMP_FILE, getFilename());
	    
	    if(!new File(getFilename()).exists()){
	    	if(!new File(DEFAULT_TMP_FOLDER).exists())
	    		new File(DEFAULT_TMP_FOLDER).mkdirs();
	    	cSVM.printFile(new File(getFilename()));
	    }
	}
	
	private String getFilename(){
		return DEFAULT_TMP_FOLDER + File.separatorChar + getDataSeries().toCompactString().replace("\\", "_").replace("/", "_") + ".svm";
	}

	@Override
	protected double evaluateElkiSnapshot(Snapshot sysSnapshot) {
		Vector v = convertSnapToVector(sysSnapshot);
		if(v.getDimensionality() > 0 && Double.isFinite(v.doubleValue(0))){
			return cSVM.evaluateSVM(v) ? 1.0 : 0.0;
		} else return 0.0;
	}

}

