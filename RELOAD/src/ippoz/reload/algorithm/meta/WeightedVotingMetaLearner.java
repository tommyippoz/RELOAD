/**
 * 
 */
package ippoz.reload.algorithm.meta;

import ippoz.reload.algorithm.DataSeriesNonSlidingAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.meta.MetaLearnerType;
import javafx.util.Pair;

/**
 * @author Tommy
 *
 */
public class WeightedVotingMetaLearner extends VotingMetaLearner {

	public WeightedVotingMetaLearner(DataSeries dataSeries, BasicConfiguration conf) {
		super(dataSeries, conf, MetaLearnerType.WEIGHTED_VOTING);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Pair<Double, Object> calculateSnapshotScore(double[] snapArray) {
		double count = 0;
		int i = 0;
		double[] scores = new double[baseLearners.size()];
		for(DataSeriesNonSlidingAlgorithm alg : baseLearners){
			double score = alg.calculateSnapshotScore(snapArray).getKey();
			scores[i++] = score;
			if(alg.getDecisionFunction().classify(new AlgorithmResult(false, score, 0.0, null)) == AnomalyResult.ANOMALY){
				String repString = alg.getConfiguration().getItem(BasicConfiguration.AVG_SCORE);
				if(AppUtility.isNumber(repString))
					count = count + Double.parseDouble(repString);
				else count = count + 1;
			}
		}
		return new Pair<Double, Object>(count, scores);
	}

}
