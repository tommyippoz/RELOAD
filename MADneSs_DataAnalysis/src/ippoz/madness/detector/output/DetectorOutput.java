/**
 * 
 */
package ippoz.madness.detector.output;

import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.TimedValue;
import ippoz.madness.detector.metric.Metric;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class DetectorOutput {
	
	private double bestScore;
	
	private String bestSetup;
	
	private String bestRuns;

	private Metric referenceMetric;
	
	private Metric[] evaluationMetrics;
	
	private String evaluationMetricsScores;
	
	private String[] anomalyTresholds;
	
	private Map<String, Integer> nVoters;
	
	private Map<String, List<TimedValue>> detailedKnowledgeScores;
	
	private Map<String, Map<String, List<Map<Metric, Double>>>> detailedMetricScores;
	
	private String writableTag;
	
	private double faultsRatio;
	
	public DetectorOutput(double bestScore, String bestSetup, Metric referenceMetric, 
			Metric[] evaluationMetrics, String evaluationMetricsScores, String[] anomalyTresholds, Map<String, Integer> nVoters, 
			Map<String, List<TimedValue>> detailedKnowledgeScores,
			Map<String, Map<String, List<Map<Metric, Double>>>> evaluations,
			String writableTag, double faultsRatio) {
		this.bestScore = bestScore;
		this.bestSetup = bestSetup;
		this.referenceMetric = referenceMetric;
		this.evaluationMetrics = evaluationMetrics;
		this.evaluationMetricsScores = evaluationMetricsScores;
		this.anomalyTresholds = anomalyTresholds;
		this.nVoters = nVoters;
		this.detailedKnowledgeScores = detailedKnowledgeScores;
		this.detailedMetricScores = evaluations;
		this.writableTag = writableTag;
		this.faultsRatio = faultsRatio;
	}
	
	public void printDetailedKnowledgeScores(String outputFolder){
		BufferedWriter writer;
		Date timedRef;
		try {
			if(detailedKnowledgeScores != null && detailedKnowledgeScores.size() > 0){
				writer = new BufferedWriter(new FileWriter(new File(buildPath(outputFolder) + "algorithmscores.csv")));
				writer.write("exp,index,score\n");
				for(String expName : detailedKnowledgeScores.keySet()){
					timedRef = detailedKnowledgeScores.get(expName).get(0).getDate();
					for(TimedValue td : detailedKnowledgeScores.get(expName)){
						writer.write(expName + "," + td.getDateOffset(timedRef) + "," + td.getValue() + "\n");
					}
				}
				writer.close();
			}
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write summary files");
		}
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
			for(Metric met : evaluationMetrics){
				writer.write(met.getMetricName() + ",");
			}
			writer.write("\n");
			for(String voterTreshold : detailedMetricScores.keySet()){
				compactWriter.write(voterTreshold + "," + nVoters.get(voterTreshold.trim()) + ",");
				for(String anomalyTreshold : anomalyTresholds){
					writer.write(voterTreshold + "," + anomalyTreshold.trim() + "," + nVoters.get(voterTreshold.trim()) + ",");
					for(Metric met : evaluationMetrics){
						score = Double.parseDouble(Metric.getAverageMetricValue(detailedMetricScores.get(voterTreshold).get(anomalyTreshold.trim()), met));
						if(met.equals(referenceMetric)){
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
			AppLogger.logInfo(getClass(), "Best score obtained is '" + bestScore + "'");
		} catch(IOException ex){
			AppLogger.logException(getClass(), ex, "Unable to write summary files");
		}
	}

	public double getBestScore() {
		return bestScore;
	}
	
	public String getFormattedBestScore() {
		return new DecimalFormat("#.##").format(bestScore);
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
		return referenceMetric;
	}

	public Metric[] getEvaluationMetrics() {
		return evaluationMetrics;
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

	public String[][] getEvaluationGrid() {
		int row = 0;
		String[][] result = new String[detailedMetricScores.keySet().size()*anomalyTresholds.length][evaluationMetrics.length + 3];
		for(String voterTreshold : detailedMetricScores.keySet()){
			for(String anomalyTreshold : anomalyTresholds){
				result[row][0] = voterTreshold;
				result[row][1] = anomalyTreshold.trim();
				result[row][2] = nVoters.get(voterTreshold.trim()).toString();
				int col = 3;
				for(Metric met : evaluationMetrics){
					String res = Metric.getAverageMetricValue(detailedMetricScores.get(voterTreshold).get(anomalyTreshold.trim()), met);
					if(res.equals(String.valueOf(Double.NaN)))
						result[row][col++] = "-";
					result[row][col++] = String.valueOf(new DecimalFormat("#.##").format(Double.parseDouble(res)));
				}
				row++;
			}
		}
		return result;
	}

}
