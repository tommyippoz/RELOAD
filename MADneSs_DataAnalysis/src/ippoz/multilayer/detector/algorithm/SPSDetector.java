/**
 * 
 */
package ippoz.multilayer.detector.algorithm;

import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.DataSeriesSnapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.support.AppUtility;
import ippoz.multilayer.detector.graphics.ChartDrawer;
import ippoz.multilayer.detector.graphics.XYChartDrawer;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.special.Erf;

/**
 * The Class SPSDetector.
 * Instantiates SPS on a given indicator and executes the prediction for the specific experiment.
 *
 * @author Tommy
 */
public class SPSDetector extends DataSeriesDetectionAlgorithm {
	
	/** The Constant SPS_UPPER_BOUND. */
	public static final String SPS_UPPER_BOUND = "UpperBound";
	
	/** The Constant SPS_LOWER_BOUND. */
	public static final String SPS_LOWER_BOUND = "LowerBound";
	
	/** The Constant SPS_OBSERVATION. */
	public static final String SPS_OBSERVATION = "Observation";
	
	/** The Constant SPS_ANOMALY. */
	public static final String SPS_ANOMALY = "Anomaly";
	
	/** The Constant SPS_FAILURE. */
	public static final String SPS_FAILURE = "Failure";
	
	/** The Constant IMG_WIDTH. */
	private static final int IMG_WIDTH = 1000;
	
	/** The Constant IMG_HEIGHT. */
	private static final int IMG_HEIGHT = 1000;
	
	public static final String SPS_PDV = "pdv";
	
	public static final String SPS_POV = "pov";
	
	public static final String SPS_PDS = "pds";
	
	public static final String SPS_POS = "pos";
	
	public static final String SPS_M = "m";
	
	public static final String SPS_N = "n";
	
	public static final String SPS_DYN_WEIGHT = "dweight";
	
	/** The SPS calculator. */
	private SPSCalculator calculator;
	
	/** The map of anomaly scores. */
	private TreeMap<Date, Double> anomalies;
	
	/** The map of the failures. */
	private TreeMap<Date, Double> failures;
	
	/** The map of the observations. */
	private TreeMap<Date, Double> observations;
	
	/** The upper threshold. */
	private TreeMap<Date, Double> upperTreshold;
	
	/** The lower threshold. */
	private TreeMap<Date, Double> lowerTreshold;
	
	/** The new thresholds. */
	private double[] newTresholds;

	/**
	 * Instantiates a new SPS detector.
	 *
	 * @param indicator the indicator
	 * @param categoryTag the data category tag
	 * @param conf the configuration
	 */
	public SPSDetector(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		calculator = new SPSCalculator();
		anomalies = new TreeMap<Date, Double>();
		failures = new TreeMap<Date, Double>();
		observations = new TreeMap<Date, Double>();
		upperTreshold = new TreeMap<Date, Double>();
		lowerTreshold = new TreeMap<Date, Double>();
		newTresholds = null;
	}
	
	@Override
	protected double evaluateDataSeriesSnapshot(DataSeriesSnapshot sysSnapshot) {
		double anomalyScore;
		observations.put(sysSnapshot.getTimestamp(), sysSnapshot.getSnapValue());
		if(newTresholds != null) {
			lowerTreshold.put(sysSnapshot.getTimestamp(), newTresholds[0]);
			upperTreshold.put(sysSnapshot.getTimestamp(), newTresholds[1]);
		} else {
			upperTreshold.put(sysSnapshot.getTimestamp(), 2*observations.get(observations.lastKey()));
			lowerTreshold.put(sysSnapshot.getTimestamp(), 0.0);
		}
		anomalyScore = calculateAnomalyScore(sysSnapshot);
		if(anomalyScore >= 1.0)
			anomalies.put(sysSnapshot.getTimestamp(), sysSnapshot.getSnapValue());
		if(sysSnapshot.getInjectedElement() != null && sysSnapshot.getInjectedElement().getTimestamp().compareTo(sysSnapshot.getTimestamp()) == 0)
			failures.put(sysSnapshot.getTimestamp(), sysSnapshot.getSnapValue());
		newTresholds = calculator.calculateTreshold(sysSnapshot);
		return anomalyScore;
	}

