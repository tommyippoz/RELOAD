/**
 * 
 */
package ippoz.multilayer.detector.manager;

import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.ExperimentData;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.invariants.DataSeriesMember;
import ippoz.multilayer.detector.commons.invariants.Invariant;
import ippoz.multilayer.detector.metric.Metric;
import ippoz.multilayer.detector.performance.TrainingTiming;
import ippoz.multilayer.detector.reputation.Reputation;
import ippoz.multilayer.detector.trainer.AlgorithmTrainer;
import ippoz.multilayer.detector.trainer.FixedConfigurationTrainer;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Tommy
 *
 */
public class InvariantManager {
	
	private final String[] invariantOperandList = {">"};
	
	private LinkedList<DataSeries> seriesList;
	private TrainingTiming tTiming;
	private HashMap<String, String> invCombinations;
	private LinkedList<ExperimentData> expList;
	private Metric metric;
	private Reputation reputation;
	
	public InvariantManager(LinkedList<DataSeries> seriesList, TrainingTiming tTiming, LinkedList<ExperimentData> expList, Metric metric, Reputation reputation, HashMap<String, String> invCombinations) {
		this.seriesList = seriesList;
		this.tTiming = tTiming;
		this.invCombinations = invCombinations;
		this.expList = expList;
		this.metric = metric;
		this.reputation = reputation;
	}

	private LinkedList<AlgorithmTrainer> filterInvSyntax(LinkedList<AlgorithmTrainer> allInv) {
		Invariant invariant;
		LinkedList<AlgorithmTrainer> filtered = new LinkedList<AlgorithmTrainer>();
		for(AlgorithmTrainer invTrainer : allInv){
			invariant = (Invariant)invTrainer.getBestConfiguration().getRawItem(AlgorithmConfiguration.INVARIANT);
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
			invariant = (Invariant)invTrainer.getBestConfiguration().getRawItem(AlgorithmConfiguration.INVARIANT);
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
					allInv.add(new FixedConfigurationTrainer(AlgorithmType.INV, null, metric, reputation, tTiming, expList, conf));
				}
			}			
		}
		return allInv;
	}
	
	private LinkedList<AlgorithmTrainer> generateInvariants() {
		DataSeries firstDS, secondDS;
		AlgorithmConfiguration conf;
		LinkedList<AlgorithmTrainer> allInv = new LinkedList<AlgorithmTrainer>();
		for(String firstString : invCombinations.keySet()){
			firstDS = DataSeries.fromList(seriesList, firstString);
			secondDS = DataSeries.fromList(seriesList, invCombinations.get(firstString));
			for(String operand : invariantOperandList){
				conf = new AlgorithmConfiguration(AlgorithmType.INV);
				conf.addRawItem(AlgorithmConfiguration.INVARIANT, new Invariant(new DataSeriesMember(firstDS), new DataSeriesMember(secondDS), operand));
				allInv.add(new FixedConfigurationTrainer(AlgorithmType.INV, null, metric, reputation, tTiming, expList, conf));
			}
		}
		return allInv;
	}
	
	public LinkedList<AlgorithmTrainer> getInvariants(boolean all){
		LinkedList<AlgorithmTrainer> allInv;
		if(all)
			allInv = generateAllInvariants();
		else allInv = generateInvariants();
		return filterInvSyntax(allInv);
	}
	
}
