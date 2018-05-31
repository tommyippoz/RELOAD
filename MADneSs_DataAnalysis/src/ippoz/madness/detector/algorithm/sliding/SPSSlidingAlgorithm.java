/**
 * 
 */
package ippoz.madness.detector.algorithm.sliding;

import ippoz.madness.detector.algorithm.DataSeriesSlidingAlgorithm;
import ippoz.madness.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.madness.detector.commons.dataseries.DataSeries;
import ippoz.madness.detector.commons.knowledge.Knowledge;
import ippoz.madness.detector.commons.knowledge.SlidingKnowledge;
import ippoz.madness.detector.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.madness.detector.commons.knowledge.snapshot.Snapshot;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.special.Erf;

/**
 * The Class SPSDetector.
 * Instantiates SPS on a given indicator and executes the prediction for the specific experiment.
 *
 * @author Tommy
 */
public class SPSSlidingAlgorithm extends DataSeriesSlidingAlgorithm {
	
	/** The Constant SPS_PDV. */
	public static final String SPS_PDV = "pdv";
	
	/** The Constant SPS_POV. */
	public static final String SPS_POV = "pov";
	
	/** The Constant SPS_PDS. */
	public static final String SPS_PDS = "pds";
	
	/** The Constant SPS_POS. */
	public static final String SPS_POS = "pos";
	
	/** The Constant SPS_DYN_WEIGHT. */
	public static final String SPS_DYN_WEIGHT = "dweight";
	
	/** The pdv. */
	private double pdv;
	
	/** The pov. */
	private double pov;
	
	/** The pds. */
	private double pds;
	
	/** The pos. */
	private double pos;
		
	/** The dynamic weights. */
	private boolean dynamicWeights;

