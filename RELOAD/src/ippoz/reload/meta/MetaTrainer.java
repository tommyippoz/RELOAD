/**
 * 
 */
package ippoz.reload.meta;

import ippoz.reload.algorithm.DataSeriesDetectionAlgorithm;
import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.ThreadScheduler;
import ippoz.reload.trainer.AlgorithmTrainer;
import ippoz.reload.trainer.ConfigurationSelectorTrainer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tommy
 *
 */
public class MetaTrainer extends ThreadScheduler {
	
	private MetaLearner learner;
	
	private MetaData data;
	
	private List<AlgorithmTrainer> trainerList;
	
	public MetaTrainer(MetaData data, MetaLearner learner) {
		super();
		this.data = data;
		this.learner = learner;
		trainerList = new LinkedList<AlgorithmTrainer>();
	}

	public void addTrainer(LearnerType algTag, DataSeries dataSeries, List<Knowledge> kList){		
		List<BasicConfiguration> cList = data.getConfigurationsFor(algTag);
		cList = updateConfigurations(cList, algTag);
		trainerList.add(new ConfigurationSelectorTrainer(algTag, dataSeries, kList, data, cList));
	}
	
	protected List<BasicConfiguration> updateConfigurations(List<BasicConfiguration> cList, LearnerType algTag){
		List<BasicConfiguration> list = new ArrayList<BasicConfiguration>(cList.size());
		try {
			for(BasicConfiguration conf : cList){
				BasicConfiguration ac = (BasicConfiguration) conf.clone();
				ac.addItem(BasicConfiguration.DATASET_NAME, data.getDatasetName());
				ac.addItem(DataSeriesDetectionAlgorithm.TAG, learner.toCompactString() + File.separatorChar + algTag.toString() + "_" + trainerList.size());
				list.add(ac);
			}
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone Configurations");
		}
		return list;
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
