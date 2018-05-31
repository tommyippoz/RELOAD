/**
 * 
 */
package ippoz.madness.detector.trainer;

import ippoz.madness.commons.datacategory.DataCategory;
import ippoz.madness.detector.algorithm.AutomaticTrainingAlgorithm;
import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.metric.FalsePositiveRate_Metric;
import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.metric.TruePositiveRate_Metric;
import ippoz.madness.detector.reputation.Reputation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

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
		Double currentMetricValue;
		List<Double> metricResults, fprResults, tprResults;
		DetectionAlgorithm algorithm;
		AlgorithmConfiguration bestConf = null;
		AUCCalculator auc = new AUCCalculator();
		try {
			metricScore = Double.NaN;
			for(AlgorithmConfiguration conf : configurations){
				metricResults = new LinkedList<Double>();
				fprResults = new LinkedList<Double>();
				tprResults = new LinkedList<Double>();
				algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), conf);
				if(algorithm instanceof AutomaticTrainingAlgorithm) {
					((AutomaticTrainingAlgorithm)algorithm).automaticTraining(getKnowledgeList(), false);
				}
				for(Knowledge knowledge : getKnowledgeList()){
					metricResults.add(getMetric().evaluateMetric(algorithm, knowledge)[0]);
					fprResults.add(new FalsePositiveRate_Metric(true).evaluateMetric(algorithm, knowledge)[0]);
					tprResults.add(new TruePositiveRate_Metric(true).evaluateMetric(algorithm, knowledge)[0]);
				}
				currentMetricValue = AppUtility.calcAvg(metricResults.toArray(new Double[metricResults.size()]));
				auc.add(AppUtility.calcAvg(fprResults), AppUtility.calcAvg(tprResults));
				if(Double.isNaN(metricScore) || getMetric().compareResults(currentMetricValue, metricScore) == 1){	
					metricScore = currentMetricValue;
					bestConf = (AlgorithmConfiguration) conf.clone();
				}
			}
			algorithm = DetectionAlgorithm.buildAlgorithm(getAlgType(), getDataSeries(), bestConf);
			if(algorithm instanceof AutomaticTrainingAlgorithm) {
				((AutomaticTrainingAlgorithm)algorithm).automaticTraining(getKnowledgeList(), true);
			}
			bestConf.addItem(AlgorithmConfiguration.AUC_SCORE, String.valueOf(auc.calculateScore()));
		} catch (CloneNotSupportedException ex) {
			AppLogger.logException(getClass(), ex, "Unable to clone configuration");
		}
		return bestConf;
	}
	
	private class AUCCalculator {
		
		private int[] INTERPOLATION_DEGREES = {2, 1, 3};
		
		private Map<Double, Double> oPoints;
		
		private static final int APPROXIMATION = 100;
		
		public AUCCalculator(){
			oPoints = new TreeMap<Double, Double>();
			//oPoints.put(0.0, 0.0);
		}
		
		public void add(double fpr, double tpr){
			if(!oPoints.containsKey(fpr))
				oPoints.put(fpr,tpr);
			else if(oPoints.get(fpr) < tpr){
				oPoints.remove(fpr);
				oPoints.put(fpr,tpr);
			}
		}
		
		List<WeightedObservedPoint> toPointList(){
			List<WeightedObservedPoint> list = new LinkedList<WeightedObservedPoint>();
			for(double fpr : oPoints.keySet()){
				list.add(new WeightedObservedPoint(1, fpr, oPoints.get(fpr)));
			}
			return list;
		}
		
		public double calculateScore(){
			double auc = 0;
			double x = 0;
			double delta = 1.0/APPROXIMATION;
			double[] coeff;
			double corrFactor;
			List<Double> aucs = new LinkedList<Double>();
			if(oPoints == null || oPoints.size() == 0)
				return 0.0;
			else if(oPoints.size() == 1){
				return oPoints.get(oPoints.keySet().iterator().next());
			} else {
				for(int polDegree : INTERPOLATION_DEGREES){
					coeff = PolynomialCurveFitter.create(polDegree).fit(toPointList());
					corrFactor = 0;
					for(int j=polDegree; j>=0; j--){
						corrFactor = corrFactor + coeff[j];
					}
					if(corrFactor <=1)
						corrFactor = 1;
					else corrFactor = 1/corrFactor;
					auc = 0;
					for(int i=0; i < APPROXIMATION; i++){
						for(int j=polDegree; j>=0; j--){
							auc = auc + delta*coeff[j]*Math.pow(x, j);
						}
						x = x + delta;
					}
					auc = auc * corrFactor;
					if(auc > 0 && auc <= 1)
						return auc;
					aucs.add(auc);
				}
				return 0.0;
			}
		}
		
	}
}