	/**
	 * Calculates anomaly score following SPS rules.
	 *
	 * @param sysSnapshot the system snapshot
	 * @return the anomaly score
	 */
	private double calculateAnomalyScore(DataSeriesSnapshot sysSnapshot){
		if(lowerTreshold.size() > 0 && upperTreshold.size() > 0) {
			if(sysSnapshot.getSnapValue() <= upperTreshold.get(upperTreshold.lastKey()) && sysSnapshot.getSnapValue() >= lowerTreshold.get(lowerTreshold.lastKey()))
				return 0;
			else return 1;
		} else return 0;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.algorithm.DetectionAlgorithm#printImageResults(java.lang.String, java.lang.String)
	 */
	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		ChartDrawer chart;
		File outFolder = new File(outFolderName + "/graphics/" + expTag);
		if(!outFolder.exists())
			outFolder.mkdirs();
		chart = new XYChartDrawer(dataSeries.getName(), "Seconds", "Values", getDataset());
		chart.saveToFile(outFolder.getPath() + "/" + dataSeries.getName() + ".png", IMG_WIDTH, IMG_HEIGHT);
	}

	/**
	 * Builds the dataset for the graphical output.
	 *
	 * @return the dataset
	 */
	private HashMap<String, TreeMap<Double, Double>> getDataset() {
		Date refDate = observations.firstKey();
		HashMap<String, TreeMap<Double, Double>> dataset = new HashMap<String, TreeMap<Double, Double>>();
		dataset.put(SPS_OBSERVATION, AppUtility.convertMapTimestamps(refDate, observations));
		dataset.put(SPS_UPPER_BOUND, AppUtility.convertMapTimestamps(refDate, upperTreshold));
		dataset.put(SPS_LOWER_BOUND, AppUtility.convertMapTimestamps(refDate, lowerTreshold));
		dataset.put(SPS_ANOMALY, AppUtility.convertMapTimestamps(refDate, anomalies));
		dataset.put(SPS_FAILURE, AppUtility.convertMapTimestamps(refDate, failures));
		return dataset;
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.algorithm.DetectionAlgorithm#printTextResults(java.lang.String, java.lang.String)
	 */
	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub	
	}
	
	/**
	 * The Class SPSCalculator.
	 * The Core of the SPS elaboration.
	 */
	private class SPSCalculator {
		
		/** The observed values. */
		private LinkedList<SPSBlock> observedValues;
		
		/** The pdv. */
		private double pdv;
		
		/** The pov. */
		private double pov;
		
		/** The pds. */
		private double pds;
		
		/** The pos. */
		private double pos;
		
		/** The m. */
		private double m;
		
		/** The n. */
		@SuppressWarnings("unused")
		private double n;
		
		/** The dynamic weights. */
		private boolean dynamicWeights;
		
		/**
		 * Instantiates a new SPS calculator.
		 */
		public SPSCalculator(){
			observedValues = new LinkedList<SPSBlock>();
			pdv = Double.parseDouble(conf.getItem(SPS_PDV));
			pov = Double.parseDouble(conf.getItem(SPS_POV));
			pds = Double.parseDouble(conf.getItem(SPS_PDS));
			pos = Double.parseDouble(conf.getItem(SPS_POS));
			m = Double.parseDouble(conf.getItem(SPS_M));
			n = Double.parseDouble(conf.getItem(SPS_N));
			dynamicWeights = (Double.parseDouble(conf.getItem(SPS_DYN_WEIGHT)) == 1.0);		
		}
		
		/**
		 * Calculates the new thresholds.
		 *
		 * @param sysSnapshot the current snapshot
		 * @return the new thresholds
		 */
		public double[] calculateTreshold(DataSeriesSnapshot sysSnapshot){
			double calcTreshold = 0;
			addSPSBlock(sysSnapshot.getSnapValue(), sysSnapshot.getTimestamp());
			if(observedValues.size() > 1)
				calcTreshold = computeThreshold();
			else calcTreshold = observedValues.getLast().getObs();
			return new double[]{observedValues.getLast().getObs() - calcTreshold, observedValues.getLast().getObs() + calcTreshold};
		}
		
		/**
		 * Computes thresholds.
		 *
		 * @return the computed threshold
		 */
		private double computeThreshold() {
			double driftBound = driftUpperBound();
			double offsetBound = offsetUpperBound();
			double pred = Erf.erf(pdv)*Math.sqrt(2.0*driftBound)*(2.0/3)*Math.pow(observedValues.getLast().getTimeDiff(), (3/2));
			double sm = Erf.erf(pov)*Math.sqrt(2.0*offsetBound);
			return pred + sm;
		}
		
		/**
		 * Calculates the drift upper bound.
		 *
		 * @return the double
		 */
		private double driftUpperBound(){
			int dof = observedValues.size() - 1;
			ChiSquaredDistribution chiSq = new ChiSquaredDistribution(dof);
			return weightedDriftVariance()*(dof/chiSq.inverseCumulativeProbability(pds));
		}
		
