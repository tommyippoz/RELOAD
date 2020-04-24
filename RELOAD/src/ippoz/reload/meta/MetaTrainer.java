/**
 * 
 */
package ippoz.reload.meta;

import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.ThreadScheduler;
import ippoz.reload.trainer.AlgorithmTrainer;
import ippoz.reload.trainer.ConfigurationSelectorTrainer;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class MetaTrainer extends ThreadScheduler {
	
	private MetaData data;
	
	private List<AlgorithmTrainer> trainerList;
	
	public MetaTrainer(MetaData data) {
		super();
		this.data = data;
		trainerList = new LinkedList<AlgorithmTrainer>();
	}

	public void addTrainer(LearnerType algTag, DataSeries dataSeries, List<Knowledge> kList){
		trainerList.add(new ConfigurationSelectorTrainer(algTag, dataSeries, kList, data));
	}

	@Override
	protected void initRun() {
		setThreadList(trainerList);
		AppLogger.logInfo(getClass(), "Meta-Training is Starting at " + new Date());
	}

	@Override
	protected void threadStart(Thread t, int tIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void threadComplete(Thread t, int tIndex) {
		// TODO Auto-generated method stub
		
	}

	public List<AlgorithmTrainer> getTrainers() {
		return trainerList;
	}

}
