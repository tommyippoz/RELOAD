/**
 * 
 */
package ippoz.madness.detector.algorithm;

import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.decisionfunction.AnomalyResult;
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
	protected AnomalyResult evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		List<Snapshot> snapList;
		if(knowledge instanceof SlidingKnowledge){
			snapList = knowledge.toArray(getAlgorithmType(), getDataSeries());
			if(snapList.size() >= DEFAULT_MINIMUM_ITEMS && snapList.size() >= ((SlidingKnowledge)knowledge).getWindowSize())
				return evaluateSlidingSnapshot((SlidingKnowledge)knowledge, snapList, sysSnapshot);
			else return AnomalyResult.UNKNOWN;
		} else {
			AppLogger.logError(getClass(), "WrongKnowledgeError", "Knowledge is not 'Sliding'");
			return AnomalyResult.NORMAL;
		}
	}
	
	public int getWindowSize(){
		return windowSize;
	}
	
	protected abstract AnomalyResult evaluateSlidingSnapshot(SlidingKnowledge sKnowledge, List<Snapshot> snapList, Snapshot dsSnapshot);

}
