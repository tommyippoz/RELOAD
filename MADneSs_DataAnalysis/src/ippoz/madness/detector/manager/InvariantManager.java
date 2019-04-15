/**
 * 
 */
package ippoz.madness.detector.manager;

import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.performance.TrainingTiming;
import ippoz.madness.detector.reputation.Reputation;
import ippoz.madness.detector.trainer.AlgorithmTrainer;
import ippoz.madness.detector.trainer.FixedConfigurationTrainer;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.IndicatorDataSeries;
import ippoz.reload.commons.invariants.DataSeriesMember;
import ippoz.reload.commons.invariants.Invariant;
import ippoz.reload.commons.knowledge.Knowledge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class InvariantManager {
	
	private final String[] invariantOperandList = {">"};
	
	private List<IndicatorDataSeries> seriesList;
	
	private Map<String, String> invCombinations;
	
	private List<Knowledge> kList;
	
	private Metric metric;
	
	private Reputation reputation;
	
	public InvariantManager(LinkedList<IndicatorDataSeries> seriesList, TrainingTiming tTiming, List<Knowledge> kList, Metric metric, Reputation reputation, Map<String, String> invCombinations) {
		this.seriesList = seriesList;
		this.invCombinations = invCombinations;
		this.kList = kList;
		this.metric = metric;
		this.reputation = reputation;
	}

	private List<AlgorithmTrainer> filterInvSyntax(List<AlgorithmTrainer> allInv) {
		Invariant invariant;
		List<AlgorithmTrainer> filtered = new LinkedList<AlgorithmTrainer>();
		for(AlgorithmTrainer invTrainer : allInv){
			invariant = (Invariant)invTrainer.getBestConfiguration().getRawItem(AlgorithmConfiguration.INVARIANT, false);
			if(!invariant.getFirstMember().getMemberName().equals(invariant.getSecondMember().getMemberName())){	
				filtered.add(invTrainer);
			}
		}
		return filtered;
	}
	
	public LinkedList<AlgorithmTrainer> filterInvType(LinkedList<AlgorithmTrainer> allInv) {
		Invariant invariant;
		LinkedList<AlgorithmTrainer> toRemove = new LinkedList<AlgorithmTrainer>();
		LinkedList<String> foundMembers = new LinkedList<String>();
		for(AlgorithmTrainer invTrainer : allInv){
			invariant = (Invariant)invTrainer.getBestConfiguration().getRawItem(AlgorithmConfiguration.INVARIANT, false);
			if(invariant.getFirstMember() instanceof DataSeriesMember){
				if(!foundMembers.contains(invariant.getFirstMember().toString()))
					foundMembers.add(invariant.getFirstMember().toString());
				else toRemove.add(invTrainer);
			}
		}
		return toRemove;
	}
	
	private LinkedList<AlgorithmTrainer> generateAllInvariants() {
		AlgorithmConfiguration conf;
		LinkedList<AlgorithmTrainer> allInv = new LinkedList<AlgorithmTrainer>();
		for(DataSeries firstDS : seriesList){
			for(DataSeries secondDS : seriesList){
				for(String operand : invariantOperandList){
					conf = new AlgorithmConfiguration(AlgorithmType.INV);
					conf.addRawItem(AlgorithmConfiguration.INVARIANT, new Invariant(new DataSeriesMember(firstDS), new DataSeriesMember(secondDS), operand));
					allInv.add(new FixedConfigurationTrainer(AlgorithmType.INV, null, metric, reputation, kList, conf));
				}
			}			
		}
		return allInv;
	}
	
	private List<AlgorithmTrainer> generateInvariants() {
		DataSeries firstDS, secondDS;
		AlgorithmConfiguration conf;
		List<AlgorithmTrainer> allInv = new ArrayList<AlgorithmTrainer>(invCombinations.keySet().size()*invariantOperandList.length);
		for(String firstString : invCombinations.keySet()){
			firstDS = DataSeries.fromList(seriesList, firstString);
			secondDS = DataSeries.fromList(seriesList, invCombinations.get(firstString));
			for(String operand : invariantOperandList){
				conf = new AlgorithmConfiguration(AlgorithmType.INV);
				conf.addRawItem(AlgorithmConfiguration.INVARIANT, new Invariant(new DataSeriesMember(firstDS), new DataSeriesMember(secondDS), operand));
				allInv.add(new FixedConfigurationTrainer(AlgorithmType.INV, null, metric, reputation, kList, conf));
			}
		}
		return allInv;
	}
	
	public List<AlgorithmTrainer> getInvariants(boolean all){
		List<AlgorithmTrainer> allInv;
		if(all)
			allInv = generateAllInvariants();
		else allInv = generateInvariants();
		return filterInvSyntax(allInv);
	}
	
}
