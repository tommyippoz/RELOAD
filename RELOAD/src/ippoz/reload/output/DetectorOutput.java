/**
 * 
 */
package ippoz.reload.output;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.TimedResult;
import ippoz.reload.featureselection.FeatureSelectorType;
import ippoz.reload.info.FeatureSelectionInfo;
import ippoz.reload.info.TrainInfo;
import ippoz.reload.loader.Loader;
import ippoz.reload.manager.InputManager;
import ippoz.reload.metric.Metric;
import ippoz.reload.voter.AlgorithmVoter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tommy
 *
 */
public class DetectorOutput {
	
	private InputManager iManager;
	
	private List<Knowledge> knowledgeList;
	
	private String bestSetup;
	
	private String bestRuns;
	
	private List<AlgorithmVoter> voterList;
	
	private String evaluationMetricsScores;
	
	private String[] anomalyTresholds;
	
	private Map<String, Integer> nVoters;
	
	private Map<String, List<TimedResult>> detailedKnowledgeScores;
	
	private Loader loader;
	
	private List<DataSeries> selectedSeries;
	
	private Map<String, Map<String, List<Map<Metric, Double>>>> detailedMetricScores;
	
	private Map<String, List<Map<AlgorithmVoter, AlgorithmResult>>> detailedExperimentsScores;
	
	private Map<String, List<InjectedElement>> injections;
	
	private double bestAnomalyThreshold;
	
	private String writableTag;
	
	private double faultsRatio;
	
	private Map<DataSeries, Map<FeatureSelectorType, Double>> selectedFeatures;
	
	private FeatureSelectionInfo fsInfo;
	
	private TrainInfo tInfo;
	
	public DetectorOutput(InputManager iManager, List<Knowledge> knowledgeList, double bestScore, String bestSetup, 
			List<AlgorithmVoter> voterList, String evaluationMetricsScores, String[] anomalyTresholds, Map<String, Integer> nVoters, 
			Map<String, List<TimedResult>> detailedKnowledgeScores,
			Loader loader, Map<String, Map<String, List<Map<Metric, Double>>>> evaluations,
			Map<String, List<Map<AlgorithmVoter, AlgorithmResult>>> detailedExperimentsScores,
			double bestAnomalyThreshold, Map<String, List<InjectedElement>> injections, 
			List<DataSeries> selectedSeries, Map<DataSeries, Map<FeatureSelectorType, Double>> selectedFeatures,
			String writableTag, double faultsRatio, FeatureSelectionInfo fsInfo, TrainInfo tInfo) {
		this.iManager = iManager;
		this.knowledgeList = knowledgeList;
		this.bestSetup = bestSetup;
		this.voterList = voterList;
		this.evaluationMetricsScores = evaluationMetricsScores;
		this.anomalyTresholds = anomalyTresholds;
		this.nVoters = nVoters;
		this.detailedKnowledgeScores = detailedKnowledgeScores;
		this.loader = loader;
		this.detailedMetricScores = evaluations;
		this.detailedExperimentsScores = detailedExperimentsScores;
		this.bestAnomalyThreshold = bestAnomalyThreshold;
		this.injections = injections;
		this.selectedSeries = selectedSeries;
		this.selectedFeatures = selectedFeatures;
		this.writableTag = writableTag;
		this.faultsRatio = faultsRatio;
		this.fsInfo = fsInfo;
		this.tInfo = tInfo;
	}
	
