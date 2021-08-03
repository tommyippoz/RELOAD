/**
 * 
 */
package ippoz.reload.output;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.loader.LoaderBatch;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.featureselection.FeatureSelectorType;
import ippoz.reload.info.FeatureSelectionInfo;
import ippoz.reload.info.TrainInfo;
import ippoz.reload.info.ValidationInfo;
import ippoz.reload.manager.InputManager;
import ippoz.reload.metric.Metric;
import ippoz.reload.metric.result.MetricResult;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class DetectorOutput {
	
	private InputManager iManager;
	
	private MetricResult bestScore;
	
	private AlgorithmModel refModel;
	
	private List<DataSeries> selectedSeries;
	
	private String writableTag;
	
	private LearnerType algorithms;
	
	private double faultsRatio;
	
	private Map<DataSeries, Map<FeatureSelectorType, Double>> selectedFeatures;	
	
	private Map<LoaderBatch, List<LabelledResult>> labelledScores;
	
	private FeatureSelectionInfo fsInfo;
	
	private TrainInfo tInfo;
	
	private ValidationInfo vInfo;
	
	public DetectorOutput(InputManager iManager, LearnerType algorithms, MetricResult bestScore, 
			AlgorithmModel modelList,
			Map<LoaderBatch, List<AlgorithmResult>> detailedExperimentsScores, 
			List<DataSeries> selectedSeries, Map<DataSeries, Map<FeatureSelectorType, Double>> selectedFeatures,
			String writableTag, double faultsRatio, FeatureSelectionInfo fsInfo, TrainInfo tInfo, ValidationInfo vInfo) {
		this.iManager = iManager;
		this.algorithms = algorithms;
		this.bestScore = bestScore;
		this.refModel = modelList;
		this.selectedSeries = selectedSeries;
		this.selectedFeatures = selectedFeatures;
		this.writableTag = writableTag;
		this.faultsRatio = faultsRatio;
		this.fsInfo = fsInfo;
		this.tInfo = tInfo;
		this.vInfo = vInfo;
		labelledScores = buildLabelledScores(detailedExperimentsScores);
	}
	
	/*public void printDetailedKnowledgeScores(String outputFolder){
		BufferedWriter writer;
		String header1 = "";
		String header2 = "";
		Map<AlgorithmModel, AlgorithmResult> map;
		Set<AlgorithmModel> voterList;
		try {
			if(votingScores != null && votingScores.size() > 0 &&
					detailedExperimentsScores != null && detailedExperimentsScores.size() > 0){
				writer = new BufferedWriter(new FileWriter(new File(buildPath(outputFolder) + "algorithmscores.csv")));
				header1 = "exp,index,fault/attack,reload_eval,reload_score,reload_confidence,";
				header2 = ",,,,,,";
				
				Iterator<String> it = detailedExperimentsScores.keySet().iterator();
				String tag = it.next();
				while(it.hasNext() && (detailedExperimentsScores.get(tag) == null || detailedExperimentsScores.get(tag).size() == 0)){
					tag = it.next();
				}
				
				map = detailedExperimentsScores.get(tag).get(0);
				voterList = map.keySet();
				for(AlgorithmModel av : voterList){
					header1 = header1 + "," + av.getAlgorithmType() + ",,,,," + av.getDataSeries().toString().replace("#PLAIN#", "(P)").replace("#DIFFERENCE#", "(D)").replace("NO_LAYER", "") + ",";
					header2 = header2 + ",score,decision_function,eval,confidence,,";
					if(av.getDataSeries().size() == 1){
						header2 = header2 + av.getDataSeries().getName().replace("#PLAIN#", "(P)").replace("#DIFFERENCE#", "(D)").replace("NO_LAYER", "");
					} else {
						for(int i=0;i<av.getDataSeries().size();i++){
							header1 = header1 + ",";
							header2 = header2 + ((MultipleDataSeries)av.getDataSeries()).getSeries(i).getSanitizedName() + ",";
						}
					}
					header2 = header2 + ",";					
				}
				
				writer.write("* This file reports on the scores each anomaly checker (couple of algorithm and indicator/feature) gives for each data point considered in the evaluation set. \n"
						+ "Data points are identified by name of the experiment and index inside the experiment, we report the true label of the data point (the one in the dataset) and the prediction made by RELOAD. \n"
						+ "In addition, for each anomaly checker we report a triple <score, decision function, evaluation> where the evaluation is calculated by applying such decision function to the score.\n");
				writer.write(header1 + "\n" + header2 + "\n");
				
				for(String expName : votingScores.keySet()){
					if(detailedExperimentsScores.get(expName) != null && detailedExperimentsScores.get(expName).size() > 0){
						//timedRef = detailedKnowledgeScores.get(expName).get(0).getDate();
						Knowledge knowledge = Knowledge.findKnowledge(knowledgeList, expName);
						for(int i=0;i<votingScores.get(expName).size();i++){
							writer.write(expName + "," + 
									i + "," + 
									(injections.get(expName).get(i) != null ? injections.get(expName).get(i).getDescription() : "") + "," +
									(votingScores.get(expName).get(i).getBooleanScore() ? "YES" : "NO") + "," +
									votingScores.get(expName).get(i).getVotingResult() + "," + 
									votingScores.get(expName).get(i).getConfidence() + ",");
							if(i < detailedExperimentsScores.get(expName).size()){
								map = detailedExperimentsScores.get(expName).get(i);
								for(AlgorithmModel av : voterList){
									for(AlgorithmModel mapVoter : map.keySet()){
										if(mapVoter.compareTo(av) == 0){
											av = mapVoter;
											break;
										}
									}
									writer.write("," + map.get(av).getScore() + "," + 
											(map.get(av).getDecisionFunction() != null ? map.get(av).getDecisionFunction().toCompactStringComplete() : "CUSTOM")  + "," + 
											map.get(av).getScoreEvaluation() + "," + map.get(av).getConfidence() + ",,");
									if(knowledge != null){
										Snapshot snap = knowledge.buildSnapshotFor(i, av.getDataSeries());
										if(av.getDataSeries().size() == 1){
											writer.write(((DataSeriesSnapshot)snap).getSnapValue().getFirst() + ",");
										} else {
											for(int j=0;j<av.getDataSeries().size();j++){
												writer.write(((MultipleSnapshot)snap).getSnapshot(((MultipleDataSeries)av.getDataSeries()).getSeries(j)).getSnapValue().getFirst() + ",");
											}
										}
									}
								}
							}
							writer.write("\n");
						}
					}
				}
				writer.close();
			}
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write summary files");
		}
	}*/

	public String buildPath(String basePath){
		String path = basePath + getDataset() + File.separatorChar + getAlgorithm() + File.separatorChar;
		if(!new File(path).exists())
			new File(path).mkdirs();
		return path;
	}
	
	/*public void summarizeCSV(String outputFolder) {
		BufferedWriter writer;
		double score;
		try {
			if(detailedMetricScores != null){
				writer = new BufferedWriter(new FileWriter(new File(buildPath(outputFolder) + "summary.csv")));
				writer.write("voter,anomaly_checkers,");
				for(Metric met : getEvaluationMetrics()){
					writer.write(met.getMetricName() + ",");
				}
				writer.write("\n");
				for(ScoresVoter voter : detailedMetricScores.keySet()){
					writer.write(voter.toString() + "," + nVoters.get(voter) + ",");
					for(Metric met : getEvaluationMetrics()){
						score = Double.parseDouble(Metric.getAverageMetricValue(detailedMetricScores.get(voter), met));
						writer.write(score + ",");
					}
					writer.write("\n");
				}
				writer.close();
				AppLogger.logInfo(getClass(), "Best score obtained is '" + getBestScore() + "'");
			}
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write summary files");
		}
	}*/

	public MetricResult getBestScore() {
		return bestScore;
	}
		
		/*
		String[][] grid = getEvaluationGrid(outFolder);
		int i = 0;
		for(Metric m : getEvaluationMetrics()){
			if()
		}
		List<AlgorithmResult> allResults = new LinkedList<>();
		if(votingScores != null){
			for(String expName : votingScores.keySet()){
				if(detailedExperimentsScores.get(expName) != null && detailedExperimentsScores.get(expName).size() > 0)
					allResults.addAll(votingScores.get(expName));
			}
			return getReferenceMetric().evaluateAnomalyResults(allResults);
		} else return Double.NaN;
	}*/
	
	public String getFormattedBestScore() {
		return new DecimalFormat("#.##").format(getBestScore().getDoubleValue());
	}

	public String getEvaluationBatches() {
		return vInfo.getBatchesString().replace("[", "").replace("]", "");
	}
	
	public String getEvaluationDataPoints() {
		return String.valueOf(vInfo.getNRuns());
	}
	
	public Metric getReferenceMetric() {
		return iManager.getTargetMetric();
	}

	public Metric[] getEvaluationMetrics() {
		return iManager.loadValidationMetrics();
	}
/*
	public String getEvaluationMetricsScores() {
		String outString = "";
		if(bestVoter != null){			
			List<AlgorithmResult> allResults = new LinkedList<>();
			for(String expName : votingScores.keySet()){
				if(detailedExperimentsScores.get(expName) != null && detailedExperimentsScores.get(expName).size() > 0)
					allResults.addAll(votingScores.get(expName));
			}
			
			for(Metric met : getEvaluationMetrics()){
				double res = met.evaluateAnomalyResults(allResults);
				if(Double.isNaN(res)){
					outString = outString + "-,";
				} else outString = outString + res + ",";
			}
		}
		return outString;
	}

	public String getWritableTag() {
		return writableTag;
	}*/
	
	public String getFaultsRatioString(){
		NumberFormat formatter = new DecimalFormat("#0.0");     
		return formatter.format(faultsRatio) + "%";
	}
	
	public String getDataset(){
		if(writableTag != null)
			return writableTag.split(",")[0];
		else return null;
	}
	
	public String getAlgorithm(){
		return algorithms.toCompactString();
	}
	
	public String getFullAlgorithm(){
		return algorithms.toString();
	}

	private String[][] getGrid(String scoresString) {
		List<String> gridRows = new LinkedList<>();
		if(scoresString != null && scoresString.trim().length() > 0){
			for(String splitItem : scoresString.trim().split(",")){
				if(splitItem.contains(":")){
					gridRows.add(splitItem.trim());
				}
			}
			int i = 0;
			String[][] grid = new String[2][gridRows.size()];
			for(String row : gridRows){
				String[] splitted = row.split(":");
				grid[0][i] = row.split(":")[0].trim();
				if(AppUtility.isInteger(splitted[1])){
					grid[1][i] = splitted[1].trim();
				} else if(AppUtility.isNumber(splitted[1])){
					grid[1][i] = AppUtility.formatDouble(Double.parseDouble(splitted[1].trim()));
				} else grid[1][i] = splitted[1].trim();
				i++;
			}
			return grid;
		} else return null;
	}
		/*
		int row = 0;
		String[][] result = new String[detailedMetricScores.keySet().size()][getEvaluationMetrics().length + 2];
		for(ScoresVoter voter : detailedMetricScores.keySet()){
			result[row][0] = voter.toString();
			result[row][1] = nVoters.get(voter).toString();
			int col = 2;
			for(Metric met : getEvaluationMetrics()){
				String res = Metric.getAverageMetricValue(detailedMetricScores.get(voter), met);
				if(res.equals(String.valueOf(Double.NaN))){
					result[row][col++] = "-";
				} else result[row][col++] = String.valueOf(new DecimalFormat("#.##").format(Double.parseDouble(res)));
			}
			row++;
		}
		return result;
	}*/
	
	public String[][] getTrainGrid() {
		return getGrid(tInfo.getMetricsString()); 
	}
	
	public String[][] getEvaluationGrid() {
		return getGrid(vInfo.getMetricsString()); 
	}
	
	public Map<LoaderBatch, List<LabelledResult>> getLabelledScores(){
		return labelledScores;
	}
	
	public List<AlgorithmResult> getAlgorithmResults(DecisionFunction df){
		List<AlgorithmResult> list = new LinkedList<>();
		Map<LoaderBatch, List<LabelledResult>> scoresMap = getLabelledScores();
		for(LoaderBatch expName : scoresMap.keySet()){
			List<LabelledResult> batchList = scoresMap.get(expName);
			for(LabelledResult lr : batchList){
				list.add(new AlgorithmResult(lr, df));
			}
		}
		return list;
	}
	
	private Map<LoaderBatch, List<LabelledResult>> buildLabelledScores(Map<LoaderBatch, List<AlgorithmResult>> votingScores){
		Map<LoaderBatch, List<LabelledResult>> outMap = new HashMap<>();
		if(votingScores != null && votingScores.size() > 0){
			for(LoaderBatch expName : votingScores.keySet()){
				outMap.put(expName, new LinkedList<LabelledResult>());
				for(int i=0;i<votingScores.get(expName).size();i++){
					AlgorithmResult ar = votingScores.get(expName).get(i);
					if(i < votingScores.get(expName).size()){
						outMap.get(expName).add(new LabelledResult(ar));
					}
				}
			}
		}
		return outMap;
	}

	public Map<DataSeries, Map<FeatureSelectorType, Double>> getSelectedFeatures() {
		Map<DataSeries, Map<FeatureSelectorType, Double>> toReturn = new HashMap<>();
		for(DataSeries ds : selectedFeatures.keySet()){
			if(selectedFeatures.get(ds) != null && selectedFeatures.get(ds).size() > 0){
				toReturn.put(ds, selectedFeatures.get(ds));
			}
		}
		return toReturn;
	}
	
	public List<DataSeries> getUsedFeatures() {
		List<DataSeries> usedFeatures = new LinkedList<DataSeries>();
		for(DataSeries ds : selectedFeatures.keySet()){
			for(DataSeries ss : getSelectedSeries()){
				if(ss.contains(ds) && !DataSeries.isIn(usedFeatures, ds)){
					usedFeatures.add(ds);
					break;
				}
			}
		}
		return usedFeatures;
	}

	public List<DataSeries> getSelectedSeries() {
		return selectedSeries;
	}
	
	public String getBestSeriesString() {
		if(refModel!= null){
			return refModel.getDataSeries().getName();
		} else return "";
	}

	public String getFeatureAggregationPolicy() {
		return iManager.getDataSeriesDomain();
	}    
	
	public List<AlgorithmModel> getTrainingModels(){
		List<AlgorithmModel> list = new LinkedList<>();
		list.add(refModel);
		if(refModel.getAlgorithmType() instanceof MetaLearner){
			String metaFile = "tmp" + File.separatorChar + getDataset() + File.separatorChar + ((MetaLearner)refModel.getAlgorithmType()).toCompactString() + File.separatorChar + "metaPreferences.csv";
			if(new File(metaFile).exists())
				list.addAll(AlgorithmModel.fromFile(metaFile));
		}
		return list;
	}  

	public int getKFold() {
		return iManager.getKFoldCounter();
	} 
	
	public String getTrainRuns(){
		return tInfo.getRuns();
	}
	
	public String getTrainDataPoints(){
		return String.valueOf(tInfo.getNRuns());
	}

	public String getTrainBatches() {
		return tInfo.getRuns().replace("[", "").replace("]", "");
	}

	public DecisionFunction getDecisionFunction() {
		if(refModel != null && refModel.getAlgorithm() != null)
			return refModel.getAlgorithm().getDecisionFunction();
		return null;
	}

	public double getPredictedMCC() {
		return fsInfo.getMCCPrediction();
	}

	public double getPredictedF2() {
		return fsInfo.getF2Prediction();
	}

	public double getPredictedR() {
		return fsInfo.getRPrediction();
	}

	/*public TrainInfo getTrainInfo() {
		if(tInfo != null)
			return tInfo;
		else return new TrainInfo();
	}

	public FeatureSelectionInfo getFeatureSelectionInfo() {
		if(fsInfo != null)
			return fsInfo;
		else return new FeatureSelectionInfo();
	}*/

	

}
