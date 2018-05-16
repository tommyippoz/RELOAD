/**
 * 
 */
package ippoz.madness.detector.trainer;

import ippoz.madness.detector.algorithm.AutomaticTrainingAlgorithm;
import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.metric.FalsePositiveRate_Metric;
import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.metric.TruePositiveRate_Metric;
import ippoz.madness.detector.reputation.Reputation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

/**
 * The Class ConfigurationSelectorTrainer.
 * This is used from algorithms which can select the best configuration out of a set of possible ones.
 * 
 * @author Tommy
 *
 */
public class ConfigurationSelectorTrainer extends AlgorithmTrainer {

	/** The possible configurations. */
	private List<AlgorithmConfiguration> configurations;
	
	/**
	 * Instantiates a new algorithm trainer.
	 *
	 * @param algTag the algorithm tag
	 * @param dataSeries the chosen data series
	 * @param metric the used metric
	 * @param reputation the used reputation metric
	 * @param expList the considered train data
	 */
	public ConfigurationSelectorTrainer(AlgorithmType algTag, DataSeries dataSeries, Metric metric, Reputation reputation, List<Knowledge> kList, List<AlgorithmConfiguration> basicConfigurations) {
		super(algTag, dataSeries, metric, reputation, kList);
		configurations = confClone(basicConfigurations);
	}
	
	/**
	 * Clones the configurations to avoid updating the same Java structures.
	 *
	 * @param inConf the configurations to clone
	 * @return the cloned list of configuration
	 */
	private List<AlgorithmConfiguration> confClone(List<AlgorithmConfiguration> inConf) {
		List<AlgorithmConfiguration> list = new ArrayList<AlgorithmConfiguration>(inConf.size());
		try {
			for(AlgorithmConfiguration conf : inConf){
				list.add((AlgorithmConfiguration) conf.clone());
			}
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone Configurations");
		}
		return list;
	}

	@Override
	protected AlgorithmConfiguration lookForBestConfiguration() {
		Double bestMetricValue = Double.NaN;
		Double currentMetricValue;
		List<Double> metricResults, fprResults, tprResults;
		DetectionAlgorithm algorithm;
		AlgorithmConfiguration bestConf = null;
		AUCCalculator auc = new AUCCalculator();
		try {
			for(AlgorithmConfiguration conf : configurations){
				metricResults = new LinkedList<Double>();
				fprResults = new LinkedList<Double>();
				tprResults = new LinkedList<Double>();
				algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), conf);
				if(algorithm instanceof AutomaticTrainingAlgorithm) {
					((AutomaticTrainingAlgorithm)algorithm).automaticTraining(getKnowledgeList());
				}
				for(Knowledge knowledge : getKnowledgeList()){
					metricResults.add(getMetric().evaluateMetric(algorithm, knowledge)[0]);
					fprResults.add(new FalsePositiveRate_Metric(true).evaluateMetric(algorithm, knowledge)[0]);
					tprResults.add(new TruePositiveRate_Metric(true).evaluateMetric(algorithm, knowledge)[0]);
				}
				currentMetricValue = AppUtility.calcAvg(metricResults.toArray(new Double[metricResults.size()]));
				auc.add(AppUtility.calcAvg(fprResults), AppUtility.calcAvg(tprResults));
				if(bestMetricValue.isNaN() || getMetric().compareResults(currentMetricValue, bestMetricValue) == 1){	
					bestMetricValue = currentMetricValue;
					bestConf = (AlgorithmConfiguration) conf.clone();
				}
			}
			bestConf.addItem(AlgorithmConfiguration.AUC_SCORE, String.valueOf(auc.calculateScore()));
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone configuration");
		}
		return bestConf;
	}
	
	private class AUCCalculator {
		
		private WeightedObservedPoints oPoints;
		
		private static final int APPROXIMATION = 100;
		
		public AUCCalculator(){
			oPoints = new WeightedObservedPoints();
		}
		
		public void add(double fpr, double tpr){
			oPoints.add(fpr, tpr);
		}
		
		public double calculateScore(){
			double auc = 0;
			double x = 0;
			double delta = 1.0/APPROXIMATION;
			double[] coeff = PolynomialCurveFitter.create(2).fit(oPoints.toList());
			coeff[2] = coeff[2] < 0 ? 0.0 : coeff[2];
			double corrFactor = coeff[2] + coeff[1] + coeff[0] <= 1 ? 1.0 : 1/(coeff[2] + coeff[1] + coeff[0]); 
			for(int i=0; i < APPROXIMATION;i++){
				auc = auc + delta*(coeff[2]*x*x + coeff[1]*x + coeff[0]);
				x = x + delta;
			}
			return auc*corrFactor;
		}
		
	}
}
