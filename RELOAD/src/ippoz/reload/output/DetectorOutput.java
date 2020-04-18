/**
 * 
 */
package ippoz.reload.output;

import ippoz.reload.algorithm.result.AlgorithmResult;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.failure.InjectedElement;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.loader.LoaderBatch;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.featureselection.FeatureSelectorType;
import ippoz.reload.info.FeatureSelectionInfo;
import ippoz.reload.info.TrainInfo;
import ippoz.reload.manager.InputManager;
import ippoz.reload.metric.Metric;
import ippoz.reload.voter.ScoresVoter;
import ippoz.reload.voter.VotingResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
	
	private double bestScore;
	
	private ScoresVoter bestVoter;
	
	private String bestRuns;
	
	private List<AlgorithmModel> modelList;
	
	private Map<LoaderBatch, List<VotingResult>> votingScores;
	
	private Loader loader;
	
	private List<DataSeries> selectedSeries;
	
	private String writableTag;
	
	private List<AlgorithmType> algorithms;
	
	private double faultsRatio;
	
	private Map<DataSeries, Map<FeatureSelectorType, Double>> selectedFeatures;	
	
	private Map<LoaderBatch, List<LabelledResult>> labelledScores;
	
	public DetectorOutput(InputManager iManager, List<AlgorithmType> algorithms, double bestScore, ScoresVoter bestSetup, 
			List<AlgorithmModel> modelList,
			Map<LoaderBatch, List<VotingResult>> votingScores,
			Loader loader, Map<LoaderBatch, List<Map<AlgorithmModel, AlgorithmResult>>> detailedExperimentsScores, 
			List<DataSeries> selectedSeries, Map<DataSeries, Map<FeatureSelectorType, Double>> selectedFeatures,
			String writableTag, double faultsRatio) {
		this.iManager = iManager;
		this.algorithms = algorithms;
		this.bestScore = bestScore;
		this.bestVoter = bestSetup;
		this.modelList = modelList;
		this.votingScores = votingScores;
		this.loader = loader;
		this.selectedSeries = selectedSeries;
		this.selectedFeatures = selectedFeatures;
		this.writableTag = writableTag;
		this.faultsRatio = faultsRatio;
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
		String path = basePath + getDataset() + getAlgorithm() + File.separatorChar;
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

	public double getBestScore() {
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
		return new DecimalFormat("#.##").format(getBestScore());
	}

	public ScoresVoter getVoter() {
		return bestVoter;
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
		return algorithms.toString().replace(",", "");
	}

	private String[][] getGrid(String basePath, String tag) {
		BufferedReader reader = null;
		List<String> gridRows = new LinkedList<>();
		try {
			reader = new BufferedReader(new FileReader(new File(basePath + tag + "Summary.csv")));
			boolean header = true;
			while(reader.ready()){
				String readline = reader.readLine();
				if(readline != null){
					readline = readline.trim();
					if(!readline.startsWith("*")){
						if(header)
							header = !header;
						else {
							gridRows.add(readline);
						}
					}
				}
			}
			reader.close();
			int i = 0;
			String[][] grid = new String[gridRows.size()][];
			for(String row : gridRows){
				grid[i] = row.split(",");
				for(int j=0;j<grid[i].length;j++){
					if(grid[i][j].contains(".") && AppUtility.isNumber(grid[i][j])){
						double val = Double.parseDouble(grid[i][j]);
						grid[i][j] = String.valueOf(new DecimalFormat("#.##").format(val));
					}
				}
				i++;
			}
			return grid;
		} catch (Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to read " + tag + " summary file");
		}
		return null;
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
	
	public String[][] getOptimizationGrid(String basePath) {
		return getGrid(basePath, "optimization"); 
	}
	
	public String[][] getEvaluationGrid(String basePath) {
		return getGrid(basePath, "validation"); 
	}
	
	public Map<LoaderBatch, List<LabelledResult>> getLabelledScores(){
		return labelledScores;
	}
	
	public Map<LoaderBatch, List<LabelledResult>> buildLabelledScores(Map<LoaderBatch, List<Map<AlgorithmModel, AlgorithmResult>>> detailedExperimentsScores){
		Map<LoaderBatch, List<LabelledResult>> outMap = new HashMap<>();
		if(votingScores != null && votingScores.size() > 0){
			for(LoaderBatch expName : votingScores.keySet()){
				outMap.put(expName, new LinkedList<LabelledResult>());
				for(int i=0;i<detailedExperimentsScores.get(expName).size();i++){
					VotingResult ar = votingScores.get(expName).get(i);
					if(i < detailedExperimentsScores.get(expName).size()){
						outMap.get(expName).add(new LabelledResult(ar.getInjection() != null, ar));
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
		double bestScore = Double.NEGATIVE_INFINITY;
		DataSeries toReturn = null;
		if(modelList!= null && modelList.size() > 0){
			for(AlgorithmModel voter : modelList){
				if(voter.getMetricScore() > bestScore){
					bestScore = voter.getMetricScore();
					toReturn = voter.getDataSeries();
				}
			}
			if(toReturn != null)
				return toReturn.getName();
			else return "";
		} else return "";
	}

	public String getFeatureAggregationPolicy() {
		return iManager.getDataSeriesDomain();
	}    
	
	public List<AlgorithmModel> getVoters(){
		return modelList;
	}  

	public int getKFold() {
		return iManager.getKFoldCounter();
	} 
	
	public String getTrainRuns(){
		return loader.getRuns();
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
