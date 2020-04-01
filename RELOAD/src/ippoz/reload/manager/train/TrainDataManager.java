/**
 * 
 */
package ippoz.reload.manager.train;

import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.manager.DataManager;
import ippoz.reload.metric.Metric;
import ippoz.reload.reputation.Reputation;

import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public abstract class TrainDataManager extends DataManager {
	
	/** The setup folder. */
	protected String setupFolder;
	
	/** The data series domain. */
	protected String dsDomain;
	
	/** The scores folder. */
	private String scoresFolder;
	
	private String datasetName;
	
	/** The possible configurations. */
	protected Map<AlgorithmType, List<AlgorithmConfiguration>> confList;
	
	/** The chosen metric. */
	private Metric metric;
	
	/** The chosen reputation metric. */
	private Reputation reputation;
	
	/** The list of indicators. */
	protected List<DataSeries> seriesList;
	
	/** The algorithm types. */
	protected List<AlgorithmType> algTypes;
	
	protected int kfold;

	/**
	 * Instantiates a new trainer data manager.
	 *
	 */
	public TrainDataManager(Map<KnowledgeType, List<Knowledge>> map, String setupFolder, String dsDomain, String scoresFolder, String datasetName, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, List<AlgorithmType> algTypes, int kfold) {
		super(map);
		this.setupFolder = setupFolder;
		this.dsDomain = dsDomain;
		this.scoresFolder = scoresFolder;
		this.datasetName = datasetName;
		this.confList = confList;
		this.metric = metric;
		this.reputation = reputation;
		this.algTypes = algTypes;
		this.kfold = kfold;
	}
	
	/**
	 * Instantiates a new trainer data manager.
	 *
	 */
	public TrainDataManager(Map<KnowledgeType, List<Knowledge>> expList, String setupFolder, String dsDomain, String scoresFolder, String datasetName, Map<AlgorithmType, List<AlgorithmConfiguration>> confList, Metric metric, Reputation reputation, List<AlgorithmType> algTypes, List<DataSeries> selectedSeries, int kfold) {
		this(expList, setupFolder, dsDomain, scoresFolder, datasetName, confList, metric, reputation, algTypes, kfold);
		seriesList = selectedSeries;
		AppLogger.logInfo(getClass(), seriesList.size() + " Data Series Loaded");
	}
	
	public String getDatasetName(){
		return datasetName;
	}

	public String getSetupFolder() {
		return setupFolder;
	}

	public String getDsDomain() {
		return dsDomain;
	}

	public String getScoresFolder() {
		return scoresFolder;
	}

	public Metric getMetric() {
		return metric;
	}

	public Reputation getReputation() {
		return reputation;
	}
	
}
