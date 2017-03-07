/**
 * 
 */
package ippoz.multilayer.detector.algorithm;

import java.util.HashMap;
import java.util.LinkedList;

import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.DataSeriesSnapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.service.StatPair;

/**
 * @author Tommy
 *
 */
public class WesternElectricRulesChecker extends DataSeriesDetectionAlgorithm {
	
	private HashMap<ServiceCall, LinkedList<WER_Zone>> histZones;
	
	public WesternElectricRulesChecker(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		histZones = new HashMap<ServiceCall, LinkedList<WER_Zone>>();
	}

	@Override
	protected double evaluateDataSeriesSnapshot(DataSeriesSnapshot sysSnapshot) {
		double anomalyRate = 0.0;
		if(sysSnapshot.getServiceCalls().size() > 0){
			for(ServiceCall sCall : sysSnapshot.getServiceCalls()){
				anomalyRate = anomalyRate + evaluateZones(updateHistZones(sCall, sysSnapshot.getSnapValue(), sysSnapshot.getSnapStat(sCall)));
			}
			return anomalyRate / sysSnapshot.getServiceCalls().size();
		} else return 0;
	}

	private double evaluateZones(ServiceCall histSCall) {
		double zonesScore = 0.0;
		LinkedList<WER_Zone> list = histZones.get(histSCall);
		for(int i=1;i<=4;i++){
			zonesScore = zonesScore + (evaluateWERule(i, list) ? 1.0 : 0.0);
		}
		return zonesScore > 0 ? 1.0 : 0.0;
	}

	private boolean evaluateWERule(int ruleIndex, LinkedList<WER_Zone> list) {
		int count = 0;
		if(list != null && list.size() > 0){
			switch(ruleIndex){
				case 1:
					if(list.getLast() == WER_Zone.OVER_UP || list.getLast() == WER_Zone.OVER_LOW)
						return true;
					break;
				case 2:
					if(list.size() >= 3){
						count = 0;
						for(int i=0;i<3;i++){
							if(list.get(list.size() - 1 - i) == WER_Zone.OVER_UP || list.get(list.size() - 1 - i) == WER_Zone.A_UP)
								count++;
						}
						if(count >= 2)
							return true;
						count = 0;
						for(int i=0;i<3;i++){
							if(list.get(list.size() - 1 - i) == WER_Zone.OVER_LOW || list.get(list.size() - 1 - i) == WER_Zone.A_LOW)
								count++;
						}
						if(count >= 2)
							return true;
					}
					break;
				case 3:
					if(list.size() >= 5){
						count = 0;
						for(int i=0;i<5;i++){
							if(list.get(list.size() - 1 - i) == WER_Zone.OVER_UP || list.get(list.size() - 1 - i) == WER_Zone.A_UP || list.get(list.size() - 1 - i) == WER_Zone.B_UP)
								count++;
						}
						if(count >= 4)
							return true;
						count = 0;
						for(int i=0;i<5;i++){
							if(list.get(list.size() - 1 - i) == WER_Zone.OVER_LOW || list.get(list.size() - 1 - i) == WER_Zone.A_LOW || list.get(list.size() - 1 - i) == WER_Zone.B_LOW)
								count++;
						}
						if(count >= 4)
							return true;
					}
					break;
				case 4:
					if(list.size() >= 9){
						count = 0;
						for(int i=0;i<9;i++){
							if(list.get(list.size() - 1 - i).toString().toUpperCase().endsWith("_UP"))
								count++;
						}
						if(count >= 9)
							return true;
						count = 0;
						for(int i=0;i<9;i++){
							if(list.get(list.size() - 1 - i).toString().toUpperCase().endsWith("_LOW"))
								count++;
						}
						if(count >= 9)
							return true;
					}
					break;
			}
		} 
		return false;
	}

	private ServiceCall updateHistZones(ServiceCall sCall, Double snapValue, StatPair snapStat) {
		ServiceCall entry = null;
		for(ServiceCall mapCall : histZones.keySet()){
			if(mapCall.compareTo(sCall) == 0)
				entry = mapCall;
		}
		if(entry == null){ 
			histZones.put(sCall, new LinkedList<WER_Zone>());
			entry = sCall;
		}
		histZones.get(entry).add(getZone(snapValue, snapStat));
		return entry;
	}

	private WER_Zone getZone(double obsVal, StatPair stat) {
		if(obsVal > stat.getAvg() + 3*stat.getStd())
			return WER_Zone.OVER_UP;
		else if(obsVal > stat.getAvg() + 2*stat.getStd())
			return WER_Zone.A_UP;
		else if(obsVal > stat.getAvg() + stat.getStd())
			return WER_Zone.B_UP;
		else if(obsVal > stat.getAvg())
			return WER_Zone.C_UP;
		else if(obsVal > stat.getAvg() - stat.getStd())
			return WER_Zone.C_LOW;
		else if(obsVal > stat.getAvg() - 2*stat.getStd())
			return WER_Zone.B_LOW;
		else if(obsVal > stat.getAvg() - 3*stat.getStd())
			return WER_Zone.A_UP;
		else return WER_Zone.OVER_LOW;
	}

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}
	
	private enum WER_Zone {
		OVER_UP,
		C_UP,
		B_UP,
		A_UP,
		A_LOW,
		B_LOW,
		C_LOW,
		OVER_LOW
	}

}