	public void printDetailedKnowledgeScores(String outputFolder){
		BufferedWriter writer;
		String header1 = "";
		String header2 = "";
		Map<AlgorithmVoter, AlgorithmResult> map;
		Set<AlgorithmVoter> voterList;
		Date timedRef;
		try {
			if(detailedKnowledgeScores != null && detailedKnowledgeScores.size() > 0 &&
					detailedExperimentsScores != null && detailedExperimentsScores.size() > 0){
				writer = new BufferedWriter(new FileWriter(new File(buildPath(outputFolder) + "algorithmscores.csv")));
				header1 = "exp,index,fault/attack,reload_eval,reload_score,";
				header2 = ",,,,,";
				
				Iterator<String> it = detailedExperimentsScores.keySet().iterator();
				String tag = it.next();
				while(it.hasNext() && (detailedExperimentsScores.get(tag) == null || detailedExperimentsScores.get(tag).size() == 0)){
					tag = it.next();
				}
				
				map = detailedExperimentsScores.get(tag).get(0);
				voterList = map.keySet();
				for(AlgorithmVoter av : voterList){
					header1 = header1 + "," + av.getAlgorithmType() + ",,," + av.getDataSeries().toString().replace("#PLAIN#", "(P)").replace("#DIFFERENCE#", "(D)").replace("NO_LAYER", "") + ",";
					header2 = header2 + ",score,decision_function,eval,";
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
				
				for(String expName : detailedKnowledgeScores.keySet()){
					if(detailedExperimentsScores.get(expName) != null && detailedExperimentsScores.get(expName).size() > 0){
						timedRef = detailedKnowledgeScores.get(expName).get(0).getDate();
						Knowledge knowledge = findKnowledge(expName);
						for(int i=0;i<detailedKnowledgeScores.get(expName).size();i++){
							writer.write(expName + "," + 
									detailedKnowledgeScores.get(expName).get(i).getDateOffset(timedRef) + "," + 
									(injections.get(expName).get(i) != null ? injections.get(expName).get(i).getDescription() : "") + "," +
									(detailedKnowledgeScores.get(expName).get(i).getValue() >= bestAnomalyThreshold ? "YES" : "NO") + "," +
									detailedKnowledgeScores.get(expName).get(i).getValue() + ",");
							if(i < detailedExperimentsScores.get(expName).size()){
								map = detailedExperimentsScores.get(expName).get(i);
								for(AlgorithmVoter av : voterList){
									for(AlgorithmVoter mapVoter : map.keySet()){
										if(mapVoter.compareTo(av) == 0){
											av = mapVoter;
											break;
										}
									}
									writer.write("," + map.get(av).getScore() + "," + 
											(map.get(av).getDecisionFunction() != null ? map.get(av).getDecisionFunction().toCompactStringComplete() : "CUSTOM")  + "," + 
											map.get(av).getScoreEvaluation() + ",");
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
	}

	private Knowledge findKnowledge(String expName) {
		for(Knowledge know : knowledgeList){
			if(know.getTag().equals(expName))
				return know;
		}
		return null;
	}

	public String buildPath(String basePath){
		String path = basePath + getDataset() + getAlgorithm() + File.separatorChar;
		if(!new File(path).exists())
			new File(path).mkdirs();
		return path;
	}
	
	public void summarizeCSV(String outputFolder) {
		BufferedWriter writer;
		BufferedWriter compactWriter;
		double score;
		try {
			compactWriter = new BufferedWriter(new FileWriter(new File(buildPath(outputFolder) + "tableSummary.csv")));
			writer = new BufferedWriter(new FileWriter(new File(buildPath(outputFolder) + "summary.csv")));
			compactWriter.write("selection_strategy,checkers,");
			for(String anomalyTreshold : anomalyTresholds){
				compactWriter.write(anomalyTreshold + ",");
			}
			compactWriter.write("\n");
			writer.write("voter,anomaly,checkers,");
			for(Metric met : getEvaluationMetrics()){
				writer.write(met.getMetricName() + ",");
			}
			writer.write("\n");
			for(String voterTreshold : detailedMetricScores.keySet()){
				compactWriter.write(voterTreshold + "," + nVoters.get(voterTreshold.trim()) + ",");
				for(String anomalyTreshold : anomalyTresholds){
					writer.write(voterTreshold + "," + anomalyTreshold.trim() + "," + nVoters.get(voterTreshold.trim()) + ",");
					for(Metric met : getEvaluationMetrics()){
						score = Double.parseDouble(Metric.getAverageMetricValue(detailedMetricScores.get(voterTreshold).get(anomalyTreshold.trim()), met));
						if(met.equals(getReferenceMetric())){
							compactWriter.write(score + ",");
						}
						writer.write(score + ",");
					}
					writer.write("\n");
				}
				compactWriter.write("\n");
			}
			compactWriter.close();
			writer.close();
			AppLogger.logInfo(getClass(), "Best score obtained is '" + getBestScore() + "'");
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write summary files");
		}
	}

	public double getBestScore() {
		List<TimedResult> allResults = new LinkedList<>();
		for(String expName : detailedKnowledgeScores.keySet()){
			if(detailedExperimentsScores.get(expName) != null && detailedExperimentsScores.get(expName).size() > 0)
				allResults.addAll(detailedKnowledgeScores.get(expName));
		}
		return getReferenceMetric().evaluateAnomalyResults(allResults, bestAnomalyThreshold);
	}
	
	public String getFormattedBestScore() {
		return new DecimalFormat("#.##").format(getBestScore());
	}

	public String getBestSetup() {
		return bestSetup;
	}

	public String getBestRuns() {
		return bestRuns;
	}

	public void setBestRuns(String bestRuns) {
		this.bestRuns = bestRuns;
	}
	
	public Metric getReferenceMetric() {
		return iManager.getTargetMetric();
	}

	public Metric[] getEvaluationMetrics() {
		return iManager.loadValidationMetrics();
	}

	public String[] getAnomalyTresholds() {
		return anomalyTresholds;
	}

	public String getEvaluationMetricsScores() {
		return evaluationMetricsScores;
	}

	public String getWritableTag() {
		return writableTag;
	}
	
	public double getFaultsRatio(){
		return faultsRatio;
	}
	
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
		String[] splitted;
		if(writableTag != null){
			splitted = writableTag.split(",");
			if(splitted.length > 3)
				return splitted[2] + " (" + splitted[4] + " - " + splitted[3] + ")";
			else return splitted[2];
		}
		else return null;
	}

	public String[][] getEvaluationGridAveraged() {
		int row = 0;
		String[][] result = new String[detailedMetricScores.keySet().size()*anomalyTresholds.length][getEvaluationMetrics().length + 3];
		for(String voterTreshold : detailedMetricScores.keySet()){
			for(String anomalyTreshold : anomalyTresholds){
				result[row][0] = voterTreshold;
				result[row][1] = anomalyTreshold.trim();
				result[row][2] = nVoters.get(voterTreshold.trim()).toString();
				int col = 3;
				for(Metric met : getEvaluationMetrics()){
					String res = Metric.getAverageMetricValue(detailedMetricScores.get(voterTreshold).get(anomalyTreshold.trim()), met);
					if(res.equals(String.valueOf(Double.NaN))){
						result[row][col++] = "-";
					} else result[row][col++] = String.valueOf(new DecimalFormat("#.##").format(Double.parseDouble(res)));
				}
				row++;
			}
		}
		return result;
	}
	
	public String[][] getEvaluationGrid() {
		String[][] result = new String[1][getEvaluationMetrics().length + 3];
		if(bestSetup != null){
			result[0][0] = bestSetup.split("-")[0].trim();
			result[0][1] = bestSetup.split("-")[1].trim();
			result[0][2] = String.valueOf(nVoters.get(bestSetup.split("-")[0].trim()));
			
			List<TimedResult> allResults = new LinkedList<>();
			for(String expName : detailedKnowledgeScores.keySet()){
				if(detailedExperimentsScores.get(expName) != null && detailedExperimentsScores.get(expName).size() > 0)
					allResults.addAll(detailedKnowledgeScores.get(expName));
			}
			
			int col = 3;
			for(Metric met : getEvaluationMetrics()){
				double res = met.evaluateAnomalyResults(allResults, bestAnomalyThreshold);
				if(Double.isNaN(res)){
					result[0][col++] = "-";
				} else result[0][col++] = String.valueOf(new DecimalFormat("#.##").format(res));
			}
		}
		return result;
	}
	
	public Map<String, List<LabelledResult>> getLabelledScores(){
		Map<AlgorithmVoter, AlgorithmResult> map;
		Map<String, List<LabelledResult>> outMap = new HashMap<>();
		AlgorithmVoter bestVoter = null;
		if(detailedKnowledgeScores != null && detailedKnowledgeScores.size() > 0 && detailedExperimentsScores != null && detailedExperimentsScores.size() > 0){
			for(String expName : detailedKnowledgeScores.keySet()){
				if(detailedExperimentsScores.get(expName) != null && detailedExperimentsScores.get(expName).size() > 0){
					map = detailedExperimentsScores.get(expName).get(0);
					bestVoter = null;
					for(AlgorithmVoter av : map.keySet()){
						if(bestVoter == null || getReferenceMetric().compareResults(bestVoter.getMetricScore(), av.getMetricScore()) > 0)
							bestVoter = av;
					}
					outMap.put(expName, new LinkedList<LabelledResult>());
					for(int i=0;i<detailedExperimentsScores.get(expName).size();i++){
						boolean tag = injections.get(expName).get(i) != null;
						if(i < detailedExperimentsScores.get(expName).size()){
							if(detailedExperimentsScores.get(expName).get(i).get(bestVoter) != null)
								outMap.get(expName).add(new LabelledResult(tag, detailedExperimentsScores.get(expName).get(i).get(bestVoter)));
						}
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

	public String getFeatureAggregationPolicy() {
		return iManager.getDataSeriesDomain();
	}    
	
	public List<AlgorithmVoter> getVoters(){
		return voterList;
	}  

	public int getKFold() {
		return iManager.getKFoldCounter();
	} 
	
	public String getTrainRuns(){
		return loader.getRuns();
	}

	public TrainInfo getTrainInfo() {
		if(tInfo != null)
			return tInfo;
		else return new TrainInfo();
	}

	public FeatureSelectionInfo getFeatureSelectionInfo() {
		if(fsInfo != null)
			return fsInfo;
		else return new FeatureSelectionInfo();
	}

}