		/**
		 * Calculates the offset upper bound.
		 *
		 * @return the double
		 */
		private double offsetUpperBound(){
			int dof = observedValues.size() - 1;
			ChiSquaredDistribution chiSq = new ChiSquaredDistribution(dof);
			return weightedOffsetVariance()*(dof/chiSq.inverseCumulativeProbability(pos));
		}
		
		/**
		 * Calculates the weighted drift variance.
		 *
		 * @return the double
		 */
		private double weightedDriftVariance(){
			double wdf = 0;
			double weigthSum = getWeightSum();
			double nWeightSum = 0;
			double weigthDMean = 0;
			for(int i=0;i<observedValues.size();i++){
				weigthDMean = weigthDMean + getWeigth(i)*observedValues.get(i).getDrift();
				nWeightSum = nWeightSum + Math.pow(getWeigth(i)/weigthSum, 2);
			}
			weigthDMean = weigthDMean/weigthSum;
			for(int i=0;i<observedValues.size();i++){
				wdf = wdf + (getWeigth(i)/weigthSum)*Math.pow(observedValues.get(i).getDrift() - weigthDMean, 2);
			}
			return wdf/(1-nWeightSum);
		}
		
		/**
		 * Calculates the weighted offset variance.
		 *
		 * @return the double
		 */
		private double weightedOffsetVariance(){
			double wof = 0;
			double weigthSum = getWeightSum();
			double nWeightSum = 0;
			double weigthOMean = 0;
			for(int i=0;i<observedValues.size();i++){
				weigthOMean = weigthOMean + getWeigth(i)*observedValues.get(i).getOffset();
				nWeightSum = nWeightSum + Math.pow(getWeigth(i)/weigthSum, 2);
			}
			weigthOMean = weigthOMean/weigthSum;
			for(int i=0;i<observedValues.size();i++){
				wof = wof + (getWeigth(i)/weigthSum)*Math.pow(observedValues.get(i).getOffset() - weigthOMean, 2);
			}
			return wof/(1-nWeightSum);
		}

		/**
		 * Adds an SPS block.
		 *
		 * @param newValue the new value
		 * @param timestamp the new timestamp
		 */
		private void addSPSBlock(double newValue, Date timestamp){
			observedValues.add(new SPSBlock(newValue, timestamp));
			if(observedValues.size() > m)
				observedValues.removeFirst();
		}
		
		/**
		 * Gets the weight of each observation in the sliding window.
		 *
		 * @param obsIndex the observation index
		 * @return the weight
		 */
		private double getWeigth(int obsIndex){
			if(dynamicWeights){
				return ((obsIndex+1.0)/observedValues.size());
			} else return 1.0;
		}
		
		/**
		 * Gets the weight sum.
		 *
		 * @return the weight sum
		 */
		private double getWeightSum(){
			double tot = 0.0;
			if(dynamicWeights){
				for(int i=0;i<observedValues.size();i++){
					tot = tot + getWeigth(i);
				}
				return tot;
			} else return 1.0*observedValues.size();
		}
		
		/**
		 * The Class SPSBlock.
		 */
		private class SPSBlock {
			
			/** The observation value. */
			private double obs;
			
			/** The timestamp. */
			private Date timestamp;
			
			/** The drift. */
			private double drift;
			
			/** The offset. */
			private double offset;
			
			/** The time difference. */
			private int timeDiff;
			
			/**
			 * Instantiates a new SPS block.
			 *
			 * @param obs the observation
			 * @param timestamp the timestamp
			 */
			public SPSBlock(double obs, Date timestamp) {
				this.obs = obs;
				this.timestamp = timestamp;
				if(observedValues.size() > 0){
					drift = (obs - observedValues.getLast().getDrift())/2;
					offset = obs - observedValues.getLast().getObs();
					timeDiff = (int) ((timestamp.getTime() - observedValues.getLast().getTimestamp().getTime())/1000);
				} else {
					drift = obs;
					offset = obs;
					timeDiff = 1;
				}
			}

			/**
			 * Gets the observation value.
			 *
			 * @return the observation
			 */
			public double getObs() {
				return obs;
			}
			
			/**
			 * Gets the drift.
			 *
			 * @return the drift
			 */
			public double getDrift() {
				return drift;
			}
			
			/**
			 * Gets the offset.
			 *
			 * @return the offset
			 */
			public double getOffset() {
				return offset;
			}
			
			/**
			 * Gets the time difference.
			 *
			 * @return the time difference
			 */
			public int getTimeDiff(){
				return timeDiff;
			}
			
			/**
			 * Gets the timestamp.
			 *
			 * @return the timestamp
			 */
			public Date getTimestamp(){
				return timestamp;
			}
			
		}
		
	}

}
