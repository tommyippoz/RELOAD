/**
 * 
 */
package ippoz.madness.detector.output;

import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.metric.Metric;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
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
	
	private Map<String, Map<String, List<Map<Metric, Double>>>> evaluations;
	
	private String writableTag;
	
	public DetectorOutput(double bestScore, String bestSetup, Metric referenceMetric, 
			Metric[] evaluationMetrics, String evaluationMetricsScores, String[] anomalyTresholds,
			Map<String, Integer> nVoters, Map<String, Map<String, List<Map<Metric, Double>>>> evaluations,
			String writableTag) {
		this.bestScore = bestScore;
		this.bestSetup = bestSetup;
		this.referenceMetric = referenceMetric;
		this.evaluationMetrics = evaluationMetrics;
		this.evaluationMetricsScores = evaluationMetricsScores;
		this.anomalyTresholds = anomalyTresholds;
		this.nVoters = nVoters;
		this.evaluations = evaluations;
		this.writableTag = writableTag;
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
			for(String voterTreshold : evaluations.keySet()){
				compactWriter.write(voterTreshold + "," + nVoters.get(voterTreshold.trim()) + ",");
				for(String anomalyTreshold : anomalyTresholds){
					writer.write(voterTreshold + "," + anomalyTreshold.trim() + "," + nVoters.get(voterTreshold.trim()) + ",");
					for(Metric met : evaluationMetrics){
						score = Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(voterTreshold).get(anomalyTreshold.trim()), met));
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
	
	public String getDataset(){
		if(writableTag != null)
			return writableTag.split(",")[0];
		else return null;
	}
	
	public String getAlgorithm(){
		if(writableTag != null)
			return writableTag.split(",")[2];
		else return null;
	}

	public String[][] getEvaluationGrid() {
		int row = 0;
		String[][] result = new String[evaluations.keySet().size()*anomalyTresholds.length][evaluationMetrics.length + 3];
		for(String voterTreshold : evaluations.keySet()){
			for(String anomalyTreshold : anomalyTresholds){
				result[row][0] = voterTreshold;
				result[row][1] = anomalyTreshold.trim();
				result[row][2] = nVoters.get(voterTreshold.trim()).toString();
				int col = 3;
				for(Metric met : evaluationMetrics){
					result[row][col++] = String.valueOf(new DecimalFormat("#.##").format(Double.parseDouble(Metric.getAverageMetricValue(evaluations.get(voterTreshold).get(anomalyTreshold.trim()), met))));
				}
				row++;
			}
		}
		return result;
	}

}