	/**
	 * Instantiates a new SPS detector.
	 *
	 * @param dataSeries the data series
	 * @param conf the configuration
	 */
	public SPSSlidingAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		pdv = Double.parseDouble(conf.getItem(SPS_PDV));
		pov = Double.parseDouble(conf.getItem(SPS_POV));
		pds = Double.parseDouble(conf.getItem(SPS_PDS));
		pos = Double.parseDouble(conf.getItem(SPS_POS));
		dynamicWeights = (Double.parseDouble(conf.getItem(SPS_DYN_WEIGHT)) == 1.0);	
	}
	
	/* (non-Javadoc)
	 * @see ippoz.madness.detector.algorithm.DataSeriesSlidingAlgorithm#evaluateSlidingSnapshot(ippoz.madness.detector.commons.knowledge.SlidingKnowledge, java.util.List, ippoz.madness.detector.commons.knowledge.snapshot.Snapshot)
	 */
	@Override
	protected double evaluateSlidingSnapshot(SlidingKnowledge sKnowledge, List<Snapshot> snapList, Snapshot dsSnapshot) {
		double[] thresholds = calculateTreshold(parseBlocks(snapList));
		double snapValue = ((DataSeriesSnapshot)dsSnapshot).getSnapValue().getFirst();
		if(snapValue <= thresholds[1] && snapValue >= thresholds[0])
			return 0.0;
		else return 1.0;
	}
	
	/* (non-Javadoc)
	 * @see ippoz.madness.detector.algorithm.DataSeriesSlidingAlgorithm#evaluateDataSeriesSnapshot(ippoz.madness.detector.commons.knowledge.Knowledge, ippoz.madness.detector.commons.knowledge.snapshot.Snapshot, int)
	 */
	@Override
	protected double evaluateDataSeriesSnapshot(Knowledge knowledge, Snapshot sysSnapshot, int currentIndex) {
		double[] thresholds = calculateTreshold(parseBlocks(knowledge.toArray(getAlgorithmType(), getDataSeries())));
		double snapValue = ((DataSeriesSnapshot) sysSnapshot).getSnapValue().getFirst();
		if(snapValue <= thresholds[1] && snapValue >= thresholds[0])
			return 0.0;
		else return 1.0;
	}

	/**
	 * Parses the blocks.
	 *
	 * @param array the array
	 * @return the list
	 */
	private List<SPSBlock> parseBlocks(List<Snapshot> array) {
		List<SPSBlock> blockList = new LinkedList<SPSBlock>();
		for(Snapshot snap : array){
			if(blockList.size() == 0)
				blockList.add(new SPSBlock(((DataSeriesSnapshot) snap).getSnapValue().getFirst(), snap.getTimestamp(), null));
			else blockList.add(new SPSBlock(((DataSeriesSnapshot) snap).getSnapValue().getFirst(), snap.getTimestamp(), blockList.get(blockList.size()-1)));
		}
		return blockList;
	}
		
	/**
	 * Calculates the new thresholds.
	 *
	 * @param observedValues the observed values
	 * @return the new thresholds
	 */
	public double[] calculateTreshold(List<SPSBlock> observedValues){
		double calcTreshold = 0;
		if(observedValues == null || observedValues.size() == 0)
			return new double[]{Double.MIN_VALUE, Double.MAX_VALUE};
		else if(observedValues.size() == 1){
			if(observedValues.get(0).getObs() >= 0)
				return new double[]{0.0, 2*observedValues.get(0).getObs()};
			else return new double[]{2*observedValues.get(0).getObs(), 0.0};
		} else {
			calcTreshold = computeThreshold(observedValues);
			return new double[]{observedValues.get(observedValues.size()-1).getObs() - calcTreshold, observedValues.get(observedValues.size()-1).getObs() + calcTreshold};
		}
	}

	/**
	 * Computes thresholds.
	 *
	 * @param observedValues the observed values
	 * @return the computed threshold
	 */
	private double computeThreshold(List<SPSBlock> observedValues) {
		double bounds[] = calculateBounds(observedValues);
		double pred = Erf.erf(pdv)*Math.sqrt(2.0*bounds[0])*(2.0/3)*Math.pow(observedValues.get(observedValues.size()-1).getTimeDiff(), (3/2));
		double sm = Erf.erf(pov)*Math.sqrt(2.0*bounds[1]);
		return pred + sm;
	}
	
	/**
	 * Calculate bounds.
	 *
	 * @param observedValues the observed values
	 * @return the double[]
	 */
	private double[] calculateBounds(List<SPSBlock> observedValues){
		int dof = observedValues.size() - 1;
		ChiSquaredDistribution chiSq = new ChiSquaredDistribution(dof);
		double wdf = 0, wof = 0;
		double weigthSum = getWeightSum(observedValues);
		double nWeightSum = getWeigthQuadraticSum(observedValues) / Math.pow(weigthSum, 2);
		double weigthDMean = 0, weigthOMean = 0;
		
		for(int i=0;i<observedValues.size();i++){
			weigthDMean = weigthDMean + getWeigth(i, observedValues)*observedValues.get(i).getDrift();
			weigthOMean = weigthOMean + getWeigth(i, observedValues)*observedValues.get(i).getOffset();
		}
		
		weigthDMean = weigthDMean/weigthSum;
		weigthOMean = weigthOMean/weigthSum;
		for(int i=0;i<observedValues.size();i++){
			wdf = wdf + (getWeigth(i, observedValues)/weigthSum)*Math.pow(observedValues.get(i).getDrift() - weigthDMean, 2);
			wof = wof + (getWeigth(i, observedValues)/weigthSum)*Math.pow(observedValues.get(i).getOffset() - weigthOMean, 2);
		}
		
		double result[] = new double[2];
		if(pds == pos){
			double den = dof/chiSq.inverseCumulativeProbability(pds);
			result[0] = wdf/(1-nWeightSum)*den;
			result[1] = wof/(1-nWeightSum)*den;
		} else {
			result[0] = wdf/(1-nWeightSum)*(dof/chiSq.inverseCumulativeProbability(pds));
			result[1] = wof/(1-nWeightSum)*(dof/chiSq.inverseCumulativeProbability(pos));
		}
		
		return result;
	}
	
	/**
	 * Gets the weight of each observation in the sliding window.
	 *
	 * @param obsIndex the observation index
	 * @param observedValues the observed values
	 * @return the weight
	 */
	private double getWeigth(int obsIndex, List<SPSBlock> observedValues){
		if(dynamicWeights){
			return ((obsIndex+1.0)/observedValues.size());
		} else return 1.0;
	}
	
	/**
	 * Gets the weight of each observation in the sliding window.
	 *
	 * @param observedValues the observed values
	 * @return the weight
	 */
	private double getWeigthQuadraticSum(List<SPSBlock> observedValues){
		int nEl = observedValues.size();
		if(dynamicWeights)
			return (nEl+1)*(2*nEl+1)/(6*nEl);
		else return nEl;
	}
	
	/**
	 * Gets the weight sum.
	 *
	 * @param observedValues the observed values
	 * @return the weight sum
	 */
	private double getWeightSum(List<SPSBlock> observedValues){
		if(dynamicWeights){
			return (observedValues.size() + 1.0)/2.0;
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
		 * @param last the last
		 */
		public SPSBlock(double obs, Date timestamp, SPSBlock last) {
			this.obs = obs;
			this.timestamp = timestamp;
			if(last != null){
				drift = (obs - last.getDrift())/2;
				offset = obs - last.getObs();
				timeDiff = (int) ((timestamp.getTime() - last.getTimestamp().getTime())/1000);
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
