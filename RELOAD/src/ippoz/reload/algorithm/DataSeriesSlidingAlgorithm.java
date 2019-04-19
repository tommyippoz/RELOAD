/**
 * 
 */
package ippoz.reload.algorithm;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.SlidingKnowledge;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppUtility;
import ippoz.utils.logging.AppLogger;

import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class DataSeriesSlidingAlgorithm extends DataSeriesDetectionAlgorithm {
	
	private int windowSize;

	public DataSeriesSlidingAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		if(conf.hasItem(AlgorithmConfiguration.SLIDING_WINDOW_SIZE) && AppUtility.isInteger(conf.getItem(AlgorithmConfiguration.SLIDING_WINDOW_SIZE))){
			windowSize = Integer.parseInt(conf.getItem(AlgorithmConfiguration.SLIDING_WINDOW_SIZE));
		} else windowSize = -1;
	}

	@Override
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		AlgorithmResult ar = calculateSnapshotScore(knowledge, sysSnapshot, currentIndex);
		if(currentIndex >= getWindowSize()){
			logScore(ar.getScore());
		}
		return ar;
	}
	
	private AlgorithmResult calculateSnapshotScore(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		List<Snapshot> snapList;
		if(knowledge instanceof SlidingKnowledge){
			windowSize = ((SlidingKnowledge)knowledge).getWindowSize();
			snapList = knowledge.toArray(getDataSeries());
			if(snapList.size() >= DEFAULT_MINIMUM_ITEMS && snapList.size() >= windowSize)
				return evaluateSlidingSnapshot((SlidingKnowledge)knowledge, snapList, sysSnapshot);
			else return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
		} else {
			AppLogger.logError(getClass(), "WrongKnowledgeError", "Knowledge is not 'Sliding'");
			return AlgorithmResult.error(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
		}
	}
	
	protected abstract AlgorithmResult evaluateSlidingSnapshot(SlidingKnowledge sKnowledge, List<Snapshot> snapList, Snapshot dsSnapshot);

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
