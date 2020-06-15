/**
 * 
 */
package ippoz.reload.algorithm;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.result.DBSCANResult;
import ippoz.reload.algorithm.result.KMeansResult;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.utils.ObjectPair;

import java.io.File;

import de.lmu.ifi.dbs.elki.data.model.KMeansModel;

// TODO: Auto-generated Javadoc
/**
 * The Class IndicatorDetectionAlgorithm.
 *
 * @author Tommy
 */
public abstract class DataSeriesDetectionAlgorithm extends DetectionAlgorithm {
	
	/** The Constant TMP_FILE. */
	protected static final String TMP_FILE = "tmp_file";
	
	/** The Constant TMP_FILE. */
	public static final String TAG = "tag";
	
	protected final static int DEFAULT_MINIMUM_ITEMS = 5;
	
	/** The indicator. */
	protected DataSeries dataSeries;

	/**
	 * Instantiates a new indicator detection algorithm.
	 *
	 * @param indicator the indicator
	 * @param categoryTag the data category tag
	 * @param conf the configuration
	 */
	public DataSeriesDetectionAlgorithm(DataSeries dataSeries, BasicConfiguration conf) {
		super(conf);
		this.dataSeries = dataSeries;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + dataSeries.getName();
	}
	
	/**
	 * Gets the filename used to store data about scores and histograms.
	 *
	 * @return the filename
	 */
	protected String getFilename(){
		String folder = getDefaultTmpFolder() + File.separatorChar;
		if(!new File(folder).exists())
			new File(folder).mkdirs();
		return folder + getDataSeries().getCompactString().replace("\\", "_").replace("/", "-").replace("*", "_") + "." + getLearnerType().toString().toLowerCase();
	}
	
	/**
	 * Gets the default folder used to store temporary data.
	 *
	 * @return the default temporary folder
	 */
	protected String getDefaultTmpFolder(){
		if(conf.hasItem(BasicConfiguration.DATASET_NAME) && conf.getItem(BasicConfiguration.DATASET_NAME).length() > 0){
			if(conf.hasItem(TAG))
				return "tmp" + File.separatorChar + conf.getItem(BasicConfiguration.DATASET_NAME) + File.separatorChar + conf.getItem(TAG);
			else if(getLearnerType() instanceof BaseLearner)
				return "tmp" + File.separatorChar + conf.getItem(BasicConfiguration.DATASET_NAME) + File.separatorChar + getLearnerType().toString();
			else return "tmp" + File.separatorChar + conf.getItem(BasicConfiguration.DATASET_NAME);
		} else {
			if(conf.hasItem(TAG))
				return "tmp" + File.separatorChar + conf.getItem(TAG);
			else if(getLearnerType() instanceof BaseLearner)
				return "tmp" + File.separatorChar + getLearnerType().toString();
			else return "tmp";
		}
	}

	@Override
	public DataSeries getDataSeries() {
		return dataSeries;
	}

	@Override
	public AlgorithmResult evaluateSnapshot(Knowledge knowledge, int currentIndex) {
		AlgorithmResult ar;
		ObjectPair<Double, Object> score;
		Snapshot dsSnap = knowledge.get(currentIndex, getDataSeries());
		double[] snapArray = getSnapValueArray(dsSnap);
		if(dsSnap != null && snapArray != null && checkCalculationCondition(snapArray)){
			score = calculateSnapshotScore(knowledge, currentIndex, dsSnap, snapArray);
			if(getLearnerType() instanceof BaseLearner){
				AlgorithmType at = ((BaseLearner)getLearnerType()).getAlgType(); 
				if(at == AlgorithmType.DBSCAN) {
					ar = new DBSCANResult(dsSnap.getInjectedElement() != null, score.getKey(), (Double)score.getValue(), getConfidence(score.getKey()));
				} else if(score.getValue() != null && score.getValue() instanceof KMeansModel){
					KMeansModel kms = (KMeansModel)score.getValue();
					ar = new KMeansResult(dsSnap.getInjectedElement() != null, score.getKey(), kms.getMean(), kms.getVarianceContribution(), getConfidence(score.getKey()));
				} else ar = new AlgorithmResult(dsSnap.getInjectedElement() != null, score.getKey(), getConfidence(score.getKey()), score.getValue());
			} else ar = new AlgorithmResult(dsSnap.getInjectedElement() != null, score.getKey(), getConfidence(score.getKey()), score.getValue());
			getDecisionFunction().assignScore(ar, true);
			return ar;
		} else return AlgorithmResult.error(dsSnap.getInjectedElement() != null);
	}
	
	protected double[] getSnapValueArray(Snapshot snap){
		double snapValue;
		double[] result = new double[getDataSeries().size()];
		if(getDataSeries().size() == 1){
			snapValue = ((DataSeriesSnapshot)snap).getSnapValue().getFirst();
			result[0] = snapValue;
		} else {
			for(int j=0;j<getDataSeries().size();j++){
				snapValue = ((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
				result[j] = snapValue;
			}
		}
		return result;
	}

	protected abstract boolean checkCalculationCondition(double[] snapArray);
	
}
