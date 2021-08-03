/**
 * 
 */
package ippoz.reload.featureselection;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppUtility;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public abstract class FeatureRanker extends FeatureSelector {
	
	private boolean higherIsBetter;
	
	private boolean considerAbsolute;
	
	private List<Double> sortedScores;

	public FeatureRanker(FeatureSelectorType fsType, double selectorThreshold, boolean rankThresholdFlag, boolean higherIsBetter, boolean considerAbsolute) {
		super(fsType, selectorThreshold, rankThresholdFlag);
		this.higherIsBetter = higherIsBetter;
		this.considerAbsolute = considerAbsolute;
	}

	@Override
	public double getHighestScore() {
		return getRankedScore(1);
	}

	@Override
	public double getRankedScore(int index) {
		if(sortedScores != null && sortedScores.size() > 0 && index-1 < sortedScores.size()){
			return sortedScores.get(index-1);
		} else return 0.0;
	}
	
	@Override
	public double getRankedAverageScore(int index) {
		if(sortedScores != null && sortedScores.size() > 0){
			if(index-1 < sortedScores.size()){
				return AppUtility.calcAvg(sortedScores.subList(0, index));
			} else return AppUtility.calcAvg(sortedScores);
		} else return 0.0;
	}

	@Override
	protected boolean checkSelection(DataSeries ds, Double toCheck, double threshold) {
		if(isRankedThreshold() && threshold >= 1 && sortedScores != null && sortedScores.size() > 0){
			for(int i=0;i<threshold;i++){
				if(i < sortedScores.size()){
					if(sortedScores.get(i) == toCheck)
						return true;
				} else break;
			}
			return false;
		} else {
			if(!Double.isFinite(toCheck))
				return false;
			else if(higherIsBetter){
				if(considerAbsolute)
					return Math.abs(toCheck) >= threshold;
				else return toCheck >= threshold;
			} else {
				if(considerAbsolute)
					return Math.abs(toCheck) <= threshold;
				else return toCheck <= threshold;
			}
		}
	}

	@Override
	public void applyFeatureSelection(List<DataSeries> seriesList, List<Knowledge> kList) {
		super.applyFeatureSelection(seriesList, kList);
		Map<DataSeries, Double> map = getScoresMap();
		if(map != null && map.size() > 0){
			sortedScores = new LinkedList<Double>();
			for(Double d : map.values()){
				if(!Double.isNaN(d) && Double.isFinite(d))
					sortedScores.add(d);
			}
			Collections.sort(sortedScores);
			if(higherIsBetter)
				Collections.reverse(sortedScores);
		}
	}
	
	

}
