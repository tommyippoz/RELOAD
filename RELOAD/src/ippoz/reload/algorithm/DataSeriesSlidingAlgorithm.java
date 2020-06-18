/**
 * 
 */
package ippoz.reload.algorithm;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.utils.ObjectPair;

import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class DataSeriesSlidingAlgorithm extends DataSeriesDetectionAlgorithm {
	
	private int windowSize;

	public DataSeriesSlidingAlgorithm(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(BasicConfiguration.SLIDING_WINDOW_SIZE) && AppUtility.isInteger(conf.getItem(BasicConfiguration.SLIDING_WINDOW_SIZE))){
			windowSize = Integer.parseInt(conf.getItem(BasicConfiguration.SLIDING_WINDOW_SIZE));
		} else windowSize = -1;
		logScore(0.0, false);
	}
	
	@Override
	protected void logScore(double score, boolean flag) {
		if(loggedScores.size() >= getWindowSize())
			loggedScores.removeFirst();
		super.logScore(score, flag);
	}

	@Override
	public AlgorithmResult evaluateSnapshot(Knowledge knowledge, int currentIndex) {
		AlgorithmResult ar = super.evaluateSnapshot(knowledge, currentIndex);
		if(currentIndex >= getWindowSize()){
			logScore(ar.getScore(), ar.hasInjection());
		}
		return ar;
	}
	
	@Override
	public void saveLoggedScores() {
		
	}
	
	
	
	@Override
	public void loadLoggedScores() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjectPair<Double, Object> calculateSnapshotScore(Knowledge knowledge, int currentIndex, Snapshot sysSnapshot, double[] snapArray) {
		List<Snapshot> snapList;
		if(knowledge instanceof SlidingKnowledge){
			windowSize = ((SlidingKnowledge)knowledge).getWindowSize();
			snapList = knowledge.toArray(getDataSeries());
			if(snapList.size() >= DEFAULT_MINIMUM_ITEMS && snapList.size() >= windowSize)
				return evaluateSlidingSnapshot((SlidingKnowledge)knowledge, snapList, sysSnapshot);
		} 
		return new ObjectPair<Double, Object>(Double.NaN, null);
	}
	
	protected abstract ObjectPair<Double, Object> evaluateSlidingSnapshot(SlidingKnowledge sKnowledge, List<Snapshot> snapList, Snapshot dsSnapshot);

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}
	
	public int getWindowSize(){
		return windowSize;
	}

}
