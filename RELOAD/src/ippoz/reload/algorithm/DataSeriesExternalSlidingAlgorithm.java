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
public abstract class DataSeriesExternalSlidingAlgorithm extends DataSeriesExternalAlgorithm {
	
	private int windowSize;
	
	public DataSeriesExternalSlidingAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf, boolean needNormalization) {
		super(dataSeries, conf, needNormalization);
		if(conf.hasItem(AlgorithmConfiguration.SLIDING_WINDOW_SIZE) && AppUtility.isInteger(conf.getItem(AlgorithmConfiguration.SLIDING_WINDOW_SIZE))){
			windowSize = Integer.parseInt(conf.getItem(AlgorithmConfiguration.SLIDING_WINDOW_SIZE));
		} else windowSize = -1;
	}

	@Override
	protected void logScore(double score) {
		if(loggedScores.size() >= getWindowSize())
			loggedScores.removeFirst();
		super.logScore(score);
	}

	@Override
	protected AlgorithmResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		List<Snapshot> snapList;
		if(knowledge instanceof SlidingKnowledge){
			snapList = knowledge.toArray(getDataSeries());
			if(snapList.size() >= DEFAULT_MINIMUM_ITEMS && snapList.size() >= ((SlidingKnowledge)knowledge).getWindowSize())
				return evaluateSlidingSnapshot((SlidingKnowledge)knowledge, snapList, sysSnapshot);
			else return AlgorithmResult.unknown(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
		} else {
			AppLogger.logError(getClass(), "WrongKnowledgeError", "Knowledge is not 'Sliding'");
			return AlgorithmResult.error(sysSnapshot.listValues(true), sysSnapshot.getInjectedElement());
		}
	}
	
	public int getWindowSize(){
		return windowSize;
	}
	
	protected abstract AlgorithmResult evaluateSlidingSnapshot(SlidingKnowledge sKnowledge, List<Snapshot> snapList, Snapshot dsSnapshot);

}
